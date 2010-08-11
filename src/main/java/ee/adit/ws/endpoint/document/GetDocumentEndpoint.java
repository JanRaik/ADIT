package ee.adit.ws.endpoint.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentRequest;
import ee.adit.pojo.GetDocumentResponse;
import ee.adit.pojo.GetDocumentResponseDocument;
import ee.adit.pojo.Message;
import ee.adit.pojo.OutputDocument;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getDocument", version = "v1")
@Component
public class GetDocumentEndpoint extends AbstractAditBaseEndpoint {
	private static Logger LOG = Logger.getLogger(GetDocumentEndpoint.class);
	private UserService userService;
	private DocumentService documentService;

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
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		GetDocumentResponse response = new GetDocumentResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		List<Long> fileIdList = new ArrayList<Long>();
		boolean includeFileContents = false;

		try {
			LOG.debug("getDocument.v1 invoked.");
			GetDocumentRequest request = (GetDocumentRequest) requestObject;
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
			this.getUserService().checkApplicationRegistered(applicationName);

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid näha
			this.getUserService().checkApplicationReadPrivilege(applicationName);

			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood();
			AditUser user = this.getUserService().getUserByID(userCode);
			if (user == null) {
				AditCodedException aditCodedException = new AditCodedException("user.nonExistent");
				aditCodedException.setParameters(new Object[] { userCode });
				throw aditCodedException;
			}

			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if ((user.getActive() == null) || !user.getActive()) {
				AditCodedException aditCodedException = new AditCodedException("user.inactive");
				aditCodedException.setParameters(new Object[] { userCode });
				throw aditCodedException;
			}
			
			// Check whether or not the application has rights to
			// read current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser < 1) {				
				AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.read");
				aditCodedException.setParameters(new Object[] { applicationName, user.getUserCode() });
				throw aditCodedException;
			}

			Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

			// Kontrollime, kas ID-le vastav dokument on olemas
			if (doc != null) {
				if ((doc.getDeleted() == null) || (!doc.getDeleted())) {
					if ((doc.getDeflated() == null) || (!doc.getDeflated())) {
						boolean saveDocument = false;
						
						// Dokumenti saab alla laadida, kui dokument:
						// a) kuulub päringu käivitanud kasutajale
						// b) on päringu käivitanud kasutajale välja jagatud
						boolean userIsDocOwner = false;
						if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
							userIsDocOwner = true;
						} else {
							if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
								Iterator it = doc.getDocumentSharings().iterator();
								while (it.hasNext()) {
									DocumentSharing sharing = (DocumentSharing)it.next();
									if (sharing.getUserCode().equalsIgnoreCase(userCode)) {
										userIsDocOwner = true;
										
										if (sharing.getLastAccessDate() == null) {
											sharing.setLastAccessDate(new Date());
											saveDocument = true;
										}
										
										break;
									}
								}
							}
						}
						
						// Kui kasutaja tohib dokumendile ligi pääseda, siis tagastame dokumendi
						if (userIsDocOwner) {
							includeFileContents = (request.isIncludeFileContents() == null) ? false : request.isIncludeFileContents(); 
							OutputDocument resultDoc = this.documentService.getDocumentDAO().getDocumentWithFiles(
									doc.getId(),
									null,
									false, false, includeFileContents,
									this.getConfiguration().getTempDir(),
									this.getMessageSource().getMessage("files.nonExistentOrDeleted", new Object[] { }, Locale.ENGLISH),
									user.getUserCode());
							
							if (resultDoc != null) {
								// Remember file IDs for logging later on.
								List<OutputDocumentFile> docFiles = resultDoc.getFiles().getFiles();
								if ((docFiles != null) && (docFiles.size() > 0)) {
									for (OutputDocumentFile file : docFiles) {
										fileIdList.add(file.getId());
									}
								}
								
								// 1. Convert java list to XML string and output to file
								String xmlFile = marshal(resultDoc);
								Util.joinSplitXML(xmlFile, "data");
								
								// 2. GZip the temporary file
								// Base64 encoding will be done at SOAP envelope level
								String gzipFileName = Util.gzipFile(xmlFile, this.getConfiguration().getTempDir());
		
								// 3. Add as an attachment
								String contentID = addAttachment(gzipFileName);
								GetDocumentResponseDocument responseDoc = new GetDocumentResponseDocument();
								responseDoc.setHref("cid:" + contentID);
								response.setDocument(responseDoc);
								
								// If document has not been viewed by current user before then mark it viewed.
								boolean isViewed = false;
								if ((doc.getDocumentHistories() != null) && (!doc.getDocumentHistories().isEmpty())) {
									Iterator it = doc.getDocumentHistories().iterator();
									while (it.hasNext()) {
										DocumentHistory event = (DocumentHistory)it.next();
										if (event.getDocumentHistoryType().equalsIgnoreCase(DocumentService.HistoryType_MarkViewed)
											&& event.getUserCode().equalsIgnoreCase(userCode)) {
											isViewed = true;
											break;
										}
									}
								}
								
								if (!isViewed) {
									// Add first viewing history event
									DocumentHistory historyEvent = new DocumentHistory();
									historyEvent.setRemoteApplicationName(applicationName);
									historyEvent.setDocumentId(doc.getId());
									historyEvent.setDocumentHistoryType(DocumentService.HistoryType_MarkViewed);
									historyEvent.setEventDate(new Date());
									historyEvent.setUserCode(userCode);
									doc.getDocumentHistories().add(historyEvent);
									saveDocument = true;
								}
								
								if (saveDocument) {
									this.documentService.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);
								}
								
								// If it was the first time for this particular user to
								// view the document then send scheduler notification to
								// document owner.
								// Notification does not need to be sent if user viewed
								// his/her own document.
								if (!user.getUserCode().equalsIgnoreCase(doc.getCreatorCode())) {
									AditUser docCreator = this.getUserService().getUserByID(doc.getCreatorCode());
									if (!isViewed && (docCreator != null) && (userService.findNotification(docCreator.getUserNotifications(), ScheduleClient.NotificationType_View) != null)) {
										ScheduleClient.addEvent(
											docCreator,
											this.getMessageSource().getMessage("scheduler.message.view", new Object[] { doc.getTitle(), docCreator.getUserCode() }, Locale.ENGLISH),
											this.getConfiguration().getSchedulerEventTypeName(),
											requestDate,
											ScheduleClient.NotificationType_View,
											doc.getId(),
											this.userService);
									}
								}
							} else {
								LOG.debug("Document has no files!");
							}
						} else {
							LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);							
							AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
							aditCodedException.setParameters(new Object[] { request.getDocumentId(), userCode });
							throw aditCodedException;
						}
					} else {
						LOG.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
						AditCodedException aditCodedException = new AditCodedException("document.deflated");
						aditCodedException.setParameters(new Object[] { Util.dateToEstonianDateString(doc.getDeflateDate()) });
						throw aditCodedException;
					}
				} else {
					LOG.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
					AditCodedException aditCodedException = new AditCodedException("document.deleted");
					aditCodedException.setParameters(new Object[] { request.getDocumentId() });
					throw aditCodedException;
				}
			} else {
				LOG.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
				AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
				aditCodedException.setParameters(new Object[] { request.getDocumentId() });
				throw aditCodedException;
			}

			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.getDocument.success",	new Object[] {}, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			additionalInformationForLog = "Request failed: " + e.getMessage();
			LOG.error("Exception: ", e);
			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, e.getMessage());
			
			response.setSuccess(false);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

			if(e instanceof AditCodedException) {
				LOG.debug("Adding exception messages to response object.");
				arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
			}

			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}

		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		
		// Log document/file download
		if (includeFileContents) {
			if ((fileIdList == null) || (fileIdList.size() < 1)) {
				super.logDownloadRequest(documentId, null, requestDate.getTime());
			} else {
				for (Long fileId : fileIdList) {
					super.logDownloadRequest(documentId, fileId, requestDate.getTime());
				}
			}
		} else {
			super.logMetadataRequest(documentId, requestDate.getTime());
		}
		
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
		GetDocumentResponse response = new GetDocumentResponse();
		response.setSuccess(false);
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}
	
	private void checkHeader(CustomXTeeHeader header) throws Exception {
		if(header != null) {
			if ((header.getIsikukood() == null) || (header.getIsikukood().length() < 1)) {
				throw new AditCodedException("request.header.undefined.personalCode");
			} else if ((header.getInfosysteem() == null) || (header.getInfosysteem().length() < 1)) {
				throw new AditCodedException("request.header.undefined.systemName");
			} else if ((header.getAsutus() == null) || (header.getAsutus().length() < 1)) {
				throw new AditCodedException("request.header.undefined.institution");
			}
		}
	}

	private void checkRequest(GetDocumentRequest request) {
		if (request != null) {
			if (request.getDocumentId() <= 0) {
				throw new AditCodedException("request.body.undefined.documentId");
			}
		} else {
			throw new AditCodedException("request.body.empty");
		}
	}

	private static void printRequest(GetDocumentRequest request) {
		LOG.debug("-------- GetDocumentRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("Include file contents: " + String.valueOf(request.isIncludeFileContents()));
		LOG.debug("-----------------------------------");
	}
}
