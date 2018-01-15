package com.sinosoft.util.json;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONTokener;

@SuppressWarnings("rawtypes")
public class JSONArray extends org.json.JSONArray implements JSON {
	private String datePattern;

	public JSONArray() {
		super();
		// TODO Auto-generated constructor stub
	}

	public JSONArray(Collection collection) {
		super();
		if (collection != null) {
			Iterator iter = collection.iterator();
			while (iter.hasNext()) {
				this.put(iter.next());
			}
		}
	}

	public JSONArray(Iterable iterable) {
		super();
		if (iterable != null) {
			Iterator iter = iterable.iterator();
			while (iter.hasNext()) {
				this.put(iter.next());
			}
		}
	}

	public JSONArray(JSONTokener x) throws JSONException {
		super(x);
		// TODO Auto-generated constructor stub
	}

	public JSONArray(Object array) throws JSONException {
		this();
		if (array.getClass().isArray()) {
			int length = Array.getLength(array);
			for (int i = 0; i < length; i += 1) {
				this.put(Array.get(array, i));
			}
		} else {
			throw new JSONException("JSONArray initial value should be a string or collection or iterable or array.");
		}
	}

	public JSONArray(String source) throws JSONException {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public JSONArray from(Object value) {
		if (value == null) return this;
		if (value instanceof org.json.JSONArray) {
			org.json.JSONArray array = (org.json.JSONArray) value;
			for (int index = 0, length = array.length(); index < length; index++) {
				this.put(array.get(index));
			}
			return this;
		}
		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			for (int i = 0; i < length; i += 1) {
				this.put(Array.get(value, i));
			}
		} else if (value instanceof Iterable) {
			Iterator iter = ((Iterable) value).iterator();
			while (iter.hasNext()) {
				this.put(iter.next());
			}
		} else {
			throw new JSONException("JSONArray initial value should be a string or collection or iterable or array.");
		}
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

	@Override
	public org.json.JSONArray put(Collection value) {
		return this.put((Iterable) value);
	}

	public org.json.JSONArray put(Iterable value) {
		return super.put(this.convert(value));
	}

	@Override
	public org.json.JSONArray put(Map value) {
		return super.put(convert(value));
	}

	@Override
	public org.json.JSONArray put(Object value) {
		return super.put(convert(value));
	}

	@Override
	public org.json.JSONArray put(int index, Collection value) throws JSONException {
		return super.put(index, (Iterable) value);
	}

	public org.json.JSONArray put(int index, Iterable value) {
		return super.put(index, convert(value));
	}

	@Override
	public org.json.JSONArray put(int index, Map value) throws JSONException {
		return super.put(index, convert(value));
	}

	@Override
	public org.json.JSONArray put(int index, Object value) throws JSONException {
		return super.put(index, convert(value));
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String toJSONString() {
		return super.toString();
	}

}
