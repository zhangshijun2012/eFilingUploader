package com.sinosoft.efiling.util;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Spring工具类,可供非spring托管的对象调用spring的对象
 * 
 * @author LuoGang
 * 
 */
public class SpringUtils {
	/** Spring的context对象 */
	private static WebApplicationContext context;

	/**
	 * 初始化Spring工厂对象
	 * 
	 * @param servletContext
	 */
	public static void initialize(ServletContext servletContext) {
		context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
	}

	/**
	 * 返回id=name的Spring托管对象
	 * 
	 * @param name 对象id
	 * @return
	 * @see org.springframework.beans.factory.BeanFactory#getBean(String)
	 */
	public static Object getBean(String name) {
		return context.getBean(name);
	}

	/**
	 * 返回类为cls的Spring托管对象
	 * 
	 * @param cls
	 * @return
	 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
	 */
	public static <K> K getBean(Class<K> cls) {
		return context.getBean(cls);
	}

	/**
	 * 得到工厂对象
	 * 
	 * @return
	 */
	public static WebApplicationContext getBeanFactory() {
		return context;
	}
}
