package com.sinosoft.util.hibernate.paging;

import com.sinosoft.util.hibernate.dao.EntityDaoSupport;

/**
 * 使用SQL语句进行分页查询
 * 
 * @author LuoGang
 * 
 * @param <E>
 */
public class SQLPagingQuery<E> extends AbstractPagingQuery<E> {

	public SQLPagingQuery(EntityDaoSupport<?> dao) {
		super(dao, true);
	}
}
