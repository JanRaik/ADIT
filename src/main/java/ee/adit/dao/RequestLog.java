package ee.adit.dao;

// Generated 21.05.2010 16:49:43 by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * RequestLog generated by hbm2java
 */
public class RequestLog implements java.io.Serializable {

	private long id;
	private String request;
	private Long documentId;
	private Date requestDate;
	private String remoteApplicationShortName;
	private String userCode;
	private String organizationCode;
	private String additionalInformation;

	public RequestLog() {
	}

	public RequestLog(long id) {
		this.id = id;
	}

	public RequestLog(long id, String request, Long documentId,
			Date requestDate, String remoteApplicationShortName,
			String userCode, String organizationCode,
			String additionalInformation) {
		this.id = id;
		this.request = request;
		this.documentId = documentId;
		this.requestDate = requestDate;
		this.remoteApplicationShortName = remoteApplicationShortName;
		this.userCode = userCode;
		this.organizationCode = organizationCode;
		this.additionalInformation = additionalInformation;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRequest() {
		return this.request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public Long getDocumentId() {
		return this.documentId;
	}

	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}

	public Date getRequestDate() {
		return this.requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public String getRemoteApplicationShortName() {
		return this.remoteApplicationShortName;
	}

	public void setRemoteApplicationShortName(String remoteApplicationShortName) {
		this.remoteApplicationShortName = remoteApplicationShortName;
	}

	public String getUserCode() {
		return this.userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getOrganizationCode() {
		return this.organizationCode;
	}

	public void setOrganizationCode(String organizationCode) {
		this.organizationCode = organizationCode;
	}

	public String getAdditionalInformation() {
		return this.additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

}
