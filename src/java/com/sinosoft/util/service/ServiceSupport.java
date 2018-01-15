package com.sinosoft.util.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Transaction;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.sinosoft.efiling.hibernate.dao.CompanyDao;
import com.sinosoft.efiling.hibernate.dao.UserDao;
import com.sinosoft.util.LoggableImpl;
import com.sinosoft.util.hibernate.dao.EntityDao;
import com.sinosoft.util.hibernate.entity.Entity;
import com.sinosoft.util.hibernate.paging.PagingEntity;

public class ServiceSupport<E extends Entity<?>, D extends EntityDao<E>> extends LoggableImpl implements Service<E, D> {

	protected D dao;
	protected UserDao userDao;
	protected CompanyDao companyDao;

	public D getDao() {
		return dao;
	}

	public void setDao(D dao) {
		this.dao = dao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public CompanyDao getCompanyDao() {
		return companyDao;
	}

	public void setCompanyDao(CompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public E get(Serializable id) {
		return dao.get(id);
	}

	public E save(E entity) {
		dao.save(entity);
		return entity;
	}

	public List<E> save(List<E> list) {
		for (E entity : list) {
			dao.save(entity);
		}
		return list;
	}

	public E update(E entity) {
		dao.update(entity);
		return entity;
	}

	public E saveOrUpdate(E entity) {
		if (dao.exists(entity) || dao.existsById(entity.getId())) {
			dao.update(entity);
		} else {
			dao.save(entity);
		}
		return entity;
	}

	public E delete(E entity) {
		dao.delete(entity);
		return entity;
	}

	public E deleteById(Serializable id) {
		return dao.deleteById(id);
	}

	public int deleteById(Serializable[] ids) {
		return dao.deleteById(ids);
	}

	public PagingEntity<?> query(String hql, Object[] parameters, int pageIndex, int maxResults) {
		return dao.query(hql, parameters, pageIndex, maxResults);
	}

	public PagingEntity<?> querySQL(String sql, Object[] parameters, int pageIndex, int maxResults) {
		return dao.querySQL(sql, parameters, pageIndex, maxResults);
	}

	public List<?> query(String hql) {
		return dao.query(hql);
	}

	public List<?> query(String hql, Object[] parameters) {
		return dao.query(hql, parameters);
	}

	public List<?> querySQL(String sql) {
		return dao.querySQL(sql);
	}

	public List<?> querySQL(String sql, Object[] parameters) {
		return dao.querySQL(sql, parameters);
	}

	/**
	 * 回滚事务,Spring托管的事务回滚TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	 */
	public void rollback() {
		// Spring事务回滚
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

		// hibernate session事务回滚
		// dao.getHibernateSession().getTransaction().rollback();
	}

	/**
	 * 提交事务
	 */
	public void commit() {
		dao.commit();
	}

	public Transaction beginTransaction() {
		return dao.beginTransaction();
	}
}
