package com.sinosoft.util.json;

import org.json.JSONString;

/**
 * 可转换为JSON对象的数据
 * 
 * @author LuoGang
 * 
 */
public interface JSON<V> extends JSONString, Cloneable {
	/**
	 * 转换为JSON字符串
	 * 
	 * @return
	 */
	public String toJSONString();

	/**
	 * 将value转换为json对象.此方法转换的json数据会修改当前对象
	 * 
	 * @param value
	 * @return this
	 */
	@SuppressWarnings("rawtypes")
	public JSON from(V value);

	/**
	 * 将value转换为一个新的json对象，不会改变当前对象
	 * 
	 * @param value
	 * @return new JSON()
	 */
	public Object convert(V value);
}
