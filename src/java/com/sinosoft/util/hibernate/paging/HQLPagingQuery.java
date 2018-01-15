package com.sinosoft.util.hibernate.paging;

import com.sinosoft.util.hibernate.dao.EntityDaoSupport;

public class HQLPagingQuery<E> extends AbstractPagingQuery<E> {
	/**
	 * 构造函数
	 * 
	 * @param dao
	 */
	public HQLPagingQuery(EntityDaoSupport<?> dao) {
		super(dao, false);
	}

}
