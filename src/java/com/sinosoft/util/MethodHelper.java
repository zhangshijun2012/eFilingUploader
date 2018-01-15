package com.sinosoft.util;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodHelper {
	/**
	 * 得到一个方法
	 * 
	 * @param cls
	 * @param methodName
	 * @param parameterTypes 参数类型那个.注意对应基本类型如long,请使用Long.TYPE,不能用Long.class
	 * @return
	 */
	public static Method get(Class<?> cls, String methodName, Class<?>[] parameterTypes) {
		try {
			return (cls == null || methodName == null) ? null : cls.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			// TODO 自动生成 catch 块
		} catch (NoSuchMethodException e) {
			// TODO 自动生成 catch 块
		}
		return null;
	}

	public static Method get(Class<?> cls, String methodName) {
		return get(cls, methodName, null);
	}

	public static Method get(Object o, String methodName, Class<?>[] parameterTypes) {
		return (o == null || methodName == null) ? null : get(o.getClass(), methodName, parameterTypes);
	}

	public static Method get(Object o, String methodName) {
		return get(o, methodName, null);
	}

	/**
	 * 执行对象o的method方法
	 * 
	 * @param o
	 * @param method
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object invoke(Object o, Method method, Object[] args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (o == null) throw new NullPointerException("要执行方法的对象不能为null");
		if (method == null) throw new NullPointerException("要执行的方法不能为null");
		return method.invoke(o, args);
	}

	/**
	 * 执行对象o的名称为methodName的方法
	 * 
	 * @param o
	 * @param methodName 方法名称
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object invoke(Object o, String methodName, Object[] args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (o == null) throw new NullPointerException("要执行方法的对象不能为null");
		if (StringHelper.isEmpty(methodName)) throw new NullPointerException("要执行的方法不能为空");
		int l = 0;
		Class<?>[] cls = null;
		if (args != null && (l = args.length) > 0) {
			cls = new Class[l];
			for (int i = 0; i < l; i++) {
				cls[i] = (args[i] == null ? null : args[i].getClass());
			}

		}
		return invoke(o, get(o.getClass(), methodName, cls), args);
	}

	public static Object invoke(Object o, String methodName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return invoke(o, methodName, null);
	}

	public static void close(Object o) {
		if (o == null) return;
		try {
			if (o instanceof Closeable) {
				((Closeable) o).close();
			} else {
				invoke(o, "close");
			}
		} catch (Exception e) {
			// TODO 自动生成 catch 块
			// e.printStackTrace();
		}
	}

}
