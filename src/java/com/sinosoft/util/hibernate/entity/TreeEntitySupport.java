package com.sinosoft.util.hibernate.entity;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sinosoft.util.json.JSONObject;

/**
 * TreeEntity的实现
 * 
 * @author LuoGang
 * @param <I> id的类型
 * @param <E> 树节点的类型
 */
@SuppressWarnings("unchecked")
public class TreeEntitySupport<I extends Serializable, E extends TreeEntity<I, E>> extends EntitySupport<I> implements
		TreeEntity<I, E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1671445146841494950L;

	protected E parent;

	protected Set<E> children;

	protected String treeId;

	public boolean addChild(E child) {
		return this.children.add(child);
	}

	public Set<E> getChildren() {
		return this.children;
	}

	public E getParent() {
		return this.parent;
	}

	public void setChildren(Set<E> children) {
		this.children = children;

	}

	public void setParent(E parent) {
		this.parent = parent;
	}

	public boolean isEmpty() {
		return this.children == null || this.children.isEmpty();
	}

	public boolean isChildOf(E parent) {
		I parentId = parent == null ? null : parent.getId();
		I lastParentId = parentId;
		E e = this.getParent();
		while (e != null && !lastParentId.equals(e.getId())) {
			if (e.equals(parent) || e.getId().equals(parentId)) {
				return true;
			}
			lastParentId = e.getId();
			e = e.getParent();
		}
		return e != null && (e.equals(parent) || parentId.equals(e.getId()));
	}

	public boolean isParentOf(E child) {
		return child.isChildOf((E) this);
	}

	/**
	 * 该对象是否为叶子节点.若返回true,则该对象无法增加子节点
	 * 
	 * @return
	 */
	public boolean isFinal() {
		return false;
	}

	public E getRoot() {
		E root = (E) this;
		E parent = root.getParent();
		while (parent != null && parent.getId() != null && !parent.equals(root) && !root.getId().equals(parent.getId())) {
			root = parent;
			parent = root.getParent();
		}
		return root;
	}

	public E[] getAbsolutePath() {
		List<E> absolutePath = new ArrayList<E>();

		E node = (E) this;
		E parent = node.getParent();
		absolutePath.add(node);
		while (parent != null && !parent.equals(node) && !node.getId().equals(parent.getId())) {
			node = parent;
			parent = node.getParent();
			absolutePath.add(0, node);
		}

		if (absolutePath.isEmpty()) return null;
		E[] nodes = (E[]) Array.newInstance(getClass(), absolutePath.size());
		return absolutePath.toArray(nodes);
	}

	public E[] getParentPath() {
		List<E> absolutePath = new ArrayList<E>();

		E node = (E) this;
		E parent = node.getParent();
		while (parent != null && !parent.equals(node) && !node.getId().equals(parent.getId())) {
			node = parent;
			parent = node.getParent();
			absolutePath.add(0, node);
		}
		if (absolutePath.isEmpty()) return null;
		E[] nodes = (E[]) Array.newInstance(getClass(), absolutePath.size());
		return absolutePath.toArray(nodes);
	}

	public E[] getParentPath(E from) {
		if (from == null) return this.getParentPath();

		boolean found = false;
		List<E> absolutePath = new ArrayList<E>();

		E node = (E) this;
		E parent = node.getParent();
		while (parent != null && !parent.equals(node) && !node.getId().equals(parent.getId())) {
			if (node.equals(from)) {
				found = true;
				break;
			}
			node = parent;
			parent = node.getParent();
			absolutePath.add(0, node);
		}
		if (!found) {
			throw new RuntimeException("from不是当前节点的父节点!");
		}
		if (absolutePath.isEmpty()) return null;
		E[] nodes = (E[]) Array.newInstance(getClass(), absolutePath.size());
		return absolutePath.toArray(nodes);
	}

	public void visit(com.sinosoft.util.hibernate.entity.TreeEntity.Visitor<E> visitor) {
		visitor.visit((E) this);
		if (children != null && !children.isEmpty()) {
			for (E child : children) {
				if (!child.equals(this) && !id.equals(child.getId())) {
					// 防止因为父节点是自身而造成的死循环
					child.visit(visitor);
				}
			}
		}
	}

	@Override
	public E clone() {
		return (E) super.clone();
	}

	public boolean isRoot() {
		return parent == null || this.equals(parent);
	}

	/** 默认使用的JSON转换器 */
	public static final JSONConverterSupport DEFAULT_JSON_CONVERTER = EntitySupport.DEFAULT_JSON_CONVERTER.clone();// new
																													// JSONConverterSupport();
	static {
		Method[] excludeMethods = TreeEntitySupport.class.getDeclaredMethods(); // 不包含当前类中的方法
		for (Method method : excludeMethods) {
			DEFAULT_JSON_CONVERTER.addExcludeMethod(method.getName());
		}
	}

	@Override
	public String toJSONString() {
		return toJSONString(DEFAULT_JSON_CONVERTER);
	}

	@Override
	public JSONObject toJSONObject() {
		// TODO Auto-generated method stub
		return super.toJSONObject(DEFAULT_JSON_CONVERTER);
	}
}
