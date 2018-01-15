package com.sinosoft.util.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Transaction;

import com.sinosoft.util.hibernate.dao.EntityDao;
import com.sinosoft.util.hibernate.entity.Entity;
import com.sinosoft.util.hibernate.paging.PagingEntity;

public interface Service<E extends Entity<?>, D extends EntityDao<E>> {

	/**
	 * 得到数据操作类实例
	 * 
	 * @return
	 */
	public D getDao();

	public void setDao(D dao);

	/** 根据主键查询数据 */
	public E get(Serializable id);

	/**
	 * 新建数据
	 * 
	 * @param entity
	 * @return
	 */
	public E save(E entity);

	/**
	 * 保存多个数据
	 * 
	 * @param list
	 * @return
	 */
	public List<E> save(List<E> list);

	/**
	 * 更新数据
	 * 
	 * @param entity
	 * @return
	 */
	public E update(E entity);

	/**
	 * 如果entity不存在，则新建数据；否则保存数据
	 * 
	 * @param entity
	 * @return
	 */
	public E saveOrUpdate(E entity);

	/**
	 * 删除数据
	 * 
	 * @param entity
	 * @return
	 */
	public E delete(E entity);

	/**
	 * 根据主键删除数据
	 * 
	 * @param id 主键值
	 * @return 被删除的对象
	 */
	public E deleteById(Serializable id);

	/**
	 * 根据主键数组删除多个数据
	 * 
	 * @param ids 主键数组
	 * @return 被删除的数量
	 */
	public int deleteById(Serializable[] ids);

	/**
	 * 通过不带参数hql语句查询数据
	 * 
	 * @param hql
	 * @return
	 */
	public List<?> query(String hql);

	/**
	 * 通过带参数的hql语句查询数据
	 * 
	 * @param hql
	 * @param parameters 参数
	 * @return
	 */
	public List<?> query(String hql, Object[] parameters);

	/**
	 * 
	 * 通过不带参数sql语句查询数据
	 * 
	 * @param sql 原生的sql查询语句
	 * @return
	 */
	public List<?> querySQL(String sql);

	/**
	 * 
	 * 通过带参数的sql语句查询数据
	 * 
	 * @param sql 原生的sql查询语句
	 * @param parameters
	 * @return
	 */
	public List<?> querySQL(String sql, Object[] parameters);

	/**
	 * 使用hql进行分页查询
	 * 
	 * @param hql 查询语句
	 * @param parameters 参数
	 * @param pageIndex 页数
	 * @param maxResults 每页数量
	 * @return
	 */
	public PagingEntity<?> query(String hql, Object[] parameters, int pageIndex, int maxResults);

	/**
	 * 使用sql进行分页查询
	 * 
	 * @param sql 查询语句
	 * @param parameters 参数
	 * @param pageIndex 页数
	 * @param maxResults 每页数量
	 * @return
	 */
	public PagingEntity<?> querySQL(String sql, Object[] parameters, int pageIndex, int maxResults);

	/** 手动回滚事务 */
	public void rollback();

	/** 手动提交事务 */
	public void commit();

	/**
	 * 手工开启事务
	 * 
	 * @return
	 */
	public Transaction beginTransaction();

}
