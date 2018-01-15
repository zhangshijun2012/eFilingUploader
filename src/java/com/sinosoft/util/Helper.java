package com.sinosoft.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;

public class Helper {
	/**
	 * 将value转为字符串,并去除前后空格
	 * 
	 * @param value
	 * @return
	 */
	public static String trim(Object value) {
		if (value == null) return "";
		if (value instanceof String) return ((String) value).trim();
		return value.toString().trim();
	}

	/**
	 * 判断对象o是否为空
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(Object value) {
		if (value == null) return true;

		if (value.getClass().isArray()) {
			return Array.getLength(value) == 0;
		} else if (value instanceof Collection) {
			return ((Collection<?>) value).isEmpty();
		} else if (value instanceof Map) {
			return ((Map<?, ?>) value).isEmpty();
		} else if (value instanceof Iterable) {
			return !((Iterable<?>) value).iterator().hasNext();
		} else if (value instanceof String) {
			return ((String) value).trim().length() == 0;
		}

		return StringHelper.isEmpty(value.toString());
	}

	/**
	 * 转换o的类型
	 * 
	 * @param cls
	 * @param o
	 * @return
	 */
	public static Object cast(Class<?> cls, Object o) {
		if (o == null) {
			return null;
		}
		if (cls == null) {
			return o;
		}

		if (cls == o.getClass()
				|| cls.isAssignableFrom(o.getClass())
				|| (cls.isArray() && o.getClass().isArray() && cls.getComponentType() == o.getClass()
						.getComponentType())) return o;

		if (cls == Byte.TYPE || cls == Byte.class) {
			return (byte) NumberHelper.intValue(o);
		} else if (cls == Short.TYPE || cls == Short.class) {
			return ((short) NumberHelper.intValue(o));
		} else if (cls == Integer.TYPE || cls == Integer.class) {
			return (NumberHelper.intValue(o));
		} else if (cls == Long.TYPE || cls == Long.class) {
			return (NumberHelper.longValue(o));
		} else if (cls == Float.TYPE || cls == Float.class) {
			return (NumberHelper.floatValue(o));
		} else if (cls == Double.TYPE || cls == Double.class) {
			return (NumberHelper.doubleValue(o));
		} else if (cls == Boolean.class || cls == Boolean.TYPE) {
			return (Boolean) o;
		} else if (cls == String.class) {
			return StringHelper.trim(o);
		} else if (cls == Date.class) {
			o = DateHelper.parse(o.toString());
			// o = cls. (o);
		} else if (cls.isArray() && o.getClass().isArray() && cls.getComponentType() != o.getClass().getComponentType()) {
			int l = ((Object[]) o).length;
			String clazz = cls.getComponentType().toString();
			Object array = Array.newInstance(cls.getComponentType(), l);
			for (int i = 0; i < l; i++) {
				if (clazz.equals("double")) {
					((double[]) array)[i] = NumberHelper.doubleValue(cast(cls.getComponentType(), ((Object[]) o)[i]));
				} else if (clazz.equals("float")) {
					((float[]) array)[i] = NumberHelper.floatValue(cast(cls.getComponentType(), ((Object[]) o)[i]));
				} else if (clazz.equals("long")) {
					((long[]) array)[i] = NumberHelper.longValue(cast(cls.getComponentType(), ((Object[]) o)[i]));
				} else if (clazz.equals("int")) {
					((int[]) array)[i] = NumberHelper.intValue(cast(cls.getComponentType(), ((Object[]) o)[i]));
				} else if (clazz.equals("short")) {
					((short[]) array)[i] = (short) NumberHelper
							.intValue(cast(cls.getComponentType(), ((Object[]) o)[i]));
				} else if (clazz.equals("byte")) {
					((byte[]) array)[i] = (byte) NumberHelper.intValue(cast(cls.getComponentType(), ((Object[]) o)[i]));
				} else {
					((Object[]) array)[i] = cast(cls.getComponentType(), ((Object[]) o)[i]);
				}
			}
			o = array;
		} else if (cls.isArray() && !o.getClass().isArray()) {
			Object[] array = (Object[]) Array.newInstance(cls.getComponentType(), 1);
			array[0] = cast(cls.getComponentType(), o);
			o = array;
		}
		return o;
	}

	/**
	 * 
	 * 将source的属性复制到target中,
	 * 向target赋值时,会调用setter方法
	 * 
	 * @param source
	 * @param target
	 * @return target
	 * @throws BeansException
	 * @see {@link BeanUtils#copyProperties(Object, Object)}
	 */
	public static Object copy(Object source, Object target) throws BeansException {
		if (source == null || target == null) return target;
		BeanUtils.copyProperties(source, target);
		return target;
	}

	/**
	 * 
	 * 将source的属性复制到target中,
	 * 向target赋值时,会调用setter方法
	 * 
	 * @param source
	 * @param target
	 * @param ignoreProperties 忽略的属性名,target中在此数组中的属性名不会被source的值覆盖
	 * @return target
	 * @throws BeansException
	 * @see {@link BeanUtils#copyProperties(Object, Object, String[])}
	 */
	public static Object copy(Object source, Object target, String[] ignoreProperties) throws BeansException {
		if (source == null || target == null) return target;
		BeanUtils.copyProperties(source, target, ignoreProperties);
		return target;
	}

	/**
	 * 
	 * 将source的不为null的属性复制到target中
	 * 向target赋值时,会调用setter方法
	 * 
	 * @param source
	 * @param target
	 * @return target
	 * @throws BeansException
	 */
	public static Object copyValues(Object source, Object target) throws BeansException {
		return copyValues(source, target, null);
	}

	/**
	 * 将source的不为null的属性复制到target中
	 * 向target赋值时,会调用setter方法
	 * 
	 * @param source
	 * @param target
	 * @param ignoreProperties 忽略的属性名,target中在此数组中的属性名不会被source的值覆盖
	 * 
	 * @return target
	 * @throws BeansException
	 * @see {@link BeanUtils#copyProperties(Object, Object, String[])}
	 */
	public static Object copyValues(Object source, Object target, String[] ignoreProperties) throws BeansException {
		if (source == null || target == null) return target;
		// Assert.notNull(source, "Source must not be null");
		// Assert.notNull(target, "Target must not be null");

		Class<?> actualEditable = target.getClass();

		PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);
		List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;

		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() != null
					&& (ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {
				PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(source);
						if (value == null) continue; // 如果得到的值为null则不复制
						Method writeMethod = targetPd.getWriteMethod();
						if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
							writeMethod.setAccessible(true);
						}
						writeMethod.invoke(target, value);
					} catch (Exception ex) {
						throw new FatalBeanException("Could not copy properties from source to target", ex);
					}
				}
			}
		}
		return target;
	}

	/**
	 * 在source中取得属性propertyName的值
	 * 
	 * @param source
	 * @param propertyName
	 * @return
	 */
	public static Object getValue(Object source, String propertyName) {
		if (source == null || Helper.isEmpty(propertyName)) return null;
		PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), propertyName);
		if (sourcePd != null && sourcePd.getReadMethod() != null) {
			try {
				Method readMethod = sourcePd.getReadMethod();
				if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
					readMethod.setAccessible(true);
				}
				Object value = readMethod.invoke(source);
				return value;
			} catch (Exception ex) {
				// throw new FatalBeanException("Could not get value from source", ex);
			}
		}
		return null;
	}

	/**
	 * 
	 * 更改target中属性propertyName的值为value
	 * 
	 * @param target
	 * @param propertyName
	 * @param value
	 * @return 是否成功
	 */
	public static boolean setValue(Object target, String propertyName, Object value) {
		if (target == null || Helper.isEmpty(propertyName)) return false;
		PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(target.getClass(), propertyName);
		if (sourcePd != null && sourcePd.getWriteMethod() != null) {
			try {
				Method writeMethod = sourcePd.getWriteMethod();
				if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
					writeMethod.setAccessible(true);
				}
				writeMethod.invoke(target, value);
				return true;
			} catch (Exception ex) {
				// throw new FatalBeanException("Could not get value from source", ex);
			}
		}
		return false;
	}

	/**
	 * 将数组复制为一个Object[]数组
	 * 
	 * @param array
	 * @return
	 */
	public static Object[] toObjectArray(Object array) {
		if (array == null) return null;
		int length = Array.getLength(array);
		Object[] dest = new Object[length];
		for (int i = 0; i < length; i++) {
			dest[0] = Array.get(array, i);
		}
		return dest;
	}

	/**
	 * 复制数组
	 * 
	 * @param array
	 * @return
	 */
	public static Object copyArray(Object array) {
		if (array == null) return null;
		int length = Array.getLength(array);
		Object dest = Array.newInstance(array.getClass().getComponentType(), length);
		System.arraycopy(array, 0, dest, 0, length);
		return dest;
	}

	/**
	 * 在source中查找target
	 * 
	 * @param source 可以为数组或列表
	 * @param target 要查找的对象
	 * @return target 所在的位置
	 */
	public static int indexOf(Object source, Object target) {
		if (source instanceof List) return ((List<?>) source).indexOf(target);
		if (source instanceof Iterable) return indexOf(((Iterable<?>) source).iterator(), target);
		if (source instanceof Iterator) { // 迭代对象的处理
			int index = 0;
			Object o;
			while (((Iterator<?>) source).hasNext()) {
				o = ((Iterator<?>) source).next().equals(target);
				if (o == target || o != null && o.equals(target)) return index;
				index++;
			}
		}
		if (source.getClass().isArray()) { // 数组的处理
			int length = Array.getLength(source);
			int index = 0;
			Object o;
			while (index < length) {
				o = Array.get(source, index);
				if (o == target || o != null && o.equals(target)) return index;
				index++;
			}
		}

		return -1;
	}

	/**
	 * source中是否包含target
	 * 
	 * @param source 可以为数组或列表
	 * @param target 要查找的对象
	 * @return true在source中找到target对象，否则为false
	 */
	public static boolean contains(Object source, Object target) {
		if (source instanceof List) return ((List<?>) source).contains(target);

		return Helper.indexOf(source, target) > -1;
	}
}
