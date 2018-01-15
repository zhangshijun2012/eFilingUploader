package com.sinosoft.util.hibernate.entity;

import java.io.Serializable;

import org.json.JSONString;

import com.sinosoft.util.json.JSONObject;


/**
 * 所有实体bean的父类，主要定义了id,name
 * 
 * @author LuoGang
 * 
 * @param <I> id类型
 */
public interface Entity<I extends Serializable> extends Cloneable, Serializable, JSONString {
	/**
	 * 主键
	 * 
	 * @return
	 */
	public I getId();

	public void setId(I id);

	// /**
	// * 描述
	// *
	// * @return
	// */
	// public String getName();
	//
	// public void setName(String name);

	/**
	 * 将该实体类格式化为json字符串
	 * 
	 * @return
	 */
	public String toJSONString();

	public String toJSONString(JSONConverter converter);

	/**
	 * 将当前对象转换为JSON对象
	 * 
	 * @return
	 */
	public JSONObject toJSONObject();

	/**
	 * 
	 * 将当前对象转换为JSON对象
	 * 
	 * @param converter
	 * @return
	 */
	public JSONObject toJSONObject(JSONConverter converter);

	/***
	 * JSONConverter,将对象转换为json字符串
	 * 
	 * @author LuoGang
	 * 
	 */
	public static interface JSONConverter {
		/**
		 * 将bea转换为可json化的对象
		 * 
		 * @param bean
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public JSONObject convert(Entity bean);
	}
}
