package ee.adit.dao;

// Generated 11.05.2010 17:19:11 by Hibernate Tools 3.3.0.GA

/**
 * AccessRestrictions generated by hbm2java
 */
public class AccessRestrictions implements java.io.Serializable {

	private AccessRestrictionsId id;
	private Users users;
	private RemoteApplications remoteApplications;
	private boolean canRead;

	public AccessRestrictions() {
	}

	public AccessRestrictions(AccessRestrictionsId id, Users users,
			RemoteApplications remoteApplications, boolean canRead) {
		this.id = id;
		this.users = users;
		this.remoteApplications = remoteApplications;
		this.canRead = canRead;
	}

	public AccessRestrictionsId getId() {
		return this.id;
	}

	public void setId(AccessRestrictionsId id) {
		this.id = id;
	}

	public Users getUsers() {
		return this.users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public RemoteApplications getRemoteApplications() {
		return this.remoteApplications;
	}

	public void setRemoteApplications(RemoteApplications remoteApplications) {
		this.remoteApplications = remoteApplications;
	}

	public boolean isCanRead() {
		return this.canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

}
