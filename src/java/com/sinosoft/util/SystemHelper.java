package com.sinosoft.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.util.ConfigHelper;

/**
 * 系统设置
 * 
 * @author LuoGang
 */
public class SystemHelper {
	public static final Logger logger = Logger.getLogger(SystemHelper.class);

	/** 加载过的所有配置 */
	static final Map<String, Properties> caches = new HashMap<String, Properties>();

	/** 所有配置属性 */
	public static final Properties props = getProperties("config.properties");// new Properties();

	/**
	 * 读取根目录下的配置文件
	 * 
	 * @param file 文件名
	 * @return
	 */
	public static final Properties getProperties(String resource) {
		Properties props = caches.get(resource); // 读取缓存
		if (props != null) return props;
		props = new Properties();
		InputStream in = ConfigHelper.getResourceAsStream(resource);
		if (in == null) {
			logger.warn("资源:" + resource + "没有找到!");
			return props;
		}
		try {
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("资源:" + resource + "加载异常!", e);
			return props;
		} finally {
			MethodHelper.close(in);
		}
		String include = props.getProperty("include");
		if (StringHelper.isEmpty(include)) return props;

		// 读取include的文件
		String[] includes = include.split(",");
		for (String res : includes) {
			if (!StringHelper.isEmpty(res)) {
				props.putAll(getProperties(res.trim()));
			}
		}

		return props;
	}

	/**
	 * 得到key对应的配置
	 * 
	 * @param key
	 * @return
	 */
	public static final String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * 得到key的配置
	 * 
	 * @param key
	 * @param defaultValue 如果没有key对应的数据,则返回此默认值
	 * @return
	 */
	public static final String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	/**
	 * 回车
	 */
	public static final String ENTER = "\r\n";

	/**
	 * ascii编码
	 */
	public static final String ENCODING_ASCII = "ascii";

	/**
	 * big5编码
	 */
	public static final String ENCODING_BIG5 = "big5";

	/**
	 * gbk编码
	 */
	public static final String ENCODING_GBK = "gbk";

	/**
	 * gb2312编码
	 */
	public static final String ENCODING_GB2312 = "gb2312";

	/**
	 * utf-8编码
	 */
	public static final String ENCODING_UTF_8 = "utf-8";

	/**
	 * iso-8859-1编码
	 */
	public static final String ENCODING_ISO_8859_1 = "iso-8859-1";

	/**
	 * 系统使用的默认编码.未指定则使用utf-8
	 */
	public static final String ENCODING = StringHelper.trim(getProperty("encoding"), ENCODING_UTF_8);

	/**
	 * 执行操作命令
	 * 
	 * @return [0: 退出代码,0为正常退出,1:执行中的输出信息]
	 */
	public static final Object[] execute(List<String> command) {
		if (ObjectHelper.isEmpty(command)) return null;
		Process process = null;
		StringBuffer message = new StringBuffer();
		List<Serializable> results = new ArrayList<Serializable>();
		ProcessBuilder builder = null;
		BufferedReader in = null;
		try {
			builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			process = builder.start();
			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				// System.out.println(line);
				message.append(line).append(System.getProperty("line.separator"));
			}
			process.waitFor();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (process != null) process.destroy();
		}
		process.destroy();
		results.add(process.exitValue());
		results.add(message);

		// System.out.println("process.exitValue():" + process.exitValue());
		return results.toArray();
	}

	public static final Object[] execute(String[] command) {
		if (ObjectHelper.isEmpty(command)) return null;
		List<String> c = new ArrayList<String>();
		CollectionHelper.add(c, command);
		return execute(c);
	}

}
