package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.DeflateDocumentRequest;
import ee.adit.pojo.DeflateDocumentResponse;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "deflateDocument" web method (web service request).
 * Contains request input validation, request-specific workflow and response
 * composition.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "deflateDocument", version = "v1")
@Component
public class DeflateDocumentEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(DeflateDocumentEndpoint.class);
    
    private UserService userService;
    
    private DocumentService documentService;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("deflateDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "deflateDocument" request.
     * 
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        DeflateDocumentResponse response = new DeflateDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;

        try {
            logger.debug("deflateDocument.v1 invoked.");
            DeflateDocumentRequest request = (DeflateDocumentRequest) requestObject;
            if (request != null) {
                documentId = request.getDocumentId();
            }
            CustomXTeeHeader header = this.getHeader();
            String applicationName = header.getInfosysteem();

            // Log request
            Util.printHeader(header);
            printRequest(request);

            // Check header for required fields
            checkHeader(header);

            // Check request body
            checkRequest(request);

            // Kontrollime, kas päringu käivitanud infosüsteem on ADITis
            // registreeritud
            boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
            if (!applicationRegistered) {
                AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
                aditCodedException.setParameters(new Object[] {applicationName });
                throw aditCodedException;
            }

            // Kontrollime, kas päringu käivitanud infosüsteem tohib
            // andmeid muuta (või üldse näha)
            int accessLevel = this.getUserService().getAccessLevel(applicationName);
            if (accessLevel != 2) {
                AditCodedException aditCodedException = new AditCodedException(
                        "application.insufficientPrivileges.write");
                aditCodedException.setParameters(new Object[] {applicationName });
                throw aditCodedException;
            }

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

            // Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja
            // lahkunud)
            if ((user.getActive() == null) || !user.getActive()) {
                AditCodedException aditCodedException = new AditCodedException("user.inactive");
                aditCodedException.setParameters(new Object[] {user.getUserCode()});
                throw aditCodedException;
            }

            // Check whether or not the application has rights to
            // modify current user's data.
            int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
            if (applicationAccessLevelForUser != 2) {
                AditCodedException aditCodedException = new AditCodedException(
                        "application.insufficientPrivileges.forUser.write");
                aditCodedException.setParameters(new Object[] {applicationName, user.getUserCode() });
                throw aditCodedException;
            }

            this.getDocumentService().deflateDocument(request.getDocumentId(), user.getUserCode(), applicationName);

            // If deflation was successful then add history events
            // for deflation and locking.
            DocumentHistory deflateEvent = new DocumentHistory(DocumentService.HISTORY_TYPE_DEFLATE, documentId,
                    requestDate.getTime(), user, xroadRequestUser, header);
            deflateEvent.setDescription(DocumentService.DOCUMENT_HISTORY_DESCRIPTION_DEFLATE);

            DocumentHistory lockEvent = new DocumentHistory(DocumentService.HISTORY_TYPE_LOCK, documentId, requestDate
                    .getTime(), user, xroadRequestUser, header);
            lockEvent.setDescription(DocumentService.DOCUMENT_HISTORY_DESCRIPTION_LOCK);
            this.getDocumentService().getDocumentHistoryDAO().save(deflateEvent);
            this.getDocumentService().getDocumentHistoryDAO().save(lockEvent);

            // Set response messages
            response.setSuccess(new Success(true));
            messages.setMessage(this.getMessageService()
                    .getMessages("request.deflateDocument.success", new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.deflateDocument.success",
                    new Object[] {}, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

        } catch (Exception e) {
            logger.error("Exception: ", e);
            String errorMessage = null;
            response.setSuccess(new Success(false));
            ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

            if (e instanceof AditCodedException) {
                logger.debug("Adding exception messages to response object.");
                arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
                errorMessage = this.getMessageService().getMessage(e.getMessage(),
                        ((AditCodedException) e).getParameters(), Locale.ENGLISH);
                errorMessage = "ERROR: " + errorMessage;
            } else {
            	arrayOfMessage.setMessage(this.getMessageService().getMessages(MessageService.GENERIC_ERROR_CODE, new Object[]{}));
                errorMessage = "ERROR: " + e.getMessage();
            }

            additionalInformationForLog = errorMessage;
            super.logError(documentId, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

            logger.debug("Adding exception messages to response object.");
            response.setMessages(arrayOfMessage);
        }

        super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
        return response;
    }

    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        DeflateDocumentResponse response = new DeflateDocumentResponse();
        response.setSuccess(new Success(false));
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        return response;
    }

    /**
     * Validates request body and makes sure that all required fields exist and
     * are not empty. <br>
     * <br>
     * Throws {@link AditCodedException} if any errors in request data are
     * found.
     * 
     * @param request
     *            Request body as {@link DeflateDocumentRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
     */
    private void checkRequest(DeflateDocumentRequest request) throws AditCodedException {
        if (request != null) {
            if (request.getDocumentId() <= 0) {
                throw new AditCodedException("request.body.undefined.documentId");
            }
        } else {
            throw new AditCodedException("request.body.empty");
        }
    }

    /**
     * Writes request parameters to application DEBUG log.
     * 
     * @param request
     *            Request body as {@link DeflateDocumentRequest} object.
     */
    private void printRequest(DeflateDocumentRequest request) {
        logger.debug("-------- DeflateDocumentRequest -------");
        logger.debug("Document ID: " + String.valueOf(request.getDocumentId()));
        logger.debug("--------------------------------------");
    }
    
    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
}
