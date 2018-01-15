package com.sinosoft.util.hibernate.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.hibernate.entity.EntitySupport;
import com.sinosoft.util.hibernate.paging.HQLPagingQuery;
import com.sinosoft.util.hibernate.paging.PagingEntity;
import com.sinosoft.util.hibernate.paging.SQLPagingQuery;

/**
 * EntityDao的实现.因为使用的是spring+hibernate3,因此必须使用HibernateDaoSupport
 * 
 * @author LuoGang
 * 
 * @param <E>
 *            继承EntitySupport的实体类
 */
public class EntityDaoSupport<E extends EntitySupport<?>> extends HibernateDaoSupport implements EntityDao<E> {

	public Session getHibernateSession() {
		return super.getSession();
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

	public void delete(E entity) {
		this.getHibernateTemplate().delete(entity);
	}

	public E deleteById(Serializable id) {
		E entity = this.get(id);
		if (entity != null) {
			this.delete(entity);
		}
		return entity;
	}

	/**
	 * 批处理数量,用与删除多个数据时,指定每次操作的数量。在某些数据库中,允许的参数个数有限制
	 */
	public static final int BATCH_COUNT = 1000;

	public int deleteById(Serializable[] ids) {
		return this.deleteById(ids, null);
	}

	public int deleteById(Serializable[] ids, String hql) {
		int l = 0;
		if ((l = ids.length) <= BATCH_COUNT) {

			String queryString = "DELETE FROM " + entityClass.getName() + " WHERE id in (?"
					+ StringHelper.copy(", ?", ids.length - 1) + ")" + (StringHelper.isEmpty(hql) ? "" : " AND " + hql);
			return this.execute(queryString, ids);
		}
		int count = 0;
		int result = 0;
		Serializable[] newIds = null;
		while (l - count > 0) {
			newIds = new Serializable[Math.min(l - count, BATCH_COUNT)];
			System.arraycopy(ids, count, newIds, 0, 1000);
			result += this.deleteById(newIds);
			count += BATCH_COUNT;
		}
		return result;
	}

	public int execute(String hql) {
		return this.execute(hql, null);
	}

	public int execute(final String hql, final Object[] parameters) {
		return NumberHelper.intValue(this.getHibernateTemplate().executeWithNativeSession(
				new HibernateCallback<Integer>() {
					public Integer doInHibernate(Session session) throws HibernateException {
						Query query = session.createQuery(hql);

						if (parameters != null) {
							for (int i = 0; i < parameters.length; i++) {
								query.setParameter(i, parameters[i]);
							}
						}
						return Integer.valueOf(query.executeUpdate());
					}
				}));
	}

	public int executeSQL(String sql) {
		return this.execute(sql, null);
	}

	public int executeSQL(final String sql, final Object[] parameters) {
		return NumberHelper.intValue(this.getHibernateTemplate().executeWithNativeSession(
				new HibernateCallback<Integer>() {
					public Integer doInHibernate(Session session) throws HibernateException {
						SQLQuery query = session.createSQLQuery(sql);

						if (parameters != null) {
							for (int i = 0; i < parameters.length; i++) {
								query.setParameter(i, parameters[i]);
							}
						}
						return Integer.valueOf(query.executeUpdate());
					}
				}));
	}

	public boolean exists(E entity) {
		return this.getHibernateTemplate().contains(entity);
	}

	public boolean existsById(Serializable id) {
		try {
			return this.get(id) != null;
		} catch (Exception e) {
			return false;
		}
	}

	public E get(Serializable id) {
		return (E) this.getHibernateTemplate().get(this.getEntityClass(), id);
	}

	/**
	 * 通过属性查询唯一的数据
	 * 
	 * @param property
	 *            属性名
	 * @param value
	 *            值
	 * @return E 得到的实体
	 */
	@SuppressWarnings("unchecked")
	public E getByProperty(String property, Object value) {
		String hql = "FROM " + this.getEntityClass().getName() + " WHERE " + property + " = ?";
		try {
			return (E) this.uniqueResult(hql, new Object[] { value });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 通过属性查询数据
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<E> queryByProperty(String property, Object value) {
		String hql = "FROM " + this.getEntityClass().getName() + " WHERE " + property + " = ?";
		return (List<E>) this.query(hql, new Object[] { value });
	}

	@SuppressWarnings("unchecked")
	public List<E> queryByProperty(Map<String, Object> properties) {
		StringBuilder hql = new StringBuilder("FROM ");
		hql.append(getEntityClass().getName());
		List<Object> parameters = null;
		if (properties != null && !properties.isEmpty()) {
			parameters = new ArrayList<Object>();
			StringBuilder temp = new StringBuilder();
			// hql.append(" WHERE ");
			// + property + " = ?";
			String orderBy = "";
			// size = 0;
			Object value;
			for (String property : properties.keySet()) {
				if (property.equalsIgnoreCase("ORDER BY")) {
					orderBy = " ORDER BY " + properties.get(property);
					continue;
				}
				value = properties.get(property);
				if (temp.length() > 0) temp.append(" AND ");
				if (value == null) {
					temp.append(property).append(" IS NULL ");
				} else {
					temp.append(property).append(" = ? ");
					parameters.add(value);
				}
			}
			if (temp.length() > 0) {
				hql.append(" WHERE ").append(temp);
			}
			hql.append(orderBy);
		}
		return (List<E>) query(hql.toString(), parameters == null ? null : parameters.toArray());
	}

	@SuppressWarnings("unchecked")
	public List<E> queryByProperty(Object[][] properties) {
		StringBuilder hql = new StringBuilder("FROM ");
		hql.append(getEntityClass().getName());
		List<Object> parameters = null;
		if (properties != null && properties.length > 0) {
			parameters = new ArrayList<Object>();
			StringBuilder temp = new StringBuilder();
			// hql.append(" WHERE ");
			// + property + " = ?";
			String orderBy = "";
			String property;
			for (Object[] propertyRow : properties) {
				property = propertyRow[0].toString().trim();
				if (property.equalsIgnoreCase("ORDER BY")) {
					orderBy = " ORDER BY " + propertyRow[1];
					continue;
				}
				if (temp.length() > 0) temp.append(" AND ");
				if (propertyRow[1] == null) {
					temp.append(property).append(" IS NULL ");
				} else {
					temp.append(property).append(" = ? ");
					parameters.add(propertyRow[1]);
				}
			}
			if (temp.length() > 0) {
				hql.append(" WHERE ").append(temp);
			}
			hql.append(orderBy);
		}
		return (List<E>) query(hql.toString(), parameters == null ? null : parameters.toArray());
	}

	public List<?> query(String hql) {
		return this.query(hql, null);
	}

	public List<?> query(final String hql, final Object[] parameters) {
		return this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<?>>() {
			public List<?> doInHibernate(Session session) throws HibernateException {
				Query query = session.createQuery(hql);

				if (parameters != null) {
					for (int i = 0; i < parameters.length; i++) {
						query.setParameter(i, parameters[i]);
					}
				}
				return query.list();
			}
		});
	}

	public Object uniqueResult(String hql) {
		return this.uniqueResult(hql, null);
	}

	public Object uniqueResult(final String hql, final Object[] parameters) {
		return this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Object>() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query query = session.createQuery(hql);

				if (parameters != null) {
					for (int i = 0; i < parameters.length; i++) {
						query.setParameter(i, parameters[i]);
					}
				}
				return query.uniqueResult();
			}
		});
	}

	public List<?> querySQL(String sql) {
		return this.query(sql, null);
	}

	public List<?> querySQL(final String sql, final Object[] parameters) {
		return this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<?>>() {
			public List<?> doInHibernate(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sql);
				if (parameters != null) {
					for (int i = 0; i < parameters.length; i++) {
						query.setParameter(i, parameters[i]);
					}
				}
				return query.list();
			}
		});
	}

	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @param clses
	 * @return
	 */
	public List<?> querySQL(final String sql, final Object[] parameters, final Class<?>... clses) {
		return this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<?>>() {
			public List<?> doInHibernate(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sql);
				for (Class<?> cls : clses) {
					query.addEntity(cls);
				}
				if (parameters != null) {
					for (int i = 0; i < parameters.length; i++) {
						query.setParameter(i, parameters[i]);
					}
				}
				return query.list();
			}
		});
	}

	public Object uniqueResultSQL(String sql) {
		return this.uniqueResultSQL(sql, null);
	}

	public Object uniqueResultSQL(final String sql, final Object[] parameters) {
		return this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Object>() {
			public Object doInHibernate(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sql);

				if (parameters != null) {
					for (int i = 0; i < parameters.length; i++) {
						query.setParameter(i, parameters[i]);
					}
				}
				return query.uniqueResult();
			}
		});

	}

	public Serializable save(E entity) {
		return this.getHibernateTemplate().save(entity);
	}

	public void saveOrUpdate(E entity) {
		// this.getHibernateTemplate().saveOrUpdate(entity);
		if (this.exists(entity)) this.update(entity);
		else this.save(entity);

	}

	public void update(E entity) {
		this.getHibernateTemplate().update(entity);
	}

	public PagingEntity<?> query(String hql, Object[] parameters, int pageIndex, int maxResults) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HQLPagingQuery<?> query = new HQLPagingQuery(this);
		return query.query(hql, parameters, pageIndex, maxResults);
	}

	public PagingEntity<?> querySQL(String sql, Object[] parameters, int pageIndex, int maxResults) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		SQLPagingQuery<?> query = new SQLPagingQuery(this);
		return query.query(sql, parameters, pageIndex, maxResults);
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
	 * 手动提交hibernate事务
	 */
	public void commit() {
		getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Object>() {
			public Object doInHibernate(Session session) throws HibernateException {
				// System.out.println("commit:" + session);
				session.getTransaction().commit();
				return null;
			}
		});
	}

	/**
	 * 手动启动事务
	 * 
	 * @return
	 */
	public Transaction beginTransaction() {
		return getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Transaction>() {
			public Transaction doInHibernate(Session session) throws HibernateException {
				// System.out.println("beginTransaction:" + session);
				return session.beginTransaction();
			}
		});
	}
}
