package com.sinosoft.util.json;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONTokener;
import org.springframework.beans.BeanUtils;

@SuppressWarnings("rawtypes")
public class JSONObject extends org.json.JSONObject implements JSON {
	// 日期转换格式
	private String datePattern;

	public JSONObject() {
		super();
		// TODO Auto-generated constructor stub
	}

	public JSONObject(org.json.JSONObject jo, String[] names) {
		super(jo, names);
		// TODO Auto-generated constructor stub
	}

	public JSONObject(JSONTokener x) throws JSONException {
		super(x);
		// TODO Auto-generated constructor stub
	}

	public JSONObject(Map map) {
		super();
		from(map);
	}

	public JSONObject(Object object, String[] names) {
		super(object, names);
		// TODO Auto-generated constructor stub
	}

	public JSONObject(Object bean) {
		this();
		this.from(bean);
	}

	/**
	 * 转换map对象
	 * 
	 * @param map
	 * @return
	 */
	public JSONObject from(Map map) {
		if (map == null || map.isEmpty()) return this;
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			Object value = e.getValue();
			if (value != null) {
				this.put(e.getKey().toString(), value);
			}
		}
		return this;
	}

	/**
	 * 转换javaBean对象
	 * 
	 * @param bean
	 * @return
	 */
	public JSONObject from(Object bean) {
		if (bean == null) return this;
		if (bean instanceof String) return this.read((String) bean);
		if (bean instanceof JSONTokener) return this.read((JSONTokener) bean);
		if (bean instanceof Map) return this.from((Map) bean);
		if (bean instanceof org.json.JSONObject) {
			org.json.JSONObject map = (org.json.JSONObject) bean;
			for (Object key : map.keySet()) {
				this.put(key.toString(), map.get(key.toString()));
			}
			return this;
		}

		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(bean.getClass());
		Method readMethod;
		Object value;
		for (PropertyDescriptor pd : pds) {
			readMethod = pd.getReadMethod();
			if (readMethod == null) continue;
			try {
				value = readMethod.invoke(bean);
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
				value = null;
			}
			this.put(pd.getName(), value);
		}

		// Class klass = bean.getClass();
		//
		// // If klass is a System class then set includeSuperClass to false.
		//
		// boolean includeSuperClass = klass.getClassLoader() != null;
		//
		// Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
		// for (int i = 0; i < methods.length; i += 1) {
		// try {
		// Method method = methods[i];
		// if (Modifier.isPublic(method.getModifiers())) {
		// String name = method.getName();
		// String key = "";
		// if (name.startsWith("get")) {
		// if ("getClass".equals(name) || "getDeclaringClass".equals(name)) {
		// key = "";
		// } else {
		// key = name.substring(3);
		// }
		// } else if (name.startsWith("is")) {
		// key = name.substring(2);
		// }
		// if (key.length() > 0 && Character.isUpperCase(key.charAt(0))
		// && method.getParameterTypes().length == 0) {
		// if (key.length() == 1) {
		// key = key.toLowerCase();
		// } else if (!Character.isUpperCase(key.charAt(1))) {
		// key = key.substring(0, 1).toLowerCase() + key.substring(1);
		// }
		//
		// Object result = method.invoke(bean, (Object[]) null);
		// if (result != null) {
		// this.put(key, result);
		// }
		// }
		// }
		// } catch (Exception ignore) {
		// }
		// }
		return this;
	}

	public Object convert(Object value) {
		if (value == null) return null;
		if (value instanceof JSON || value instanceof org.json.JSONObject || value instanceof org.json.JSONArray
				|| value instanceof org.json.JSONString) {
			return value;
		}
		if (value.getClass().isArray() || value instanceof Iterable) {
			JSONArray array = new JSONArray();
			array.setDatePattern(datePattern);
			value = array.from(value);
		} else if (value instanceof Date) {
			value = new JSONDate((Date) value, datePattern);
		} else if (value instanceof Map) {
			JSONObject o = new JSONObject();
			o.setDatePattern(datePattern);
			value = o.from(value);
		} else {
			Package objectPackage = value.getClass().getPackage();
			String objectPackageName = objectPackage != null ? objectPackage.getName() : "";
			if (objectPackageName.startsWith("java.") || objectPackageName.startsWith("javax.")
					|| value.getClass().getClassLoader() == null) {
				// java自带类
			} else {
				JSONObject o = new JSONObject();
				o.setDatePattern(datePattern);
				value = o.from(value);
			}
		}
		return value;
	}

	public JSONObject(String baseName, Locale locale) throws JSONException {
		super(baseName, locale);
		// TODO Auto-generated constructor stub
	}

	public JSONObject(String source) throws JSONException {
		super(source);
		// TODO Auto-generated constructor stub
	}

	@Override
	public org.json.JSONObject put(String key, Object value) throws JSONException {
		return super.put(key, convert(value));
	}

	@Override
	public org.json.JSONObject put(String key, Collection value) throws JSONException {
		return this.put(key, (Iterable) value);
	}

	public org.json.JSONObject put(String key, Iterable value) throws JSONException {
		return super.put(key, convert(value));
	}

	public String toJSONString() {
		return super.toString();
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	/**
	 * 读取JSON字符串的数据
	 * 
	 * @param value JSON格式的字符串
	 * @return this
	 */
	public JSONObject read(String value) {
		JSONTokener reader = new JSONTokener(value);
		return read(reader);
	}

	/**
	 * 读取JSON字符串转换的JSONTokener对象
	 * 
	 * @param x JSON格式字符串转换的JSONTokener对象
	 * @return this
	 */
	public JSONObject read(JSONTokener x) {
		char c;
		String key;

		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}
		for (;;) {
			c = x.nextClean();
			switch (c) {
			case 0:
				throw x.syntaxError("A JSONObject text must end with '}'");
			case '}':
				return this;
			default:
				x.back();
				key = x.nextValue().toString();
			}

			// The key is followed by ':'. We will also tolerate '=' or '=>'.

			c = x.nextClean();
			if (c == '=') {
				if (x.next() != '>') {
					x.back();
				}
			} else if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}
			this.putOnce(key, x.nextValue());

			// Pairs are separated by ','. We will also tolerate ';'.

			switch (x.nextClean()) {
			case ';':
			case ',':
				if (x.nextClean() == '}') {
					return this;
				}
				x.back();
				break;
			case '}':
				return this;
			default:
				throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	@Override
	public Object get(String key) throws JSONException {
		return this.opt(key);
	}
}
