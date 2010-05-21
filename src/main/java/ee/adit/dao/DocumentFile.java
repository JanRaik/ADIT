package ee.adit.dao;

// Generated 21.05.2010 16:49:43 by Hibernate Tools 3.2.4.GA

import java.math.BigDecimal;
import java.sql.Blob;

/**
 * DocumentFile generated by hbm2java
 */
public class DocumentFile implements java.io.Serializable {

	private long id;
	private Document document;
	private String fileName;
	private String contentType;
	private String description;
	private Blob fileData;
	private BigDecimal fileSizeBytes;

	public DocumentFile() {
	}

	public DocumentFile(long id, Document document, String fileName) {
		this.id = id;
		this.document = document;
		this.fileName = fileName;
	}

	public DocumentFile(long id, Document document, String fileName,
			String contentType, String description, Blob fileData,
			BigDecimal fileSizeBytes) {
		this.id = id;
		this.document = document;
		this.fileName = fileName;
		this.contentType = contentType;
		this.description = description;
		this.fileData = fileData;
		this.fileSizeBytes = fileSizeBytes;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Blob getFileData() {
		return this.fileData;
	}

	public void setFileData(Blob fileData) {
		this.fileData = fileData;
	}

	public BigDecimal getFileSizeBytes() {
		return this.fileSizeBytes;
	}

	public void setFileSizeBytes(BigDecimal fileSizeBytes) {
		this.fileSizeBytes = fileSizeBytes;
	}

}
