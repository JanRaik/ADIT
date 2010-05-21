package ee.adit.dao;

// Generated 21.05.2010 16:49:43 by Hibernate Tools 3.2.4.GA

import java.util.HashSet;
import java.util.Set;

/**
 * RemoteApplication generated by hbm2java
 */
public class RemoteApplication implements java.io.Serializable {

	private String shortName;
	private String name;
	private String organizationCode;
	private Boolean canRead;
	private Boolean canWrite;
	private Set documents = new HashSet(0);
	private Set accessRestrictions = new HashSet(0);
	private Set documentHistories = new HashSet(0);

	public RemoteApplication() {
	}

	public RemoteApplication(String shortName, String organizationCode) {
		this.shortName = shortName;
		this.organizationCode = organizationCode;
	}

	public RemoteApplication(String shortName, String name,
			String organizationCode, Boolean canRead, Boolean canWrite,
			Set documents, Set accessRestrictions, Set documentHistories) {
		this.shortName = shortName;
		this.name = name;
		this.organizationCode = organizationCode;
		this.canRead = canRead;
		this.canWrite = canWrite;
		this.documents = documents;
		this.accessRestrictions = accessRestrictions;
		this.documentHistories = documentHistories;
	}

	public String getShortName() {
		return this.shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrganizationCode() {
		return this.organizationCode;
	}

	public void setOrganizationCode(String organizationCode) {
		this.organizationCode = organizationCode;
	}

	public Boolean getCanRead() {
		return this.canRead;
	}

	public void setCanRead(Boolean canRead) {
		this.canRead = canRead;
	}

	public Boolean getCanWrite() {
		return this.canWrite;
	}

	public void setCanWrite(Boolean canWrite) {
		this.canWrite = canWrite;
	}

	public Set getDocuments() {
		return this.documents;
	}

	public void setDocuments(Set documents) {
		this.documents = documents;
	}

	public Set getAccessRestrictions() {
		return this.accessRestrictions;
	}

	public void setAccessRestrictions(Set accessRestrictions) {
		this.accessRestrictions = accessRestrictions;
	}

	public Set getDocumentHistories() {
		return this.documentHistories;
	}

	public void setDocumentHistories(Set documentHistories) {
		this.documentHistories = documentHistories;
	}

}
