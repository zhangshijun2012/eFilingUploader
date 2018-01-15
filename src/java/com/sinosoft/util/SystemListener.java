package com.sinosoft.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SystemListener implements ServletContextListener {
	/** 日志目录关键字 */
	public static final String LOGGER_ROOT_KEY = "logger.root.uploader";
	static {
		// log4j的目录,卸载静态模块中才能保证比log4j的加载先执行,写在contextInitialized方法中会在log4j初始化后才执行
		String loggerRoot;
		try {
			loggerRoot = URLDecoder.decode(SystemListener.class.getResource("/").getFile(), "UTF-8");
			File file = new File(loggerRoot);
			file = new File(file.getParentFile(), "logs");
			loggerRoot = file.getAbsolutePath();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			loggerRoot = "";
		}
		System.setProperty(LOGGER_ROOT_KEY, loggerRoot);
		System.out.println(DateHelper.now() + "\t日志目录logs:" + loggerRoot);

		/* 设置默认的国际化语言为中文 */
		Locale.setDefault(Locale.CHINA);
		System.out.println("defaultLocale=" + Locale.CHINA);
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void contextInitialized(ServletContextEvent servletContextEvent) {
	}

}
