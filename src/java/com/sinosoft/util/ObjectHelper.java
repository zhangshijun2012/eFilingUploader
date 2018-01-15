package com.sinosoft.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class ObjectHelper {
	/**
	 * 判断对象o是否为空
	 * 
	 * @param o
	 * @return
	 */
	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}

		if (o.getClass().isArray()) {
			return Array.getLength(o) <= 0;
		} else if (o instanceof Collection) {
			return ((Collection<?>) o).isEmpty();
		} else if (o instanceof Map) {
			return ((Map<?, ?>) o).isEmpty();
		} else if (o instanceof Iterable) {
			return !((Iterable<?>) o).iterator().hasNext();
		}

		return StringHelper.isEmpty(o.toString());
	}

	/**
	 * 得到cls的属性name
	 * 
	 * @param cls
	 * @param name
	 * @return
	 */
	public static Field getField(Class<?> cls, String name) {
		if (cls == null || name == null) {
			return null;
		}

		Field field = null;
		try {
			try {
				field = cls.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
			}
			// 如果没有则检测其父类是否有该属性
			if (field == null) {
				field = getField(cls.getSuperclass(), name);
			}

			if (field == null) {
				// 依然没有，则检测接口
				Class<?>[] clsInterfaces = cls.getInterfaces();
				int l = (clsInterfaces == null) ? 0 : clsInterfaces.length;
				for (int i = 0; i < l && field == null; i++) {
					field = getField(clsInterfaces[i], name);
				}
			}

		} catch (SecurityException e) {
			// TODO 自动生成 catch 块
			// e.printStackTrace();
		}
		return field;
	}

	public static Field getField(Object o, String name) {
		return (o == null || name == null) ? null : getField(o.getClass(), name);
	}

	public static Object getFieldValue(Object o, String name) {
		Field field = getField(o, name);
		return getFieldValue(o, field);
	}

	/**
	 * 得到o的name属性的值
	 * 
	 * @param o
	 * @param name
	 * @return
	 */
	public static Object getFieldValue(Object o, Field field) {
		if (o != null && field != null) {
			field.setAccessible(true);
			try {
				return field.get(o);
			} catch (IllegalArgumentException e) {
				// TODO 自动生成 catch 块
				// e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO 自动生成 catch 块
				// e.printStackTrace();
			}
		}
		return null;
		// return doMethod(field, "get", new Object[] { o });
	}

	public static void setFieldValue(Object o, String fieldName, Object value) {
		Field field = getField(o, fieldName);
		ObjectHelper.setFieldValue(o, field, value);

	}

	/**
	 * 更改o的name属性的值为value
	 * 
	 * @param o
	 * @param name
	 * @param value
	 * @return
	 */
	public static void setFieldValue(Object o, Field field, Object value) {
		if (o != null && field != null) {
			field.setAccessible(true);
			try {
				field.set(o, cast(field.getType(), value));
				// System.out.println(getFieldValue(o, name));
			} catch (IllegalArgumentException e) {
				// TODO 自动生成 catch 块
				// e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO 自动生成 catch 块
				// e.printStackTrace();
			}
		}
		// return doMethod(field, "set", new Object[] { o, value });
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
						.getComponentType()))
			return o;

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
			return StringHelper.parseBoolean(o);
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
}
