package com.sinosoft.filenet;

import java.util.Properties;

import com.filenet.api.core.Connection;
import com.filenet.api.util.UserContext;
import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.SystemHelper;

public class FileNetHelper {
	/** 存放的属性 */
	public static final Properties props = SystemHelper.getProperties("filenet.cfg.properties"); // new Properties();
	// static {
	// try {
	// // 加载根目录下的配置文件
	// ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	// props.load(classLoader.getResourceAsStream("filenet.cfg.properties"));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/** filenet连接地址 */
	public static String URI = props.getProperty("filenet.uri");
	/** 用户名 */
	public static String USER = props.getProperty("filenet.user");
	/** 密码 */
	public static String PASSWORD = props.getProperty("filenet.password");
	/**
	 * optionalJAASStanzaName
	 * 
	 * @see UserContext#createSubject(Connection, String, String, String)
	 */
	public static String STANZA = props.getProperty("filenet.stanza");
	/** domain名称,可以为null */
	public static String DOMAIN = props.getProperty("filenet.domain");
	/** ObjectStore对象名称 */
	public static String OBJECT_STORE = props.getProperty("filenet.objectStore");
	/** FileNet的Document的Class ID */
	public static String DOCUMENT_CLASS = StringHelper.trim(props.getProperty("filenet.document.class"));
	/** 默认使用的FileNet根目录,不能为null */
	public static String ROOT = StringHelper.trim(props.getProperty("filenet.root"));

	/** Socket上传的主机地址 */
	public static String SOCKET_HOST = props.getProperty("filenet.socket.host");
	/** Socket上传的主机端口 */
	public static int SOCKET_PORT = NumberHelper.intValue(props.getProperty("filenet.socket.port"));
	/** Socket上传的主机端口 */
	public static int SOCKET_PORT_UPLOAD = NumberHelper.intValue(props.getProperty("filenet.socket.port.upload"));
	/** Socket下载的主机端口 */
	public static int SOCKET_PORT_DOWNLOAD = NumberHelper.intValue(props.getProperty("filenet.socket.port.download"));

	static {
		read(props);
	}

	/** 是否已经初始化,主要用于标记是否从数据库中读取数据 */
	private static boolean initialized = false;

	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * 使用props对当前属性进行初始化.
	 * 
	 * @param props 从数据库中读取的数据
	 */
	public static void initialize(Properties props) {
		if (initialized) return;
		FileNetHelper.props.putAll(props);
		read(FileNetHelper.props);
		initialized = true;
	}

	/**
	 * 使用props对当前属性进行初始化.
	 * 
	 * @param props 从数据库中读取的数据
	 * @param refresh 是否强制重新读取数据
	 */
	public static void initialize(Properties props, boolean refresh) {
		initialized = !refresh;
		initialize(props);
	}

	/**
	 * 读取props中的属性
	 * 
	 * @param props
	 */
	public static void read(Properties props) {
		// System.out.println("props=" + props);
		/* filenet连接地址 */
		URI = props.getProperty("filenet.uri");
		/* 用户名 */
		USER = props.getProperty("filenet.user");
		/* 密码 */
		PASSWORD = props.getProperty("filenet.password");
		STANZA = props.getProperty("filenet.stanza");
		/* domain名称,可以为null */
		DOMAIN = props.getProperty("filenet.domain");
		/* ObjectStore对象名称 */
		OBJECT_STORE = props.getProperty("filenet.objectStore");
		/* FileNet的Document的Class ID */
		DOCUMENT_CLASS = StringHelper.trim(props.getProperty("filenet.document.class"));
		/* 默认使用的FileNet根目录,不能为null */
		ROOT = StringHelper.trim(props.getProperty("filenet.root"));

		/* Socket上传的主机地址 */
		SOCKET_HOST = props.getProperty("filenet.socket.host");
		/* Socket上传的主机端口 */
		SOCKET_PORT = NumberHelper.intValue(props.getProperty("filenet.socket.port"));
		/* Socket上传的主机端口 */
		SOCKET_PORT_UPLOAD = NumberHelper.intValue(props.getProperty("filenet.socket.port.upload"));
		/* Socket下载的主机端口 */
		SOCKET_PORT_DOWNLOAD = NumberHelper.intValue(props.getProperty("filenet.socket.port.download"));
	}
}
