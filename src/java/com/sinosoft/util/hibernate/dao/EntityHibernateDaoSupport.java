package com.sinosoft.util.hibernate.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.sinosoft.util.StringHelper;
import com.sinosoft.util.hibernate.entity.EntitySupport;
import com.sinosoft.util.hibernate.paging.PagingEntity;

public class EntityHibernateDaoSupport<E extends EntitySupport<?>> implements EntityDao<E> {
	/**
	 * 无参的构造函数,在创建时初始化泛型参数的类型
	 */
	public EntityHibernateDaoSupport() {
		// 创建时初始化泛型参数类型
		entityClass = getEntityClass();
	}

	/**
	 * 实体类的类型.如果子类在继承此类时明确指定了E,且未覆盖getEntityClass方法,则不需要指定此属性的值.<br />
	 * 否则如果子类中未指定继承的E的明确类型,则需要指定此属性的值或覆盖getEntityClass方法
	 */
	protected Class<E> entityClass;

	/**
	 * 得到实体类型E的Class对象<br />
	 * 注意此方法只有在子类中调用才不会出错.且子类中需要指定E的类型.<br />
	 * 如果子类使用泛型则不需要覆盖此方法，否则需要指定entityClass的值，或覆盖此方法
	 * 
	 * @return E的class，相当与E.class
	 */
	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass() {
		if (entityClass == null) {
			// 得到此类的父类的泛型参数.
			// 因为只能在子类中调用，则getGenericSuperclass即是EntityBaseDao类.
			// 取得本类的泛型参数的类型
			entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		return entityClass;
	}

	public String getEntityClassName() {
		return getEntityClass().getName();
	}

	SessionFactory sessionFactory;

	public Session getHibernateSession() {
		return sessionFactory.getCurrentSession();
	}

	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	public void delete(E entity) {
		this.getSession().delete(entity);
	}

	public E deleteById(Serializable id) {
		E entity = get(id);
		if (entity != null) {
			delete(entity);
		}
		return entity;
	}

	/**
	 * 批处理数量,用与删除多个数据时,指定每次操作的数量。在某些数据库中,允许的参数个数有限制
	 */
	private static final int BATCH_COUNT = 1000;

	public int deleteById(Serializable[] ids) {
		int l = 0;
		if ((l = ids.length) <= BATCH_COUNT) {
			String hql = "DELETE FROM " + getEntityClass().getName() + " WHERE id in (?"
					+ StringHelper.copy(", ?", ids.length - 1) + ")";
			return execute(hql, ids);
		}
		int count = 0;
		int result = 0;
		Serializable[] newIds = null;
		while (l - count > 0) {
			newIds = new Serializable[Math.min(l - count, BATCH_COUNT)];
			System.arraycopy(ids, count, newIds, 0, 1000);
			result += deleteById(newIds);
			count += BATCH_COUNT;
		}
		return result;
	}

	public int execute(String hql) {
		return execute(hql, null);
	}

	public int execute(String hql, Object[] parameters) {
		Query query = getSession().createQuery(hql);
		if (parameters != null) {
			int l = parameters.length;
			for (int i = 0; i < l; i++) {
				query.setParameter(i, parameters[i]);
			}
		}
		return query.executeUpdate();
	}

	public int executeSQL(String sql) {
		return executeSQL(sql, null);
	}

	public int executeSQL(String sql, Object[] parameters) {
		SQLQuery query = getSession().createSQLQuery(sql);
		if (parameters != null) {
			int l = parameters.length;
			for (int i = 0; i < l; i++) {
				query.setParameter(i, parameters[i]);
			}
		}
		return query.executeUpdate();
	}

	public boolean exists(E entity) {
		return getSession().contains(entity);
	}

	public boolean existsById(Serializable id) {
		if (id == null) return false;
		return get(id) != null;
	}

	@SuppressWarnings("unchecked")
	public E get(Serializable id) {
		return (E) getSession().get(getEntityClass(), id);
	}

	/**
	 * 通过属性查询唯一的数据
	 * 
	 * @param propertyName 属性名
	 * @param value 值
	 * @return E 得到的实体
	 */
	@SuppressWarnings("unchecked")
	public E getByProperty(String propertyName, Object value) {
		String hql = "FROM " + getEntityClass().getName() + " WHERE " + propertyName + " = ?";
		return (E) uniqueResult(hql, new Object[] { value });
	}

	/**
	 * 通过属性查询数据
	 * 
	 * @param propertyName
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<E> queryByProperty(String propertyName, Object value) {
		String hql = "FROM " + getEntityClass().getName() + " WHERE " + propertyName + " = ?";
		return (List<E>) query(hql, new Object[] { value });
	}

	@SuppressWarnings("unchecked")
	public List<E> queryByProperty(Map<String, Object> properties) {
		StringBuffer hql = new StringBuffer("FROM ");
		hql.append(getEntityClass().getName());
		Object[] parameters = null;
		if (properties != null && !properties.isEmpty()) {
			int size = properties.size();
			parameters = new Object[size];
			hql.append(" WHERE ");
			// + property + " = ?";
			String orderBy = "";
			size = 0;
			for (String property : properties.keySet()) {
				if (property.equalsIgnoreCase("ORDER BY")) {
					orderBy = " ORDER BY " + properties.get(property);
					continue;
				}
				if (size > 0) {
					hql.append(" AND ");
				}
				hql.append(property).append(" = ? ");
				parameters[size++] = properties.get(property);
			}
			hql.append(orderBy);
		}
		return (List<E>) query(hql.toString(), parameters);
	}

	@SuppressWarnings("unchecked")
	public List<E> queryByProperty(Object[][] properties) {
		StringBuffer hql = new StringBuffer("FROM ");
		hql.append(getEntityClass().getName());
		Object[] parameters = null;
		if (properties != null && properties.length > 0) {
			int size = properties.length;
			parameters = new Object[size];
			StringBuffer temp = new StringBuffer();
			// hql.append(" WHERE ");
			// + property + " = ?";
			String orderBy = "";
			size = 0;
			String property;
			for (Object[] propertyRow : properties) {
				property = propertyRow[0].toString().trim();
				if (property.equalsIgnoreCase("ORDER BY")) {
					orderBy = " ORDER BY " + propertyRow[1];
					continue;
				}
				if (temp.length() > 0) {
					temp.append(" AND ");
				}
				if (propertyRow[1] == null) {
					temp.append(property).append(" IS NULL ");
				} else {
					temp.append(property).append(" = ? ");
					parameters[size++] = propertyRow[1];
				}
			}
			if (temp.length() > 0) {
				hql.append(" WHERE ").append(temp);
			}
			hql.append(orderBy);
		}
		return (List<E>) query(hql.toString(), parameters);
	}

	public List<?> query(String hql) {
		return query(hql, null);
	}

	public List<?> query(String hql, Object[] parameters) {
		Query query = getSession().createQuery(hql);
		if (parameters != null) {
			int l = parameters.length;
			for (int i = 0; i < l; i++) {
				query.setParameter(i, parameters[i]);
			}
		}
		return query.list();
	}

	public PagingEntity<?> query(String hql, Object[] parameters, int pageIndex, int maxResults) {
		// @SuppressWarnings({ "unchecked", "rawtypes" })
		// HQLPagingQuery<?> query = new HQLPagingQuery(this);
		// return query.query(hql, parameters, pageIndex, maxResults);
		return null;
	}

	public List<?> querySQL(String sql) {
		return this.querySQL(sql, null);
	}

	public List<?> querySQL(String sql, Object[] parameters) {
		SQLQuery query = getSession().createSQLQuery(sql);
		if (parameters != null) {
			int l = parameters.length;
			for (int i = 0; i < l; i++) {
				query.setParameter(i, parameters[i]);
			}
		}
		// List results = query.list();
		return query.list();
	}

	public List<?> querySQL(String sql, Object[] parameters, Class<?>... clses) {
		SQLQuery query = getSession().createSQLQuery(sql);
		for (Class<?> cls : clses) {
			query.addEntity(cls);
		}
		if (parameters != null) {
			int l = parameters.length;
			for (int i = 0; i < l; i++) {
				query.setParameter(i, parameters[i]);
			}
		}
		// List results = query.list();
		return query.list();
	}

	public PagingEntity<?> querySQL(String sql, Object[] parameters, int pageIndex, int maxResults) {
		// @SuppressWarnings({ "unchecked", "rawtypes" })
		// SQLPagingQuery<?> query = new SQLPagingQuery(this);
		// return query.query(sql, parameters, pageIndex, maxResults);
		return null;
	}

	public Object uniqueResult(String hql) {
		return this.uniqueResult(hql, null);
	}

	public Object uniqueResult(String hql, Object[] parameters) {
		Query query = getSession().createQuery(hql);
		if (parameters != null) {
			int l = parameters.length;
			for (int i = 0; i < l; i++) {
				query.setParameter(i, parameters[i]);
			}
		}
		return query.uniqueResult();
	}

	public Object uniqueResultSQL(String sql) {
		return this.uniqueResultSQL(sql, null);
	}

	public Object uniqueResultSQL(String sql, Object[] parameters) {
		SQLQuery query = getSession().createSQLQuery(sql);
		if (parameters != null) {
			int l = parameters.length;
			for (int i = 0; i < l; i++) {
				query.setParameter(i, parameters[i]);
			}
		}
		// List results = query.list();
		return query.uniqueResult();
	}

	public Serializable save(E entity) {
		return this.getSession().save(entity);
	}

	public void saveOrUpdate(E entity) {
		this.getSession().saveOrUpdate(entity);
	}

	public void update(E entity) {
		this.getSession().update(entity);
	}

	/**
	 * 回滚事务,Spring托管的事务回滚TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	 */
	public void rollback() {

		// hibernate session事务回滚
		getHibernateSession().getTransaction().rollback();
	}

	/**
	 * 手动提交hibernate事务
	 */
	public void commit() {
		getHibernateSession().flush();
		getHibernateSession().getTransaction().commit();
	}

	/**
	 * 手动启动事务
	 * 
	 * @return
	 */
	public Transaction beginTransaction() {
		return getHibernateSession().beginTransaction();
	}

	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
