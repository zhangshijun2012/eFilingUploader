package com.sinosoft.util.hibernate.entity;

import java.io.Serializable;
import java.util.Set;

/**
 * 树形结构的实体,主要定义了parent,children.
 * 
 * @author LuoGang
 * @param <I> id的类型
 * @param <E> 树节点的类型.当具体的类型实现此类接口时,E就是类本身
 */
public interface TreeEntity<I extends Serializable, E extends TreeEntity<I, E>> extends Entity<I> {
	/**
	 * 得到根节点
	 * 
	 * @return
	 */
	public E getRoot();

	/**
	 * 得到从根节点开始到当前节点的全路径,从根节点开始,到节点本身
	 * 
	 * @return
	 */
	public E[] getAbsolutePath();

	/**
	 * 得到整个路径,从根节点开始,到本节点的父节点
	 * 
	 * @return
	 */
	public E[] getParentPath();

	/**
	 * 得到从from开始到当前父节点路径.不包括from节点本身.如果节点为null,则相当于从根节点开始
	 * 
	 * @param from 必须是当前节点的父节点
	 * @return
	 * @throws RuntimeException 如果from不是当前节点的父节点则抛出异常RuntimeException
	 */
	public E[] getParentPath(E from);

	/**
	 * 父节点
	 * 
	 * @return
	 */
	public E getParent();

	public void setParent(E parent);

	/**
	 * 子节点
	 * 
	 * @return
	 */
	public Set<E> getChildren();

	public void setChildren(Set<E> children);

	/**
	 * 增加一个子节点
	 * 
	 * @param child
	 */
	public boolean addChild(E child);

	/**
	 * 是否是child的父节点
	 * 
	 * @param child
	 */
	public boolean isParentOf(E child);

	/**
	 * 是否是parent的子节点
	 * 
	 * @param child
	 */
	public boolean isChildOf(E parent);

	/**
	 * 是否有子节点
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * 是否叶子节点,为true则不允许新增根节点
	 * 
	 * @return
	 */
	public boolean isFinal();

	/**
	 * 是否为根节点
	 * 
	 * @return
	 */
	public boolean isRoot();

	/**
	 * 从当前节点开始，遍历自己及其下的所有子节点。
	 * 
	 * @param visitor 遍历到节点时执行的方法
	 */
	public void visit(Visitor<E> visitor);

	public static interface Visitor<E extends TreeEntity<?, E>> {
		/**
		 * 遍历到节点e是调用的方法
		 * 
		 * @param e
		 */
		public void visit(E e);
	}

}
