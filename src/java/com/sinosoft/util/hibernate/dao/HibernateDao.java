package com.sinosoft.util.hibernate.dao;

import org.hibernate.Session;

/**
 * 
 * @author LuoGang
 * 
 */
public interface HibernateDao {

	/**
	 * 得到Hibernate的session对象
	 * 
	 * @return
	 */
	public Session getHibernateSession();
}