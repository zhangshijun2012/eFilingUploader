package com.sinosoft.util.hibernate.paging;


/**
 * 分页服务接口
 * 
 * @author LuoGang
 * 
 */
public interface PagingQuery<E> {
	/**
	 * 使用queryString进行分页查询
	 * 
	 * @param queryString 查询语句
	 * @param parameters 参数
	 * @param pageIndex 页数
	 * @param maxResults 每页数量
	 * @return
	 */
	public PagingEntity<E> query(String queryString, Object[] parameters, int pageIndex, int maxResults);

}
