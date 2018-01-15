package com.sinosoft.util.hibernate.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;

import com.sinosoft.util.hibernate.entity.Entity;
import com.sinosoft.util.hibernate.paging.PagingEntity;

public interface EntityDao<E extends Entity<?>> extends HibernateDao {

	/**
	 * 得到实体类型E的Class对象
	 * 
	 * @return E的class，等于E.class
	 */
	public Class<E> getEntityClass();

	/**
	 * 类名，等于E.class.getName();
	 * 
	 * @return
	 */
	public String getEntityClassName();

	/**
	 * 删除实例
	 * 
	 * @param entity 要删除的对象
	 */
	public void delete(E entity);

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
	 * 执行不带参数的hql语句
	 * 
	 * @param hql
	 */
	public int execute(String hql);

	/**
	 * 执行带参数的hql语句
	 * 
	 * @param hql
	 * @param parameters 参数
	 */
	public int execute(String hql, Object[] parameters);

	/**
	 * 执行不带参数的sql语句
	 * 
	 * @param sql 原生的sql语句
	 */
	public int executeSQL(String sql);

	/**
	 * 执行带参数的sql语句
	 * 
	 * @param sql 原生的sql语句
	 * @param parameters 参数
	 */
	public int executeSQL(String sql, Object[] parameters);

	/**
	 * 判断实例entity是否存在
	 * 
	 * @param entity
	 * @return Boolean 数据是否存在
	 */
	public boolean exists(E entity);

	/**
	 * 判断主键为id的数据是否存在
	 * 
	 * @param id 主键值
	 * @return Boolean 数据是否存在
	 */
	public boolean existsById(Serializable id);

	/**
	 * 根据主键查询数据
	 * 
	 * @param id 主键值
	 * @return E 得到的实体
	 */
	public E get(Serializable id);

	/**
	 * 通过属性查询唯一的数据
	 * 
	 * @param propertyName 属性名,应该是一个唯一键
	 * @param value 值
	 * @return E 得到的实体
	 */
	public E getByProperty(String propertyName, Object value);

	/**
	 * 通过属性查询数据
	 * 
	 * @param propertyName
	 * @param value
	 * @return
	 */
	public List<E> queryByProperty(String propertyName, Object value);

	/**
	 * 
	 * 通过属性查询数据
	 * 
	 * @param properties
	 * @return
	 */
	public List<E> queryByProperty(Map<String, Object> properties);

	/**
	 * 
	 * 通过属性查询数据
	 * 
	 * @param properties
	 * @return
	 */
	public List<E> queryByProperty(Object[][] properties);

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
	 * 
	 * 通过带参数的sql语句查询数据, 返回的数据为hibernate映射的实体类
	 * 
	 * @param sql
	 * @param parameters
	 * @param clses
	 * @return
	 * @see org.hibernate.SQLQuery#addEntity(Class)
	 */
	public List<?> querySQL(final String sql, final Object[] parameters, final Class<?>... clses);

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

	/**
	 * 查询单一数据
	 * 
	 * @param hql
	 * @return
	 */
	public Object uniqueResult(String hql);

	/**
	 * 查询单一数据
	 * 
	 * @param hql
	 * @param parameters 参数
	 * @return
	 */
	public Object uniqueResult(String hql, Object[] parameters);

	/**
	 * 查询单一数据
	 * 
	 * @param sql
	 * @return
	 */
	public Object uniqueResultSQL(String sql);

	/**
	 * 查询单一数据
	 * 
	 * @param sql
	 * @param parameters 参数
	 * @return
	 */
	public Object uniqueResultSQL(String sql, Object[] parameters);

	/**
	 * 新增实例
	 * 
	 * @param entity
	 * @return 保存后的主键
	 */
	public Serializable save(E entity);

	/**
	 * 更新或保存数据。如果entity已经存在则进行更新，否则进行新增. 注意：此方法与不是单纯调用session.saveOrUpdate方法
	 * 
	 * @param entity
	 */
	public void saveOrUpdate(E entity);

	/**
	 * 更新数据
	 * 
	 * @param entity
	 */
	public void update(E entity);

	/**
	 * 回滚事务,Spring托管的事务回滚TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	 */
	public void rollback();

	/**
	 * 手动提交hibernate事务
	 */
	public void commit();

	/**
	 * 手工启动事务
	 * 
	 * @return
	 */
	public Transaction beginTransaction();
}
