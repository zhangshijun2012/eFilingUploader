package com.sinosoft.util;

import java.util.Properties;

public class ContentTypeHelper {

	/**
	 * 所有文件类型与contentype的对应关系
	 */
	public static final Properties CONTENT_TYPES = new Properties();

	static {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			CONTENT_TYPES.load(classLoader.getResourceAsStream("contentTypes.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取key对应的类型
	 * 
	 * @param key 主要指文件后缀名
	 * @return
	 */
	public static final String get(String key) {
		if (!StringHelper.isEmpty(key) && key.charAt(0) != '.') key = '.' + key;
		return key == null ? "" : StringHelper.trim(CONTENT_TYPES.get(key.toLowerCase()));
	}

	public static void main(String[] args) {
		System.out.println(get(".gif"));
	}
}
