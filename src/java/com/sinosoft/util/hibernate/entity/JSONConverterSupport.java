package com.sinosoft.util.hibernate.entity;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.beans.BeanUtils;

import com.opensymphony.xwork2.interceptor.MethodFilterInterceptorUtil;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.sinosoft.util.DateHelper;
import com.sinosoft.util.hibernate.entity.Entity.JSONConverter;
import com.sinosoft.util.json.JSONDate;
import com.sinosoft.util.json.JSONObject;

/**
 * 默认的JSON处理器
 * 
 * @author LuoGang
 * 
 */
public class JSONConverterSupport implements JSONConverter, Cloneable {
	public JSONConverterSupport() {
		datePattern = DateHelper.DEFAULT_DATE_FORMAT;
		depth = 0;
		maxDepth = 1; // 默认可迭代一次
	}

	/** 日期转换为字符串的格式 */
	private String datePattern;

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	/** 迭代处理Entity对象的次数 */
	private int depth;
	/** 最大允许的次数 */
	private int maxDepth;

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	/**
	 * 当前是否可处理Entity属性
	 * 
	 * @return
	 */
	public boolean accept() {
		return maxDepth < 0 || depth < maxDepth;
	}

	/** 除外的方法 */
	protected Set<String> excludeMethods = new HashSet<String>();
	/** 允许的方法 */
	protected Set<String> includeMethods = new HashSet<String>();

	/** 除外的属性 */
	protected Set<String> excludeProperties = new HashSet<String>();
	/** 允许的属性 */
	protected Set<String> includeProperties = new HashSet<String>();

	/**
	 * 是否允许转换method方法的值
	 * 
	 * @param method
	 * @return
	 */
	protected boolean applyMethod(Method method) {
		// 仅转换public和参数为0的getXXX,isXXX方法
		if (!Modifier.isPublic(method.getModifiers()) || method.getParameterTypes().length != 0) return false;
		if (!Entity.class.isAssignableFrom(method.getDeclaringClass())) return false;
		String name = method.getName();
		// getClass方法不转换
		if ("getClass".equals(name) || "getDeclaringClass".equals(name)) return false;
		boolean applyMethod = MethodFilterInterceptorUtil.applyMethod(excludeMethods, includeMethods, name);
		return applyMethod;
	}

	/**
	 * 是否允许转换name属性的值
	 * 
	 * @param name
	 * @return
	 */
	protected boolean applyProperty(String name) {
		boolean applyMethod = MethodFilterInterceptorUtil.applyMethod(excludeProperties, includeProperties, name);
		return applyMethod;
	}

	@SuppressWarnings("rawtypes")
	public JSONObject convert(Entity entity) {
		Map<String, Object> map = new HashMap<String, Object>();
		Class entityClass = HibernateProxyHelper.getClassWithoutInitializingProxy(entity);
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(entityClass);
		Method readMethod;
		String name;
		Object value;
		for (PropertyDescriptor pd : pds) {
			readMethod = pd.getReadMethod();
			if (readMethod == null) continue;
			if (!applyMethod(readMethod)) continue;
			name = pd.getName();
			if (!applyProperty(name)) continue;
			try {
				value = readMethod.invoke(entity);
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
				value = null;
			}
			map.put(pd.getName(), convert(value, entity));
		}

		// Map<String, Object> map = new HashMap<String, Object>();
		// Method[] methods = entityClass.getMethods();
		// for (int i = 0; i < methods.length; i += 1) {
		// Method method = methods[i];
		// if (!applyMethod(method)) continue;
		// String name = method.getName();
		// String key;
		// if (name.startsWith("get")) {
		// key = name.substring(3);
		// } else if (name.startsWith("is")) {
		// key = name.substring(2);
		// } else {
		// continue;
		// }
		// if (key.length() < 1 || !Character.isUpperCase(key.charAt(0))) continue;
		// if (key.length() == 1) {
		// key = key.toLowerCase();
		// } else if (!Character.isUpperCase(key.charAt(1))) {
		// key = key.substring(0, 1).toLowerCase() + key.substring(1);
		// }
		//
		// Object result;
		// try {
		// result = method.invoke(entity, (Object[]) null);
		// if (result != null) {
		// result = convert(result, entity);
		// if (result != null) map.put(key, result);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// }

		JSONObject o = new JSONObject();
		o.setDatePattern(datePattern);
		o.from(map);
		return o;

	}

	/** 迭代处理内部Entity对象时使用的转换器,其depth比当前大1 */
	private JSONConverterSupport internal;

	/** 迭代处理的内部转换器 */
	protected JSONConverterSupport getInternal() {
		if (internal == null) {
			internal = clone();
			internal.internal = null;
			internal.depth++;
		}
		return internal;
	}

	/**
	 * 将通过bean得到的value转换为可json化的对象
	 * 
	 * @param value
	 * @param entity
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convert(final Object value, Entity entity) {
		if (value == null) return null;
		if (value instanceof Date) {
			// 转换对象日期
			// return DateHelper.format((Date) value, getDatePattern());
			return new JSONDate((Date) value, getDatePattern());
		}
		if (value instanceof Entity) {
			if (!this.accept()) return null;
			return getInternal().convert((Entity) value);
		}
		if (value.getClass().isArray()) {
			// 数组
			List list = new ArrayList();
			for (int i = 0, l = Array.getLength(value); i < l; i++) {
				list.add(convert(Array.get(value, i), entity));
			}
			if (list.isEmpty() || list.size() == 1 && list.get(0) == null) return null;
			return list;
		}
		if (value instanceof Iterable) {
			// 集合
			List list = new ArrayList();
			for (Object obj : (Iterable) value) {
				list.add(convert(obj, entity));
			}
			if (list.isEmpty() || list.size() == 1 && list.get(0) == null) return null;
			return list;
		}

		return value;
	}

	/**
	 * 克隆当前对象
	 * 
	 * @return
	 */
	public JSONConverterSupport clone() {
		JSONConverterSupport converter;
		try {
			converter = (JSONConverterSupport) super.clone();
		} catch (CloneNotSupportedException e) {
			converter = new JSONConverterSupport();
			converter.datePattern = datePattern;
			converter.maxDepth = maxDepth;
			converter.depth = depth;
		}
		converter.internal = null;
		return converter;
	}

	public void setExcludeMethods(String excludeMethods) {
		this.excludeMethods.addAll(TextParseUtil.commaDelimitedStringToSet(excludeMethods));
	}

	public void addExcludeMethods(String excludeMethods) {
		this.excludeMethods.addAll(TextParseUtil.commaDelimitedStringToSet(excludeMethods));
	}

	public void addExcludeMethods(Set<String> excludeMethods) {
		this.excludeMethods.addAll(excludeMethods);
	}

	public void addExcludeMethod(String excludeMethod) {
		this.excludeMethods.add(excludeMethod);
	}

	public Set<String> getExcludeMethodsSet() {
		return excludeMethods;
	}

	public void addIncludeMethod(String includeMethod) {
		this.includeMethods.add(includeMethod);
	}

	public void setIncludeMethods(String includeMethods) {
		this.includeMethods.addAll(TextParseUtil.commaDelimitedStringToSet(includeMethods));
	}

	public void addIncludeMethods(String includeMethods) {
		this.includeMethods.addAll(TextParseUtil.commaDelimitedStringToSet(includeMethods));
	}

	public void addIncludeMethods(Set<String> includeMethods) {
		this.includeMethods.addAll(includeMethods);
	}

	public Set<String> getIncludeMethodsSet() {
		return includeMethods;
	}

	public void addExcludeProperties(String excludeProperties) {
		this.excludeProperties.addAll(TextParseUtil.commaDelimitedStringToSet(excludeProperties));
	}

	public void addExcludeProperties(Set<String> excludeProperties) {
		this.excludeProperties.addAll(excludeProperties);
	}

	public void addExcludeProperty(String excludeProperty) {
		this.excludeProperties.add(excludeProperty);
	}

	public Set<String> getExcludePropertiesSet() {
		return excludeProperties;
	}

	public void addIncludeProperty(String includeProperty) {
		this.includeProperties.add(includeProperty);
	}

	public void setIncludeProperties(String includeProperties) {
		this.includeProperties.addAll(TextParseUtil.commaDelimitedStringToSet(includeProperties));
	}

	public void addIncludeProperties(String includeProperties) {
		this.includeProperties.addAll(TextParseUtil.commaDelimitedStringToSet(includeProperties));
	}

	public void addIncludeProperties(Set<String> includeProperties) {
		this.includeProperties.addAll(includeProperties);
	}

	public Set<String> getIncludePropertiesSet() {
		return includeProperties;
	}

}
