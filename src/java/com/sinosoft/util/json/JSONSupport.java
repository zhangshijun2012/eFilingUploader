package com.sinosoft.util.json;

import org.json.JSONString;

/**
 * 默认的json对象
 * 
 * @author LuoGang
 * 
 */
public class JSONSupport implements JSON<Object>, JSONString {
	Object value;

	public JSONSupport(Object value) {
		this.value = value;
	}

	public String toJSONString() {
		return value == null ? "null" : "\"" + value.toString().replace("\"", "\\\"") + "\"";
	}

	public JSON<Object> from(Object value) {
		this.value = value;
		return this;
	}

	public JSON<?> convert(Object value) {
		return new JSONSupport(value);
	}

}
