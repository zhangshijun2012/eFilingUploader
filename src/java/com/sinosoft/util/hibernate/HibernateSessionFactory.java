package com.sinosoft.util.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Configures and provides access to Hibernate sessions, tied to the current thread of execution. Follows the Thread
 * Local Session pattern, see {@link http://hibernate.org/42.html }.
 */
@SuppressWarnings({ "unchecked" })
public class HibernateSessionFactory {

	/**
	 * Location of hibernate.cfg.xml file. Location should be on the classpath as Hibernate uses #resourceAsStream style
	 * lookup for its configuration file. The default classpath location of the hibernate config file is in the default
	 * package. Use #setConfigFile() to update the location of the configuration file for the current session.
	 */
	private static String CONFIG_FILE_LOCATION = "/hibernate.cfg.xml";

	@SuppressWarnings("rawtypes")
	private static final ThreadLocal threadLocal = new ThreadLocal();

	private static Configuration configuration = new Configuration();

	private static SessionFactory sessionFactory;

	private static String configFile = CONFIG_FILE_LOCATION;

	private HibernateSessionFactory() {
	}

	/**
	 * Returns the ThreadLocal Session instance. Lazy initialize the <code>SessionFactory</code> if needed.
	 * 
	 * @return Session
	 * @throws HibernateException
	 */
	public static synchronized Session getSession() throws HibernateException {
		Session session = (Session) threadLocal.get();

		if (session == null || !session.isOpen()) {
			if (sessionFactory == null) {
				rebuildSessionFactory();
			}
			session = (sessionFactory != null) ? sessionFactory.getCurrentSession() : null;
			threadLocal.set(session);
		}

		return session;
	}

	/**
	 * Rebuild hibernate session factory
	 * 
	 */
	public static synchronized void rebuildSessionFactory() {
		try {
			configuration.configure(configFile);
			// final ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(
			// configuration.getProperties()).buildServiceRegistry();
			sessionFactory = configuration.buildSessionFactory();
		} catch (Exception e) {
			System.err.println("%%%% Error Creating SessionFactory %%%%");
			e.printStackTrace();
		}
	}

	/**
	 * Close the single hibernate session instance.
	 * 
	 * @throws HibernateException
	 */
	public static synchronized void closeSession() throws HibernateException {
		Session session = (Session) threadLocal.get();
		threadLocal.set(null);
		if (session != null) {
			session.close();
		}

	}

	/**
	 * return session factory
	 * 
	 */
	public static synchronized SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * return session factory
	 * 
	 * session factory will be rebuilded in the next call
	 */
	public static synchronized void setConfigFile(String configFile) {
		HibernateSessionFactory.configFile = configFile;
		sessionFactory = null;
	}

	/**
	 * return hibernate configuration
	 * 
	 */
	public static synchronized Configuration getConfiguration() {
		return configuration;
	}

}