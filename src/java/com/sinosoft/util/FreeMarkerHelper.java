package com.sinosoft.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * 使用FreeMarkert进行模板文件的转换
 * 
 * @author LuoGang
 * 
 */
public class FreeMarkerHelper {
	/** FreeMarker的配置对象 */
	private static Configuration configuration;

	static {
		// 默认配置
		try {
			// ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			String dir = SystemHelper.getProperty("freemarker.template");
			if (Helper.isEmpty(dir)) {
				// 默认的模板目录为ftl目录
				dir = URLDecoder.decode(FreeMarkerHelper.class.getResource("/ftl").getFile(),
						SystemHelper.ENCODING_UTF_8);
			}
			configuration = configure(new File(dir));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Configuration getDefaultConfiguration() {
		return configuration;
	}

	/**
	 * 设定配置文件目录
	 * 
	 * @param dir
	 * @return
	 */
	public static Configuration configure(File dir) {
		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding(SystemHelper.ENCODING);
		try {
			configuration.setDirectoryForTemplateLoading(dir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		configuration.setObjectWrapper(new DefaultObjectWrapper());
		return configuration;
	}

	/**
	 * 处理模板数据
	 * 
	 * @param name 模板文件名
	 * @param rootMap 数据
	 * @param out 输出流
	 * @throws CustomException
	 */
	public static void process(String name, Object rootMap, Writer out) throws CustomException {
		try {
			Template template = configuration.getTemplate(name);
			template.setEncoding(SystemHelper.ENCODING);
			template.process(rootMap, out);
		} catch (Exception e) {
			// TODO
			throw new CustomException(e);
		}
	}

	/**
	 * 得到模板数据
	 * 
	 * @param name 模板文件名
	 * @param rootMap 数据
	 * @return 转换后的文件内容
	 */
	public static String get(String name, Object rootMap) {
		StringWriter out = new StringWriter();
		try {
			Template template = configuration.getTemplate(name);
			template.process(rootMap, out);
		} catch (Exception e) {
			// TODO
			// throw new CustomException(e);
			return null;
		}
		return out.toString();
	}
}
