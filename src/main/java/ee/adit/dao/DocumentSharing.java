package ee.adit.dao;

// Generated 21.05.2010 16:49:43 by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * DocumentSharing generated by hbm2java
 */
public class DocumentSharing implements java.io.Serializable {

	private long id;
	private DocumentSharingType documentSharingType;
	private DocumentDvkStatus documentDvkStatus;
	private DocumentWfStatus documentWfStatus;
	private Document document;
	private String userCode;
	private String taskDescription;
	private Date creationDate;
	private Long dvkId;
	private Date lastAccessDate;

	public DocumentSharing() {
	}

	public DocumentSharing(long id, DocumentSharingType documentSharingType,
			Document document, String userCode) {
		this.id = id;
		this.documentSharingType = documentSharingType;
		this.document = document;
		this.userCode = userCode;
	}

	public DocumentSharing(long id, DocumentSharingType documentSharingType,
			DocumentDvkStatus documentDvkStatus,
			DocumentWfStatus documentWfStatus, Document document,
			String userCode, String taskDescription, Date creationDate,
			Long dvkId, Date lastAccessDate) {
		this.id = id;
		this.documentSharingType = documentSharingType;
		this.documentDvkStatus = documentDvkStatus;
		this.documentWfStatus = documentWfStatus;
		this.document = document;
		this.userCode = userCode;
		this.taskDescription = taskDescription;
		this.creationDate = creationDate;
		this.dvkId = dvkId;
		this.lastAccessDate = lastAccessDate;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public DocumentSharingType getDocumentSharingType() {
		return this.documentSharingType;
	}

	public void setDocumentSharingType(DocumentSharingType documentSharingType) {
		this.documentSharingType = documentSharingType;
	}

	public DocumentDvkStatus getDocumentDvkStatus() {
		return this.documentDvkStatus;
	}

	public void setDocumentDvkStatus(DocumentDvkStatus documentDvkStatus) {
		this.documentDvkStatus = documentDvkStatus;
	}

	public DocumentWfStatus getDocumentWfStatus() {
		return this.documentWfStatus;
	}

	public void setDocumentWfStatus(DocumentWfStatus documentWfStatus) {
		this.documentWfStatus = documentWfStatus;
	}

	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getUserCode() {
		return this.userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getTaskDescription() {
		return this.taskDescription;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getDvkId() {
		return this.dvkId;
	}

	public void setDvkId(Long dvkId) {
		this.dvkId = dvkId;
	}

	public Date getLastAccessDate() {
		return this.lastAccessDate;
	}

	public void setLastAccessDate(Date lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}

}
