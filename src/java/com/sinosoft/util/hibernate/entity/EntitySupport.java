package com.sinosoft.util.hibernate.entity;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.sinosoft.util.json.JSONObject;

/**
 * Entity的实现
 * 
 * @author LuoGang
 * @param <I> id类型
 */
public class EntitySupport<I extends Serializable> implements Entity<I>, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3025154317495101671L;

	/** 默认使用的JSON转换器c */
	public static final JSONConverterSupport DEFAULT_JSON_CONVERTER = new JSONConverterSupport();
	/**
	 * 仅转换基础类型为JSON对象的转换器
	 */
	public static final JSONConverterSupport DEFAULT_VALUE_JSON_CONVERTER = new JSONConverterSupport() {
		/**
		 * 是否允许转换method方法的值
		 * 
		 * @param method
		 * @return
		 */
		protected boolean applyMethod(Method method) {
			if (!Entity.class.isAssignableFrom(method.getDeclaringClass())) return false;
			Class<?> cls = method.getReturnType();
			if (Entity.class.isAssignableFrom(cls)) return false; // 不转换内部的Entity属性,即外键
			if (Iterable.class.isAssignableFrom(cls)) return false; // 不转换Set属性
			return super.applyMethod(method);
			// 仅转换public和参数为0的getXXX,isXXX方法
			// if (!Modifier.isPublic(method.getModifiers()) || method.getParameterTypes().length != 0) return false;
			// if (!Entity.class.isAssignableFrom(method.getDeclaringClass())) return false;
			// Class<?> cls = method.getReturnType();
			// if (Entity.class.isAssignableFrom(cls)) return false; // 不转换内部的Entity属性,即外键
			// if (Collection.class.isAssignableFrom(cls)) return false; // 不转换Set属性
			// String name = method.getName();
			// // getClass方法不转换
			// if ("getClass".equals(name) || "getDeclaringClass".equals(name)) return false;
			// boolean applyMethod = MethodFilterInterceptorUtil.applyMethod(excludeMethods, includeMethods, name);
			// return applyMethod;
		}
	};

	static {
		((JSONConverterSupport) DEFAULT_JSON_CONVERTER).addExcludeMethods("getUser,getInsertTime,"
				+ "getUpdateUser,getUpdateDepartment,getUpdateTime," + "getDepartment,getCompany,getParent,getParents");
	}

	/** 物理主键，不会改变 */
	protected I id;

	/** 描述 */
	// protected String name;

	public EntitySupport() {
		super();
	}

	public I getId() {
		return id;
	}

	public void setId(I id) {
		this.id = id;
	}

	// public String getName() {
	// return name;
	// }
	//
	// public void setName(String name) {
	// this.name = name;
	// }

	@SuppressWarnings("unchecked")
	@Override
	public Entity<I> clone() {
		try {
			return (Entity<I>) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将当前对象输出为json字符串
	 * 
	 * @return
	 */
	public String toJSONString() {
		return this.toJSONString(DEFAULT_JSON_CONVERTER);
	}

	/**
	 * 将当前对象按JSON格式输出,默认仅通过public getXXX方法得到的值进行转换
	 */
	public String toJSONString(JSONConverter converter) {
		JSONObject json = converter.convert(this);
		return json.toString();
	}

	public JSONObject toJSONObject() {
		return DEFAULT_JSON_CONVERTER.convert(this);
	}

	public JSONObject toJSONObject(JSONConverter converter) {
		return converter.convert(this);
	}

	/**
	 * 将当前对象转换为仅含有基础数据类型的JSON对象
	 * 
	 * @return
	 */
	public JSONObject toDefaultValueJSONObject() {
		return DEFAULT_VALUE_JSON_CONVERTER.convert(this);
	}
}
