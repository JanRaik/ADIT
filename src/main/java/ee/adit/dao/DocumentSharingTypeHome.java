package ee.adit.dao;

// Generated 21.05.2010 16:49:44 by Hibernate Tools 3.2.4.GA

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class DocumentSharingType.
 * @see ee.adit.dao.DocumentSharingType
 * @author Hibernate Tools
 */
public class DocumentSharingTypeHome {

	private static final Log log = LogFactory
			.getLog(DocumentSharingTypeHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(DocumentSharingType transientInstance) {
		log.debug("persisting DocumentSharingType instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(DocumentSharingType instance) {
		log.debug("attaching dirty DocumentSharingType instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(DocumentSharingType instance) {
		log.debug("attaching clean DocumentSharingType instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(DocumentSharingType persistentInstance) {
		log.debug("deleting DocumentSharingType instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public DocumentSharingType merge(DocumentSharingType detachedInstance) {
		log.debug("merging DocumentSharingType instance");
		try {
			DocumentSharingType result = (DocumentSharingType) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public DocumentSharingType findById(java.lang.String id) {
		log.debug("getting DocumentSharingType instance with id: " + id);
		try {
			DocumentSharingType instance = (DocumentSharingType) sessionFactory
					.getCurrentSession().get("ee.adit.dao.DocumentSharingType",
							id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(DocumentSharingType instance) {
		log.debug("finding DocumentSharingType instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria(
					"ee.adit.dao.DocumentSharingType").add(
					Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
