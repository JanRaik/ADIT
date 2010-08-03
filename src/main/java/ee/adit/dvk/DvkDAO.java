package ee.adit.dvk;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import dvk.api.DVKAPI;
import dvk.api.ml.PojoMessage;
import dvk.api.ml.PojoMessageRecipient;
import dvk.api.ml.PojoSettings;

public class DvkDAO {

	private SessionFactory sessionFactory;

	public DvkDAO() {
		this.setSessionFactory(DVKAPI.createSessionFactory("hibernate_ora_dvk.cfg.xml"));
	}

	public List<PojoMessage> getIncomingDocuments() {
		List<PojoMessage> result = new ArrayList<PojoMessage>();

		final String SQL = "from PojoMessage where isIncoming = true and (recipientStatusId = null or recipientStatusId = 0 or recipientStatusId = 101 or recipientStatusId = 1)";
		result = this.getSessionFactory().openSession().createQuery(SQL).list();

		return result;
	}

	public List<PojoMessage> getIncomingDocumentsWithoutStatus(Long statusID) {
		List<PojoMessage> result = new ArrayList<PojoMessage>();

		final String SQL = "from PojoMessage where isIncoming = true and (recipientStatusId != " + statusID + " or recipientStatusId is null)";
		result = this.getSessionFactory().openSession().createQuery(SQL).list();

		return result;
	}
	
	public void updateDocument(PojoMessage document) throws Exception {
		
		Session session = this.getSessionFactory().openSession();
		Transaction transaction = null;
		
		try {
			transaction = session.beginTransaction();
		    session.saveOrUpdate(document);
		    transaction.commit();
		}
		catch (Exception e) {
		    if (transaction != null) transaction.rollback();
		    throw e;
		}
		finally {
			session.close();
		}
		
	}

	public List<PojoMessageRecipient> getMessageRecipients(Long dvkMessageID, boolean incoming) {
		// TODO: pojomesage.ID - invalid identifier
		int incomingInt = 0;
		if(incoming) {
			incomingInt = 1;
		}
		
		String SQL = "select mr from PojoMessageRecipient mr, PojoMessage m where mr.dhlMessageId = m.dhlMessageId and m.dhlMessageId = " + dvkMessageID + " and m.isIncoming = " + incomingInt;
		return this.getSessionFactory().openSession().createQuery(SQL).list();
	}
	
	public PojoSettings getDVKSettings() {
		Session session = this.getSessionFactory().openSession();
		String SQL = "from PojoSettings where id = (select max(id) from PojoSettings)";
		try {
		    return (PojoSettings) session.createQuery(SQL).uniqueResult();
		} finally {
			session.close();
		}
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
