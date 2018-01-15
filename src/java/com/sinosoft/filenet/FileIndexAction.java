package com.sinosoft.filenet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sinosoft.efiling.util.SystemUtils;
import com.sinosoft.efiling.util.UserSessionEntity;
import com.sinosoft.filenet.handler.DefaultFileHandler;
import com.sinosoft.filenet.handler.EFilingHandler;
import com.sinosoft.util.ContentTypeHelper;
import com.sinosoft.util.DateHelper;
import com.sinosoft.util.FileHelper;
import com.sinosoft.util.Helper;
import com.sinosoft.util.MethodHelper;
import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.SystemHelper;
import com.sinosoft.util.image.ImageHelper;
import com.sinosoft.util.image.TIFFHelper;
import com.sinosoft.util.json.JSONArray;
import com.sinosoft.util.json.JSONObject;
import com.sinosoft.util.struts2.action.EntityActionSupport;

/**
 * 用于文件上传下载的Action
 * 
 * @author LuoGang
 * 
 */
public class FileIndexAction extends EntityActionSupport<FileIndex, FileIndexDao, FileIndexService, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5528252747335427780L;

	/** 日志记录对象 */
	public static final Logger logger = LoggerFactory.getLogger(FileIndexAction.class);
	/**
	 * 所有暂存的文件。主要用与SWFUpload控件上传.Map的key通过前端传入
	 */
	private static final Map<String, List<FileEntry>> FILES = new HashMap<String, List<FileEntry>>();

	/** 上传文件时存放的临时目录 */
	private static String DIRECTORY = SystemHelper.getProperty("fileindex.uploder.root");
	static {
		if (StringHelper.isEmpty(DIRECTORY)) {
			try {
				DIRECTORY = URLDecoder.decode(FileIndexAction.class.getResource("").getFile(), "utf-8");
				File file = new File(DIRECTORY);
				if (file.isFile()) DIRECTORY = file.getParent();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				DIRECTORY = "";
			}
		}

		File dir = new File(DIRECTORY);
		logger.debug("文件上传临时目录:" + dir.getAbsolutePath());
	}

	public void validateUpload() {
		if (Helper.isEmpty(files)) {
			// 调用upload方法必须传入batch参数
			addActionError("请上传文件");
		}
	}

	/**
	 * 进行上传文件,暂时不存入数据库中.上传的文件存入FILES对象中.
	 * 
	 * @return
	 */
	public String upload() {
		int i = 0;
		List<FileEntry> list = null;
		if (StringHelper.isEmpty(batch)) {
			list = new ArrayList<FileEntry>();
		} else {
			list = FILES.get(batch);
			if (list == null) {
				list = new ArrayList<FileEntry>();
				FILES.put(batch, list);
			}
		}
		if (entity == null) entity = new FileIndex();
		if (!StringHelper.isEmpty(batch)) entity.setBatchNo(batch);
		entity.setOperateTime(new Date());
		if (getCurrentUserSession() != null && !StringHelper.isEmpty(getCurrentUserSession().getName())) {
			// 有登录的人员
			entity.setOperator(getCurrentUserSession().getName());
		}
		boolean autoSetFileTitle = StringHelper.isEmpty(entity.getFileTitle());
		File dest;
		String contentType;
		for (File file : files) {
			entity = (FileIndex) entity.clone();
			entity.setFileName(filesFileName[i]);
			entity.setFileSize(file.length());
			contentType = StringHelper.trim(filesContentType[i]);
			if (StringHelper.isEmpty(contentType) || FileHelper.DEFAULT_CONTENT_TYPE.equalsIgnoreCase(contentType)
					|| contentType.toLowerCase().startsWith(FileHelper.DEFAULT_CONTENT_TYPE.toLowerCase())) {
				// 例如: application/octet-stream; charset=ISO-8859-1
				contentType = FileHelper.getContentType(filesFileName[i]);
			}
			entity.setFileContentType(contentType);
			if (autoSetFileTitle) {
				// 没有指定文件标题,则使用文件名做文件标题
				entity.setFileTitle(FileHelper.getSimpleFileName(entity.getFileName()));
			}
			dest = new File(DIRECTORY, file.getName());
			FileHelper.move(file, dest); // 移动文件,如果不移动file,因为file是一个临时文件,会被JDK删除
			FileEntry entry = new FileEntry(entity, dest);
			list.add(entry);
			i++;
		}
		this.list = list;
		if (StringHelper.isEmpty(batch)) {
			// 一次性上传,则需要调用save方法保存到FileNet中
			validateSave();
			return this.save();
		}
		return this.dispatchSaveSuccess("文件上传成功!");
	}

	/** 用于处理文件的对象 */
	FileHandler handler;

	/** 所有已知的FileHandler class对象 */
	public static final Map<String, FileHandler> handlers = new HashMap<String, FileHandler>();
	static {
		/* EFiling的文件处理 */
		handlers.put(SystemUtils.SYSTEM_CODE, new EFilingHandler());
		handlers.put(EFilingHandler.class.getName(), handlers.get(SystemUtils.SYSTEM_CODE));
		handlers.put("default", DefaultFileHandler.getInstance());
		handlers.put(DefaultFileHandler.class.getName(), DefaultFileHandler.getInstance());
	}

	/**
	 * 将text中JS的特殊字符进行转码
	 * 
	 * @param text
	 * @return
	 */
	public static final String escape(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'").replace("\n", "\\n").replace("\r", "\\r");
	}

	/**
	 * 得到异常的堆栈信息,转换为String
	 * 
	 * @param e
	 * @return
	 * @see Throwable#printStackTrace(PrintWriter)
	 */
	public static String printStackTrace(Throwable e) {
		StringWriter buf = new StringWriter();
		e.printStackTrace(new PrintWriter(buf));
		return buf.toString();
	}

	@Override
	public void validateSave() {
		if (!StringHelper.isEmpty(batch)) {
			list = FILES.get(batch);
			// FILES.remove(batch);
		}
		if (Helper.isEmpty(list)) {
			addActionError("请至少上传一个附件!");
		}
		if (Helper.isEmpty(handlerClass)) {
			handler = DefaultFileHandler.getInstance();
		} else {
			// 强制转换
			handler = handlers.get(handlerClass);
			if (handler == null) {
				try {
					handler = (FileHandler) Class.forName(handlerClass).newInstance();
					handlers.put(handlerClass, handler);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler = null;
				}
			} else if (!handlers.containsKey(handler.getClass().getName())) {
				// 使用className缓存对象
				handlers.put(handler.getClass().getName(), handler);
			}
			if (handler == null) addActionError("参数错误,handlerClass:\"" + handlerClass + "\"不存在!");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String save() {
		JSONObject json = new JSONObject();
		json.put("batch", this.batch);
		if (!StringHelper.isEmpty(this.batch)) FILES.remove(batch);
		List<FileEntry> entries = null;
		// Exception ex = null;
		try {
			entries = handler.handle(list);
		} catch (Exception e) {
			e.printStackTrace();
			json.put("success", false);
			json.put("message", e.getMessage());

			// 失败后删除缓存文件
			for (FileEntry entry : (List<FileEntry>) list) {
				for (File file : entry.getFiles()) {
					file.delete();
				}
			}
		}
		if (entries != null) {
			try {
				FileEntry[] a = service.save(entries.toArray(new FileEntry[entries.size()]));
				list = new ArrayList<FileIndex>();
				for (FileEntry e : a) {
					list.add(e.getFileIndex());
				}
				json.put("success", true);
				json.put("list", list);
			} catch (Exception e) {
				// 出错
				// if (StringHelper.isEmpty(callback)) return this.dispatchException(e);
				e.printStackTrace();
				json.put("success", false);
				json.put("message", e.getMessage());
			} finally {// 删除本地文件
				for (FileEntry e : entries) {
					for (File file : e.getFiles()) {
						file.delete();
					}
				}
			}
		}
		if (StringHelper.isEmpty(callback)) return dispatchSuccess(json); // 未指定回调函数,则直接返回JSON数据

		// 使用iframe提交,需要执行回调函数
		StringBuffer content = new StringBuffer();
		content.append("<html><head><script type=\"text/javascript\">");
		content.append(callback); // 执行回调函数,参数为json对象和json字符串
		content.append("(").append(json).append(", \"").append(escape(json.toString())).append("\");");
		content.append("</script></head></html>");
		write(content.toString());

		return null;
	}

	/**
	 * 复制对象为一个新对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String copy() {
		entity = service.copy(id, entity);
		list = new ArrayList<FileIndex>();
		list.add(entity);
		JSONObject json = new JSONObject();
		json.put("success", true);
		json.put("list", list);
		return dispatchSuccess(json);
	}

	/**
	 * 向数据库中插入一条数据
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String insert() {
		entity = service.save(entity);
		list = new ArrayList<FileIndex>();
		list.add(entity);
		JSONObject json = new JSONObject();
		json.put("success", true);
		json.put("list", list);
		return dispatchSuccess(json);
	}

	@Override
	public void validateQuery() {
		super.validateQuery();

		// SystemCode参数必须传入
		if (Helper.isEmpty(ids) && Helper.isEmpty(id)) {
			if (Helper.isEmpty(systemCode)) {
				addActionError("SystemCode不能为空");
				return;
			} else if (Helper.isEmpty(businessNo)) {
				addActionError("businessNo不能为空");
				return;
			}
		}
		addQuery("systemCode", systemCode);
		addQuery("businessNo", businessNo);
		if (!Helper.isEmpty(ids)) addQuery("UPPER(id)", StringHelper.toUpperCase(ids));
		if (!StringHelper.isEmpty(id)) addQuery("UPPER(id)", id.toUpperCase());
		if (!StringHelper.isEmpty(fileId)) addQuery("UPPER(fileId)", fileId.toUpperCase());

		addQuery("batchNo", batchNo);

		addBetweenQuery("operateTime", operateTime, Date.class);
		addBetweenQuery("fileSize", fileSize, Long.class);
		addLikeQuery("fileTitle", fileTitle);
		addLikeQuery("fileName", fileName);

		if (!StringHelper.isEmpty(queryOperator)) { // 查询上传人,在上传时且查看的地址会带上操作人,但是默认情况不把操作人带入查询条件
			addQuery("UPPER(queryOperator)", StringHelper.toUpperCase(queryOperator));
		}

		addLikeQuery("text", text);

		if (!Helper.isEmpty(keywords)) {
			String[] words = new String[keywords.length];
			int i = 0;
			for (String keyword : keywords) {
				words[i++] = "%" + keyword.toUpperCase() + "%";
			}
			addQueryString("AND (UPPER(keywords) LIKE (?)" + StringHelper.copy("OR UPPER(keywords) LIKE (?)", keywords.length - 1) + ")",
					words);
		}

		addQuery("property00", property00);
		addQuery("property01", property01);
		addQuery("property02", property02);
		addQuery("property03", property03);
		addQuery("property04", property04);
		addQuery("property05", property05);
		addQuery("property06", property06);
		addQuery("property07", property07);
		addQuery("property08", property08);
		addQuery("property09", property09);

		if (property10 != null) {
			if (property10.length == 1) {
				addQuery("property10", NumberHelper.longValue(property10[0]));
			} else {
				addBetweenQuery("property10", property10, Long.class);
			}
		}
		if (property11 != null) {
			if (property11.length == 1) {
				addQuery("property11", NumberHelper.longValue(property11[0]));
			} else {
				addBetweenQuery("property11", property11, Long.class);
			}
		}
		if (property12 != null) {
			if (property12.length == 1) {
				addQuery("property12", NumberHelper.longValue(property12[0]));
			} else {
				addBetweenQuery("property12", property12, Long.class);
			}
		}
		if (property13 != null) {
			if (property13.length == 1) {
				addQuery("property13", NumberHelper.longValue(property13[0]));
			} else {
				addBetweenQuery("property13", property13, Long.class);
			}
		}
		if (property14 != null) {
			if (property14.length == 1) {
				addQuery("property14", NumberHelper.longValue(property14[0]));
			} else {
				addBetweenQuery("property14", property14, Long.class);
			}
		}
		if (property15 != null) {
			if (property15.length == 1) {
				addQuery("property15", NumberHelper.longValue(property15[0]));
			} else {
				addBetweenQuery("property15", property15, Long.class);
			}
		}
		queryString.insert(0, "FROM " + FileIndex.class.getName() + " WHERE ");

		addOrderString("ORDER BY operateTime, id");

		// 保存查询密码,以便view,preview等方法调用
		getSession().setAttribute("fileIndex.systemCode", StringHelper.trim(this.systemCode));
		getSession().setAttribute("fileIndex.password." + systemCode, StringHelper.trim(this.password));

		// System.out.print("password0=" + password);
	}

	private void doQuery() {
		if (this.pageIndex <= 0) this.pageIndex = 1;
		if (this.maxResults <= 0) this.maxResults = SystemUtils.DEFAULT_MAX_RESULTS; // 默认返回为20页
		String queryString = this.queryString.toString();
		if (orderString != null) {
			queryString += (" " + orderString.toString());
		}
		Object[] parameters = this.queryParameters == null || this.queryParameters.isEmpty() ? null : this.queryParameters.toArray();
		// list = service.query(queryString, parameters);
		pagingEntity = service.query(queryString, parameters, pageIndex, maxResults);

		list = pagingEntity.list();

		if (list != null) {
			for (Object e : list) {
				initPreview((FileIndex) e);
			}
		}
	}

	@Override
	public String query() {
		doQuery();
		return LIST;
	}

	public void validateShow() {
		this.validateQuery();
	}

	/**
	 * 显示在某个子系统的某个业务号下的所有附件
	 * 
	 * @return
	 */
	public String show() {
		doQuery();
		if (Helper.isEmpty(list)) {
			if (StringHelper.isEmpty(message)) {
				message = "未能找到任何文件!";
			}
			write(message);
			return null;
		}

		// JSONArray json = new JSONArray();//
		// json.setDatePattern(DateHelper.DEFAULT_DATETIME_FORMAT);
		// json.from(list);

		// getRequest().setAttribute("data", this.pagingEntity);

		JSONObject parameters = new JSONObject(getRequest().getParameterMap());
		getRequest().setAttribute("queryParameters", parameters.toJSONString());

		return "show";
	}

	public void validateList() {
		this.maxResults = 200; // 查询结果最多返回200条记录
		this.validateQuery();
	}

	/**
	 * 列出满足条件的附件,同时可在页面上进行上传操作
	 * 
	 * @return
	 */
	public String list() {
		doQuery();
		JSONArray json = new JSONArray();//
		json.setDatePattern(DateHelper.DEFAULT_DATETIME_FORMAT);
		json.from(list);
		return dispatchSuccess(json);
	}

	/** 图片预览属性文件前缀,对于非图片文件.xxx,如果配置PREVIEW_KEY_PREFIX.xxx,则指向其预览的图标文件 */
	private static final String PREVIEW_KEY_PREFIX = "fileindex.uploader.preview.";
	// private static final String PREVIEW_KEY_DEFAULT = "fileindex.uploader.preview.default";
	/** 默认的预览图标 */
	private static final String PREVIEW_DEFAULT = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + "default");
	/** 非图片文件的图标目录 */
	private static String PREVIEW_ICON = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + "dir.icon",
			"resources" + File.separator + "template" + File.separator + "default" + File.separator + "icon" + File.separator);
	/** 非图片文件的图标目录,用于URL,分隔符为/ */
	private static String PREVIEW_ICON_URL = PREVIEW_ICON.replace(File.separator, "/");
	private static File PREVIEW_ICON_DIR = null;

	/** 预览图片的缓存目录,仅是相对目录 */
	private static String PREVIEW_CACHE = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + "dir.cache",
			"cache" + File.separator + "preview" + File.separator);
	/** 非图片文件的图标目录,用于URL,分隔符为/ */
	private static String PREVIEW_CACHE_URL = PREVIEW_CACHE.replace(File.separator, "/");
	private static File PREVIEW_CACHE_DIR = null;

	/** 预览图片的高度 */
	private static final int PREVIEW_WIDTH = 128;
	/** 预览图片的高度 */
	private static final int PREVIEW_HEIGHT = 128;

	/**
	 * 初始化静态目录
	 * 
	 * @param refresh 如果为true则始终重新初始化,否则已经初始化则不会再初始化
	 */
	public static final void initPreviewCacheDir(boolean refresh) {
		if (refresh) {
			PREVIEW_ICON_DIR = null;
			PREVIEW_CACHE_DIR = null;
		}
		initPreviewCacheDir();
	}

	/** 初始化静态目录,如果已经初始化则不会再次初始化 */
	public static final void initPreviewCacheDir() {
		if (PREVIEW_ICON_DIR == null) {
			PREVIEW_ICON_DIR = new File(SystemUtils.getServerPath(PREVIEW_ICON));
			if (!PREVIEW_ICON_DIR.exists()) {
				PREVIEW_ICON_DIR.mkdirs();
			}
		}

		if (PREVIEW_CACHE_DIR == null) {
			PREVIEW_CACHE_DIR = new File(SystemUtils.getServerPath(PREVIEW_CACHE));
			if (!PREVIEW_CACHE_DIR.exists()) {
				PREVIEW_CACHE_DIR.mkdirs();
			}
		}
	}

	/**
	 * 默认图标所在目录
	 * 
	 * @return
	 */
	public static final File getPreviewIconDir() {
		return PREVIEW_ICON_DIR;
	}

	/**
	 * 图片文件缓存目录
	 * 
	 * @return
	 */
	public static final File getPreviewCacheDir() {
		return PREVIEW_CACHE_DIR;
	}

	/**
	 * 初始化entity的预览文件URL
	 * 
	 * @param entity
	 * @return
	 * @see FileIndex#setPreview(String)
	 */
	private String initPreview(FileIndex entity) {
		String fileSuffix = FileHelper.getSimpleFileSuffix(entity.getFileName());
		initPreviewCacheDir();
		String previewFileName = entity.getId() + ".preview." + fileSuffix;
		File previewFile = new File(PREVIEW_CACHE_DIR, previewFileName);
		if (previewFile.exists()) {
			// 有压缩图片的预览文件
			entity.setPreview(getContextPath(PREVIEW_CACHE_URL + previewFileName));
		} else {
			String result = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + fileSuffix);
			String contentType = entity.getFileContentType();

			if (result == null) {
				// 根据文件类型显示图片预览
				String path = FileHelper.getFilePath(contentType).replace(File.separator, ".");
				result = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + path);
				while (result == null) {
					int p = path.lastIndexOf(".");
					if (p < 0) break;
					path = path.substring(0, p);
					result = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + path);
				}
			}
			if ((result == null) && !contentType.startsWith("image")) {
				result = PREVIEW_DEFAULT;
			}

			if (!StringHelper.isEmpty(result)) {
				entity.setPreview(getContextPath(PREVIEW_ICON_URL + result));
			}
		}
		return entity.getPreview();
	}

	public void validatePreview() {
		validateView();
	}

	/**
	 * 预览数据,将所有文件显示为128*128的图标
	 * 
	 * @return
	 */
	public String preview() {
		File file = null;
		String contentType = entity.getFileContentType();
		if (entity.getPreview() != null) {
			file = new File(SystemUtils.getServerPath(entity.getPreview()));
			contentType = FileHelper.getContentType(file.getName());
		} else {
			String fileSuffix = FileHelper.getSimpleFileSuffix(entity.getFileName());
			String result = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + fileSuffix);
			if (result == null) {
				// 根据文件类型显示图片预览
				String path = FileHelper.getFilePath(contentType).replace(File.separator, ".");
				result = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + path);
				while (result == null) {
					int p = path.lastIndexOf(".");
					if (p < 0) break;
					path = path.substring(0, p);
					result = SystemHelper.getProperty(PREVIEW_KEY_PREFIX + path);
				}
			}
			initPreviewCacheDir();

			if ((result == null) && ImageHelper.isImageContentType(contentType)) { // contentType.startsWith("image")) {
				// 图片文件,进行压缩
				file = readFile(entity, false);
				fileSuffix = FileHelper.getSimpleFileSuffix(file);
				contentType = FileHelper.getContentType(file.getName());
				File previewFile = new File(PREVIEW_CACHE_DIR, entity.getId() + ".preview." + fileSuffix); // 创建预览文件
				try {
					ImageHelper.compress(file, previewFile, PREVIEW_WIDTH, PREVIEW_HEIGHT);
					initPreview(entity);
					service.save(entity);
				} catch (Exception e) {
					// 图片压缩失败,可能是不支持的格式;
					previewFile = null;
				} finally {
					file.delete();
				}
				file = previewFile;
			}
			if (file == null) {
				if (Helper.isEmpty(result)) result = PREVIEW_DEFAULT;

				file = new File(PREVIEW_ICON_DIR, result);
				contentType = FileHelper.getContentType(result);
			}
		}
		getResponse().setContentType(contentType);
		write(file);
		return null;
	}

	/** tiff的contentType */
	static final String TIFF_CONTENT_TYPE = EFilingHandler.TIFF_CONTENT_TYPE; // ContentTypeHelper.get(".tiff");

	/**
	 * 
	 * 读取entity对象的文件内容,会返回一个File对象。
	 * 
	 * @param entity
	 * @param original 是否读取原始文件，如果为否，且读取的是tiff文件，将会转换为JPEG
	 * @return
	 */
	protected File readFile(FileIndex entity, boolean original) {
		File file = readOriginalFile(entity);
		if (!original) {
			String contentType = entity.getFileContentType();
			if (!TIFF_CONTENT_TYPE.equals(contentType)) {
				contentType = ContentTypeHelper.get(FileHelper.getFileSuffix(entity.getFileName()));
			}
			if (TIFF_CONTENT_TYPE.equals(contentType)) { // TIFF转为JPEG
				File imgFile = null;
				try {
					imgFile = File.createTempFile(entity.getId(), ".jpeg");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				imgFile = TIFFHelper.toIMG(file, imgFile, "jpeg");
				file.delete();
				file = imgFile;
			}
		}
		return file;
	}

	/**
	 * 读取原始文件
	 * 
	 * @param entity
	 * @return
	 */
	protected File readOriginalFile(FileIndex entity) {
		if (entity == null) entity = service.get(id);
		File file;
		try {
			file = service.read(entity);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/** view方法跳转配置的前缀 */
	private static final String VIEWER_KEY_PREFIX = "fileindex.uploader.viewer.";

	// private static final String VIEWER_KEY_DEFAULT = "fileindex.uploader.viewer";
	/** view默认跳转至的页面配置,如果未空则直接下载文件 */
	private static final String VIEWER_DEFAULT = SystemHelper.getProperty("fileindex.uploader.viewer");

	/**
	 * 默认情况下,在view,read,download等情况下传入的参数都是通过fileId处理的
	 */
	public void validateView() {
		entity = service.get(id);
	}

	/** 仅读取附件 */
	@Override
	public String view() {
		if (entity == null || !service.exists(entity.getFileId())) {
			// 文件不存在,可通过message传入提示语
			message = StringHelper.noEmpty(message, text);
			if (StringHelper.isEmpty(message)) {
				message = "文件不存在或已被删除!";
			}
			write(message);
			// write("文件不存在,可能是因为扫描文件尚未上传至FileNet服务器,请隔天再进行查看!");
			// write("您扫描的影像文件系统正在处理，请于明日查看！");
			write(text);
			return null;
		}
		super.view();
		// 首先根据后缀名查找view的最终页面
		// logger.debug("view: fileName=" + fileName + "," + VIEWER_KEY_PREFIX);
		String result = SystemHelper.getProperty(VIEWER_KEY_PREFIX + FileHelper.getSimpleFileSuffix(entity.getFileName()));
		// logger.debug("view: result=" + result + ", simpleFileSuffix="
		// + FileHelper.getSimpleFileSuffix(entity.getFileName()));
		if (result == null) {
			// 如果没有后缀对应的页面,则通过contentType查找
			String contentType = entity.getFileContentType();
			String path = FileHelper.getFilePath(contentType).replace(File.separator, ".");
			result = SystemHelper.getProperty(VIEWER_KEY_PREFIX + path);
			// logger.debug("view: result=" + result + ", path=" + path + ", contentType=" + contentType);
			while (result == null) {
				int p = path.lastIndexOf(".");
				if (p < 0) break;
				path = path.substring(0, p);
				result = SystemHelper.getProperty(VIEWER_KEY_PREFIX + path);
				// logger.debug("view: result=" + result + ", path=" + path);
			}
		}
		if (result == null) result = VIEWER_DEFAULT;
		// logger.debug("view: result=" + result);
		if (!StringHelper.isEmpty(result)) return result;

		return download();
	}

	public void validateDownload() {
		this.validateView();
	}

	/**
	 * 下载附件,如果是tiff图片，将会转换为jpeg
	 * 
	 * @return
	 */
	public String download() {
		if (this.original == null) this.original = "true";
		File file = readFile(entity, StringHelper.parseBoolean(this.original));
		getResponse().setContentType(entity.getFileContentType());
		String fileName = entity.getFileTitle() + FileHelper.getFileSuffix(file.getName());
		try {
			// 防止中文乱码
			fileName = new String(fileName.getBytes("GBK"), "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			getResponse().setHeader("content-disposition", "attachment; filename=" + fileName);
			write(file);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			file.delete(); // 删除此文件
		}
		return NONE;
	}

	public void validateRead() {
		this.validateView();
	}

	/**
	 * 读取文件,如果original不为true，则如果是tiff图片，将会转换为jpeg
	 * 
	 * @return
	 * @see #original
	 */
	public String read() {
		File file = readFile(entity, StringHelper.parseBoolean(this.original));
		getResponse().setContentType(entity.getFileContentType());
		String fileName = entity.getFileTitle() + FileHelper.getFileSuffix(file.getName());
		try {
			// 防止中文乱码
			fileName = new String(fileName.getBytes("GBK"), "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			getResponse().setHeader("content-disposition", "attachment; filename=" + fileName);
			write(file);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			file.delete(); // 删除此文件
		}
		return NONE;
	}

	/**
	 * 向response写入文件
	 * 
	 * @param file
	 */
	public void write(java.io.File file) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			bos = new BufferedOutputStream(getResponse().getOutputStream());
			byte[] buf = new byte[1024];
			int bytes = 0;
			while ((bytes = bis.read(buf, 0, buf.length)) != -1) {
				bos.write(buf, 0, bytes);
			}
			bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MethodHelper.close(bis);
			MethodHelper.close(bos);
		}
	}

	public void validateDisable() {
		this.validateQuery();
		this.pageIndex = 1;
		this.maxResults = Integer.MAX_VALUE;
	}

	/**
	 * 禁用上传的文件 禁用条件:①根据文件id去禁用上传的文件②如果id为null,则根据单号和系统名称和文件名称两个条件去禁用上传的文件
	 */
	@SuppressWarnings("unchecked")
	public String disable() {
		this.doQuery();
		if (Helper.isEmpty(list)) return null;
		StringBuffer hql = new StringBuffer();
		hql.append(" UPDATE ").append(FileIndex.class.getName()).append(" f ");
		hql.append(" SET ").append(" f.status = ? WHERE f IN (? ").append(StringHelper.copy(",? ", (list.size() - 1)));
		list.add(0, SystemUtils.STATUS_VALID);
		// Object[] parameters = new Object[] { SystemUtils.STATUS_VALID, list };
		service.getDao().execute(hql.toString(), list.toArray());
		return null;
	}

	/** 所有validateXX调用后，最终会执行validate方法 */
	public void validate() {
		UserSessionEntity user = getCurrentUserSession();
		if ((user != null && !Helper.isEmpty(user.getId()))) { return; }

		if (entity != null) {
			systemCode = StringHelper.trim(entity.getSystemCode(), systemCode);
		}
		if (StringHelper.isEmpty(systemCode)) {
			systemCode = (String) getSession().getAttribute("fileIndex.systemCode");
		}
		// System.out.print("password1=" + password);
		if (StringHelper.isEmpty(password)) {
			// 读取vlidateQuery方法中保存的查询密码
			password = (String) getSession().getAttribute("fileIndex.password." + systemCode);
		} else {
			getSession().setAttribute("fileIndex.password." + systemCode, password);
		}

		// Object uri = getSession().getAttribute("uri");
		if (Helper.isEmpty(systemCode) || !service.validateSecurity(systemCode, password)) {
			addActionError(systemCode + "密码错误!");
			// if (uri == null) {
			// 发现密码错误则跳转至url,在error.jsp页面会自动跳转到login.html?url=url页面方便用户进行单点登陆
			// @see error.jsp
			String uri = getRequest().getRequestURI();
			getRequest().setAttribute("uri", uri);
			return;
			// }
		}
		// getSession().removeAttribute("uri");
	}

	/** 上传的文件对象 */
	private File[] files;
	private String[] filesFileName;
	private String[] filesContentType;

	// 对于FileIndex需要的参数,在前台必须通过name=entity.xxx进行提交
	/** 用于SWFUpload文件上传，指定批次的id.所有文件上传完成之后,可以通过FILES.get(batchId)来获取此次上传的所有文件对象.如果batch为null,则表示是一次性上传文件 */
	private String batch;
	/**
	 * 如果不是使用SWFUpload控件上传文件,则是通过iframe上传文件,因此需要执行回调函数。 callback一般为parent.XXX的形式,通过JS调用parent窗口的某个方法.仅需要方法名,不能带上括号。
	 * 在回调函数中,参数为json对象:{success: true/false,list:保存后的FileIndex数组, message:异常信息 }
	 */
	private String callback;

	/** 对上传文件的处理,处理完毕之后需要返回一个FileEntry对象 */
	private String handlerClass;

	/** 可以使用的查询条件 */
	private String[] ids;
	private String[] batchNo;
	private String[] operator;
	private String[] queryOperator;
	private String[] operateTime;
	private String[] fileSize;
	private String fileId;
	private String fileTitle;
	private String fileName;
	// private String[] fileContentType;
	// private Long[] fileCount;
	// private Long[] pageCount;

	private String systemCode;
	private String[] businessNo;
	private String[] keywords;
	private String[] property00;
	private String[] property01;
	private String[] property02;
	private String[] property03;
	private String[] property04;
	private String[] property05;
	private String[] property06;
	private String[] property07;
	private String[] property08;
	private String[] property09;
	private String[] property10;
	private String[] property11;
	private String[] property12;
	private String[] property13;
	private String[] property14;
	private String[] property15;
	private String password;
	private String text;
	/** 用于显示的异常信息 */
	private String message;
	/** 是(1)否(0)下载原始文件 */
	private String original;

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String[] getFilesFileName() {
		return filesFileName;
	}

	public void setFilesFileName(String[] filesFileName) {
		this.filesFileName = filesFileName;
	}

	public String[] getFilesContentType() {
		return filesContentType;
	}

	public void setFilesContentType(String[] filesContentType) {
		this.filesContentType = filesContentType;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batchId) {
		this.batch = batchId;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	// public static class FileEntry {
	// private File file;
	// private String fileName;
	// private String contentType;
	//
	// FileIndex fileIndex;
	//
	// public FileEntry() {
	// super();
	// // TODO Auto-generated constructor stub
	// }
	//
	// public FileEntry(File file, String fileName, String contentType, FileIndex fileIndex) {
	// super();
	// this.file = file;
	// this.fileName = fileName;
	// this.contentType = contentType;
	// this.fileIndex = fileIndex;
	// if (this.fileIndex == null) this.fileIndex = new FileIndex();
	// this.fileIndex.setFileName(fileName);
	// this.fileIndex.setFileSize(file.length());
	// this.fileIndex.setFileContentType(contentType);
	// this.fileIndex.setOperateTime(new Date());
	// }
	//
	// public File getFile() {
	// return file;
	// }
	//
	// public void setFile(File file) {
	// this.file = file;
	// }
	//
	// public String getFileName() {
	// return fileName;
	// }
	//
	// public void setFileName(String fileName) {
	// this.fileName = fileName;
	// }
	//
	// public String getContentType() {
	// return contentType;
	// }
	//
	// public void setContentType(String contentType) {
	// this.contentType = contentType;
	// }
	//
	// public FileIndex getFileIndex() {
	// return fileIndex;
	// }
	//
	// public void setFileIndex(FileIndex fileIndex) {
	// this.fileIndex = fileIndex;
	// }
	// }

	public String getHandlerClass() {
		return handlerClass;
	}

	public FileHandler getHandler() {
		return handler;
	}

	public void setHandler(FileHandler handler) {
		this.handler = handler;
	}

	public String[] getIds() {
		return ids;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}

	public String[] getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String[] batchNo) {
		this.batchNo = batchNo;
	}

	public String[] getOperator() {
		return operator;
	}

	public void setOperator(String[] operator) {
		this.operator = operator;
	}

	public String[] getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(String[] operateTime) {
		this.operateTime = operateTime;
	}

	public String[] getFileSize() {
		return fileSize;
	}

	public void setFileSize(String[] fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileTitle() {
		return fileTitle;
	}

	public void setFileTitle(String fileTitle) {
		this.fileTitle = fileTitle;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String[] getBusinessNo() {
		return businessNo;
	}

	public void setBusinessNo(String[] businessNo) {
		this.businessNo = businessNo;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public String[] getProperty00() {
		return property00;
	}

	public void setProperty00(String[] property00) {
		this.property00 = property00;
	}

	public String[] getProperty01() {
		return property01;
	}

	public void setProperty01(String[] property01) {
		this.property01 = property01;
	}

	public String[] getProperty02() {
		return property02;
	}

	public void setProperty02(String[] property02) {
		this.property02 = property02;
	}

	public String[] getProperty03() {
		return property03;
	}

	public void setProperty03(String[] property03) {
		this.property03 = property03;
	}

	public String[] getProperty04() {
		return property04;
	}

	public void setProperty04(String[] property04) {
		this.property04 = property04;
	}

	public String[] getProperty05() {
		return property05;
	}

	public void setProperty05(String[] property05) {
		this.property05 = property05;
	}

	public String[] getProperty06() {
		return property06;
	}

	public void setProperty06(String[] property06) {
		this.property06 = property06;
	}

	public String[] getProperty07() {
		return property07;
	}

	public void setProperty07(String[] property07) {
		this.property07 = property07;
	}

	public String[] getProperty08() {
		return property08;
	}

	public void setProperty08(String[] property08) {
		this.property08 = property08;
	}

	public String[] getProperty09() {
		return property09;
	}

	public void setProperty09(String[] property09) {
		this.property09 = property09;
	}

	public String[] getProperty10() {
		return property10;
	}

	public void setProperty10(String[] property10) {
		this.property10 = property10;
	}

	public String[] getProperty11() {
		return property11;
	}

	public void setProperty11(String[] property11) {
		this.property11 = property11;
	}

	public String[] getProperty12() {
		return property12;
	}

	public void setProperty12(String[] property12) {
		this.property12 = property12;
	}

	public String[] getProperty13() {
		return property13;
	}

	public void setProperty13(String[] property13) {
		this.property13 = property13;
	}

	public String[] getProperty14() {
		return property14;
	}

	public void setProperty14(String[] property14) {
		this.property14 = property14;
	}

	public String[] getProperty15() {
		return property15;
	}

	public void setProperty15(String[] property15) {
		this.property15 = property15;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String[] getQueryOperator() {
		return queryOperator;
	}

	public void setQueryOperator(String[] queryOperator) {
		this.queryOperator = queryOperator;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setHandlerClass(String handlerClass) {
		this.handlerClass = handlerClass;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

}
