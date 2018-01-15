/**
 * 
 */
package com.sinosoft.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Field的一些操作工具
 * 
 * @author LuoGang
 * 
 */
public class FieldHelper {
	/**
	 * 得到cls的所有属性，包括父类和接口定义的属性
	 * 
	 * @param cls 要检查的类
	 * @param superCls 当递归到类superCls时结束.不包含superCls的属性
	 * @param containSuper 是否包含superCls的属性
	 * @return
	 */
	public static Field[] getAllFields(Class<?> cls, Class<?> superCls, boolean containSuper) {
		if (cls == null || (cls == superCls && !containSuper)) {
			return null;
		}

		List<Field> list = new ArrayList<Field>();
		Field[] fields = cls.getDeclaredFields();
		CollectionHelper.add(list, fields);
		int l = 0;
		if (cls != superCls) {

			// if (cls != superCls) {
			// 检查父类的属性
			if (cls.getSuperclass() != null && cls.getSuperclass() != Object.class
					&& (containSuper || cls.getSuperclass() != superCls)) {
				CollectionHelper.add(list, getAllFields(cls.getSuperclass(), superCls, containSuper));
			}

			// 则检测接口的属性
			Class<?>[] clsInterfaces = cls.getInterfaces();
			l = (clsInterfaces == null) ? 0 : clsInterfaces.length;
			for (int i = 0; i < l; i++) {
				CollectionHelper.add(list, getAllFields(clsInterfaces[i], superCls, containSuper));
			}
		}
		// }

		l = list.size();
		if (l <= 0) {
			return new Field[0];
		}
		fields = new Field[l];
		for (int i = 0; i < l; i++) {
			fields[i] = list.get(i);
		}
		return fields;
	}

	/**
	 * 得到cls的所有属性,包括继承的属性
	 * 
	 * @param cls
	 * @return
	 */
	public static Field[] getAllFields(Class<?> cls) {
		return getAllFields(cls, null, true);

	}

	/**
	 * 得到o的所有属性，包括继承的的属性
	 * 
	 * @param o
	 * @return
	 */
	public static Field[] getAllFields(Object o) {
		return o == null ? null : getAllFields(o.getClass());
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
			e.printStackTrace();
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
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
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
				field.set(o, ObjectHelper.cast(field.getType(), value));
				// System.out.println(getFieldValue(o, name));
			} catch (IllegalArgumentException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}
		// return doMethod(field, "set", new Object[] { o, value });
	}

	/**
	 * 将src中与dest相同的数据的值复制到dest中.
	 * 
	 * @param src
	 * @param dest
	 * @param exception 不需要复制的字段,可使用正则表达式
	 * @param exceptionNull 是否将exceptionNull中字段设为null
	 * @param copyNull 是否复制src中的null值
	 * @param setNull 是否将在src中没有的字段设为null值
	 */
	public static void copy(Object src, Object dest, String[] exception, boolean exceptionNull, boolean copyNull,
			boolean setNull) {
		Field[] destFields = getAllFields(dest);
		Field destField = null;
		Field srcField = null;
		Object value = null;
		for (int i = 0, l = destFields.length; i < l; i++) {
			destField = destFields[i];
			if (Modifier.isStatic(destField.getModifiers()) // 静态字段
					|| Modifier.isFinal(destField.getModifiers())) { // final字段
				// 该字段需要保留
				continue;
			}
			if (StringHelper.match(destField.getName(), exception)) {
				// 不需要复制的字段
				if (exceptionNull) {
					// 需要设置为null
					setFieldValue(dest, destField, null);
				}
				continue;
			}

			srcField = getField(src, destFields[i].getName());
			if (srcField != null && !destField.getType().isAssignableFrom(srcField.getType())) {
				// destField 与 srcField的类型不一致，且不是其超类或者接口
				srcField = null;
			}
			if (srcField == null) {
				// 源对象中没有此属性
				if (setNull) {
					// 需要将此字段设为null
					setFieldValue(dest, destField, null);
				}
				continue;
			}

			value = getFieldValue(src, srcField);
			if (value == null && !copyNull) {
				continue;
			}

			setFieldValue(dest, destField, value);
		}
	}

	/**
	 * 
	 * 复制src中与dest一致的属性字段,不包括名字在exception中的字段,忽略不一致的
	 * 
	 * @param src
	 * @param dest
	 * @param exception
	 * @see #copy(Object, Object, String[], boolean, boolean, boolean)
	 */
	public static void copy(Object src, Object dest, String[] exception) {
		// copy(src, dest, exception, false, true, false);
		Helper.copy(src, dest, exception);
	}

	/**
	 * 复制src中与dest一致的属性字段,忽略不一致的
	 * 
	 * @param src
	 * @param dest
	 * @see #copy(Object, Object, String[], boolean, boolean, boolean)
	 */
	public static void copy(Object src, Object dest) {
		// copy(src, dest, null, false, true, false);
		Helper.copy(src, dest);
	}

}
