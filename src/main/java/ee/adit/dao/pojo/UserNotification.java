package ee.adit.dao.pojo;

// Generated 21.06.2010 12:03:01 by Hibernate Tools 3.2.4.GA

/**
 * UserNotification generated by hbm2java
 */
public class UserNotification implements java.io.Serializable {

	private UserNotificationId id;
	private NotificationType notificationType;

	public UserNotification() {
	}

	public UserNotification(UserNotificationId id,
			NotificationType notificationType) {
		this.id = id;
		this.notificationType = notificationType;
	}

	public UserNotificationId getId() {
		return this.id;
	}

	public void setId(UserNotificationId id) {
		this.id = id;
	}

	public NotificationType getNotificationType() {
		return this.notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

}
