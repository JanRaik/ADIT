package ee.adit.service;

import java.util.Date;

import org.apache.log4j.Logger;

import ee.adit.dao.ErrorLogDAO;
import ee.adit.dao.NotificationDAO;
import ee.adit.dao.RequestLogDAO;
import ee.adit.dao.pojo.ErrorLog;
import ee.adit.dao.pojo.Notification;
import ee.adit.dao.pojo.RequestLog;

public class LogService {
	
	private static Logger LOG = Logger.getLogger(LogService.class);
	
	private RequestLogDAO requestLogDAO;

	private ErrorLogDAO errorLogDAO;
	
	public RequestLogDAO getRequestLogDAO() {
		return requestLogDAO;
	}

	public void setRequestLogDAO(RequestLogDAO requestLogDAO) {
		this.requestLogDAO = requestLogDAO;
	}
	
	public Long addRequestLogEntry(RequestLog requestLogEntry) {
		return this.requestLogDAO.save(requestLogEntry);
	}
	
	public Long addRequestLogEntry(
			String requestName,
			Long documentId,
			Date requestDate,
			String remoteApplicationShortName,
			String userCode,
			String organizationCode,
			String additionalInformation) {
		
		RequestLog logEntry = new RequestLog();
		logEntry.setAdditionalInformation(additionalInformation);
		logEntry.setDocumentId(documentId);
		logEntry.setOrganizationCode(organizationCode);
		logEntry.setRemoteApplicationShortName(remoteApplicationShortName);
		logEntry.setRequest(requestName);
		logEntry.setRequestDate(requestDate);
		logEntry.setUserCode(userCode);
		
		return this.requestLogDAO.save(logEntry);
	}
	
	public Long addErrorLogEntry(
			String actionName,
			Long documentId,
			Date errorDate,
			String remoteApplicationShortName,
			String userCode,
			String errorLevel,
			String errorMessage) {
		
		ErrorLog logEntry = new ErrorLog();
		logEntry.setActionName(actionName);
		logEntry.setDocumentId(documentId);
		logEntry.setErrorDate(errorDate);
		logEntry.setErrorLevel(errorLevel);
		logEntry.setErrorMessage(errorMessage);
		logEntry.setRemoteApplicationShortName(remoteApplicationShortName);
		logEntry.setUserCode(userCode);
		
		return this.errorLogDAO.save(logEntry);
	}

	public ErrorLogDAO getErrorLogDAO() {
		return errorLogDAO;
	}

	public void setErrorLogDAO(ErrorLogDAO errorLogDAO) {
		this.errorLogDAO = errorLogDAO;
	}
		
	
}
