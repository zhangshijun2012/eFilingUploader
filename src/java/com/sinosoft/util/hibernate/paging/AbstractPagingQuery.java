package com.sinosoft.util.hibernate.paging;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.sinosoft.efiling.util.SystemUtils;
import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.hibernate.dao.EntityDaoSupport;

public abstract class AbstractPagingQuery<E> implements PagingQuery<E> {

	/** 数据库操作DAO */
	EntityDaoSupport<?> dao;
	/** 是否使用SQL语句进行查询 */
	boolean useSQL;

	protected AbstractPagingQuery() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 默认构造函数
	 * 
	 * @param dao 数据库操作DAO
	 * @param useSQL 是否使用SQL查询
	 */
	public AbstractPagingQuery(EntityDaoSupport<?> dao, boolean useSQL) {
		super();
		this.dao = dao;
		this.useSQL = useSQL;
	}

	@SuppressWarnings("unchecked")
	public PagingEntity<E> query(final String queryString, final Object[] parameters, final int index, final int max) {
		final int pageIndex = index == 0 ? 1 : index;
		final int maxResults = max == 0 ? SystemUtils.DEFAULT_MAX_RESULTS : max;

		/** 查询结果 */
		PagingEntity<E> pagingEntity = new PagingEntity<E>();
		pagingEntity.setPageIndex(pageIndex);
		pagingEntity.setMaxResults(maxResults);

		// 组合为SELECT COUNT(*) FROM ...的语句
		String sizeQueryString = "SELECT COUNT(*) FROM (" + queryString + ")";

		/* 总记录数 */
		int total = -1;
		try {
			if (useSQL) {
				total = NumberHelper.intValue(useSQL ? dao.uniqueResultSQL(sizeQueryString, parameters) : dao
						.uniqueResult(sizeQueryString, parameters));
			}
		} catch (Exception e) {
			// e.printStackTrace();
			total = -1;
		}
		if (total == -1) {
			try {
				// 不支持多级查询
				int p = 0;
				sizeQueryString = queryString;
				// 首先除去orderBy
				if ((p = sizeQueryString.toUpperCase().indexOf(" ORDER BY ")) > -1) {
					sizeQueryString = sizeQueryString.substring(0, p);
				}
				// 除去GROUP BY语句
				if ((p = sizeQueryString.toUpperCase().indexOf(" GROUP BY ")) > -1) {
					sizeQueryString = sizeQueryString.substring(0, p);
				}

				sizeQueryString = sizeQueryString.substring(Math.max(0, queryString.toUpperCase().indexOf(" FROM ")));
				sizeQueryString = "SELECT COUNT(*) " + sizeQueryString;
				total = NumberHelper.intValue(useSQL ? dao.uniqueResultSQL(sizeQueryString, parameters) : dao
						.uniqueResult(sizeQueryString, parameters));
			} catch (Exception ex) {
				try {
					// 直接进行滚动数据集操作
					total = dao.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Integer>() {
						public Integer doInHibernate(Session session) throws HibernateException {
							Query query = useSQL ? session.createSQLQuery(queryString) : session
									.createQuery(queryString);
							if (parameters != null) {
								for (int i = 0; i < parameters.length; i++) {
									query.setParameter(i, parameters[i]);
								}
							}
							ScrollableResults rs = query.scroll();
							rs.last();
							return rs.getRowNumber() + 1;
						}
					});
				} catch (Exception exc) {
					// TODO
					exc.printStackTrace();
					throw new RuntimeException(exc);
				}
			}
		}
		// 没有数据
		if (total <= 0) return pagingEntity;
		pagingEntity.setTotal(total);

		/* 总页数 */
		final int pageCount = (int) Math.ceil((double) total / (double) maxResults);
		pagingEntity.setPageCount(pageCount);
		if (pageIndex > pageCount) return pagingEntity;

		List<E> list = dao.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<E>>() {
			public List<E> doInHibernate(Session session) throws HibernateException {
				Query query = useSQL ? session.createSQLQuery(queryString) : session.createQuery(queryString);
				if (parameters != null) {
					for (int i = 0; i < parameters.length; i++) {
						query.setParameter(i, parameters[i]);
					}
				}
				query.setFirstResult((pageIndex - 1) * maxResults);
				query.setMaxResults(maxResults);
				return query.list();
			}
		});
		pagingEntity.setList(list);
		pagingEntity.setSize(pageIndex < pageCount ? maxResults : total - (pageCount - 1) * maxResults);
		return pagingEntity;
	}
}
