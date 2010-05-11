package ee.adit.dao;

// Generated 11.05.2010 17:19:11 by Hibernate Tools 3.3.0.GA

/**
 * AccessRestrictionsId generated by hbm2java
 */
public class AccessRestrictionsId implements java.io.Serializable {

	private String remoteApplicationShortName;
	private String userCode;

	public AccessRestrictionsId() {
	}

	public AccessRestrictionsId(String remoteApplicationShortName,
			String userCode) {
		this.remoteApplicationShortName = remoteApplicationShortName;
		this.userCode = userCode;
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

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof AccessRestrictionsId))
			return false;
		AccessRestrictionsId castOther = (AccessRestrictionsId) other;

		return ((this.getRemoteApplicationShortName() == castOther
				.getRemoteApplicationShortName()) || (this
				.getRemoteApplicationShortName() != null
				&& castOther.getRemoteApplicationShortName() != null && this
				.getRemoteApplicationShortName().equals(
						castOther.getRemoteApplicationShortName())))
				&& ((this.getUserCode() == castOther.getUserCode()) || (this
						.getUserCode() != null
						&& castOther.getUserCode() != null && this
						.getUserCode().equals(castOther.getUserCode())));
	}

	public int hashCode() {
		int result = 17;

		result = 37
				* result
				+ (getRemoteApplicationShortName() == null ? 0 : this
						.getRemoteApplicationShortName().hashCode());
		result = 37 * result
				+ (getUserCode() == null ? 0 : this.getUserCode().hashCode());
		return result;
	}

}
