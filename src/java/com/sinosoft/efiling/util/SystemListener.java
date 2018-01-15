package com.sinosoft.efiling.util;

import java.util.Properties;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sinosoft.efiling.hibernate.dao.ConfigureDao;
import com.sinosoft.filenet.FileIndexService;
import com.sinosoft.filenet.FileNetHelper;
import com.sinosoft.util.timer.TimerScheduler;

public class SystemListener implements ServletContextListener, ServletContextAttributeListener {
	private static final Logger logger = LoggerFactory.getLogger(SystemListener.class);

	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	// public static final String LOGS_ROOT_KEY = "Log4j.ROOT";

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		logger.debug("系统启动!");
		// 设置系统编码为utf-8
		// System.setProperty("file.encoding", "utf-8");
		// 系统主目录
		SystemUtils.setServerHome(servletContextEvent.getServletContext());
		logger.debug("系统主目录:[" + SystemUtils.getServerHome() + "]");


		/* 初始化Spring工厂 */
		SpringUtils.initialize(servletContextEvent.getServletContext());

		SystemUtils.initialize();

		logger.debug("初始化FileNet配置!");
		if (!FileNetHelper.isInitialized()) {
			// 初始化FileNet的配置数据
			ConfigureDao configureDao = SpringUtils.getBean(ConfigureDao.class);
			Properties props = configureDao.getProperties(FileIndexService.CONFIGURE_TYPE_FILE_NET);
			logger.debug(props.toString());
			FileNetHelper.initialize(props);
		}

		/* 启动定时器 */
		TimerScheduler.start();
	}

	public void attributeAdded(ServletContextAttributeEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void attributeRemoved(ServletContextAttributeEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void attributeReplaced(ServletContextAttributeEvent arg0) {
		// TODO Auto-generated method stub

	}

}
