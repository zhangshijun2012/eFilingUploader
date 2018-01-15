package com.sinosoft.filenet;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.Subject;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.property.Properties;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.sinosoft.util.CustomException;
import com.sinosoft.util.DateHelper;
import com.sinosoft.util.FileHelper;
import com.sinosoft.util.LoggableImpl;
import com.sinosoft.util.MethodHelper;
import com.sinosoft.util.StringHelper;

public class FileNetConnection extends LoggableImpl implements Closeable {
	private String uri;
	private String user;
	private String password;
	private String stanza;
	private String domainName;
	private String objectStoreName;
	private String documentClassId;
	private String root;
	private Connection conn;
	private Domain domain;
	private ObjectStore objectStore;

	/**
	 * 使用默认配置进行FileNet的连接
	 * 
	 * @see FileNetHelper
	 */
	public FileNetConnection() {
		this.uri = FileNetHelper.URI;
		this.user = FileNetHelper.USER;
		this.password = FileNetHelper.PASSWORD;
		this.stanza = FileNetHelper.STANZA;
		this.domainName = FileNetHelper.DOMAIN;
		this.objectStoreName = FileNetHelper.OBJECT_STORE;
		this.documentClassId = FileNetHelper.DOCUMENT_CLASS;
		this.root = FileNetHelper.ROOT;
		connect();
	}

	public FileNetConnection(String uri, String user, String password, String stanza, String domainName, String objectStoreName,
			String documentClassId, String root) {
		super();
		this.uri = uri;
		this.user = user;
		this.password = password;
		this.stanza = stanza;
		this.domainName = domainName;
		this.objectStoreName = objectStoreName;
		this.documentClassId = documentClassId;
		this.root = root;
	}

	/** 关闭连接 */
	public void close() {
		UserContext uc = UserContext.get();
		uc.popSubject();
	}

	public Connection connect() {
		if (conn != null) return conn;
		conn = Factory.Connection.getConnection(uri, null);
		Subject sub = UserContext.createSubject(conn, user, password, stanza);
		UserContext uc = UserContext.get();
		uc.pushSubject(sub);

		// ConfigurationParameter c = ConfigurationParameter
		// .getInstanceFromInt(ConfigurationParameter.WSI_TRANSPORT_CONNECTION_TIMEOUT_AS_INT);
		// conn.setParameter(c, 10000);

		// get Domain object;
		domain = Factory.Domain.fetchInstance(conn, domainName, null);

		// get ObjectStore object
		objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

		return conn;
	}

	/**
	 * 保存多个文件对象
	 * 
	 * @param entries
	 * @return
	 */
	public FileEntry[] save(FileEntry[] entries) {
		for (FileEntry fileEntry : entries) {
			save(fileEntry);
		}
		getLogger().debug("FileNetConnection.save(FileEntry[]) success!");
		return entries;
	}

	/**
	 * 保存文件到FileNet中
	 * 
	 * @param fileEntry
	 */
	@SuppressWarnings("unchecked")
	public FileEntry save(FileEntry fileEntry) {
		// 保存文件
		Document doc = Factory.Document.createInstance(objectStore, documentClassId);
		Folder folder = getCurrentFolder();
		ContentElementList contentList = Factory.ContentElement.createList();
		ContentTransfer content;
		// File file;
		int i = 0;
		List<InputStream> ins = new ArrayList<InputStream>();
		try {
			for (File file : fileEntry.getFiles()) {
				// file = fileEntry.getFile();
				content = Factory.ContentTransfer.createInstance();
				try {
					FileInputStream in = new FileInputStream(file);
					ins.add(in);
					content.setCaptureSource(in);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new CustomException(e);
				}
				content.set_ContentType(fileEntry.getFileContentType(i));
				content.set_RetrievalName(fileEntry.getFileName(i));
				contentList.add(content);
				i++;
			}

			doc.set_ContentElements(contentList);
			doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
			Properties properties = doc.getProperties();
			// System.out.println("ID1=" + properties.getObjectValue("ID"));
			FileIndex fileIndex = fileEntry.getFileIndex();
			if (StringHelper.isEmpty(fileIndex.getId())) fileIndex.setId(StringHelper.uuid());
			if (StringHelper.isEmpty(fileIndex.getFileId())) fileIndex.setFileId(fileIndex.getId());
			if (StringHelper.isEmpty(fileIndex.getFileNo())) fileIndex.setFileNo(fileIndex.getFileId());
			FileNetEntity entity = new FileNetEntity(properties);
			fileEntry.setFileNetEntity(entity);
			entity.setId(fileIndex.getFileId());
			entity.setUnique(fileIndex.getFileNo());
			entity.setDocumentTitle(fileIndex.getFileTitle());
			entity.setServiceNum(fileIndex.getBusinessNo());
			entity.setOperator(fileIndex.getOperator());
			entity.setOperateTime(DateHelper.formatDateTime(fileIndex.getOperateTime()));
			entity.setDef1(fileIndex.getProperty00());
			entity.setDef2(fileIndex.getProperty01());
			entity.setDef3(fileIndex.getProperty02());
			entity.setDef4(fileIndex.getProperty03());
			entity.setDef5(fileIndex.getProperty04());
			entity.write();
			// String id = StringHelper.uuid();
			// System.out.println("id=" + id);
			// properties.putValue("id", new Id(id));
			doc.set_MimeType(fileIndex.getFileContentType());// "image/tiff");
			doc.save(RefreshMode.REFRESH);

			// System.out.println("ID=" + properties.getObjectValue("id"));
			ReferentialContainmentRelationship rel = folder.file(doc, AutoUniqueName.NOT_AUTO_UNIQUE, entity.getId(),
					DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
			rel.save(RefreshMode.NO_REFRESH);
			properties = doc.getProperties();
			// entity.setProperties(properties);
			entity.read(properties);
			fileIndex.setFileId(entity.getId());
			// System.out.println("ID=" + properties.getObjectValue("id"));
			// System.out.println("document with multiple content is added");
			getLogger().debug("FileNetConnection.save(FileEntry) success!id=" + entity.getId());
		} finally {
			for (InputStream in : ins) {
				MethodHelper.close(in);
			}
		}
		return fileEntry;
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param id
	 * @return
	 */
	public boolean exists(String id) {
		try {
			Document doc = Factory.Document.fetchInstance(this.objectStore, new Id(id), null);
			return doc != null;
		} catch (EngineRuntimeException e) {
			// e.printStackTrace();
			if (ExceptionCode.E_OBJECT_NOT_FOUND.equals(e.getExceptionCode())) {
				// 未找到此对象
				return false;
			}
			throw e;
		}

		// return false;
	}

	/**
	 * 从FileNet读取 第一个文件并保存写入file文件中
	 * 
	 * @param id
	 * @param file
	 * @return
	 */
	public File read(String id, File file) {
		Document doc = Factory.Document.fetchInstance(this.objectStore, new Id(id), null);
		InputStream in = doc.accessContentStream(0);
		FileHelper.write(file, in);
		MethodHelper.close(in);
		return file;
	}

	/**
	 * 将id中的文件全部下载到folder目录中
	 * 
	 * @param id
	 * @param folder
	 * @return
	 */
	public File[] download(String id, File folder) {
		FileHelper.createDirectory(folder);
		Document doc = Factory.Document.fetchInstance(this.objectStore, new Id(id), null);
		ContentElementList contentList = doc.get_ContentElements();
		int count = contentList.size();
		File[] files = new File[count];
		ContentTransfer ce;
		for (int i = 0; i < count; i++) {
			ce = (ContentTransfer) contentList.get(i);
			files[i] = new File(folder, ce.get_RetrievalName());
			InputStream in = doc.accessContentStream(i);
			FileHelper.write(files[i], in);
			MethodHelper.close(in);
		}
		return files;
	}

	/**
	 * 下载FileNet的文件到临时目录中
	 * 
	 * @param id
	 * @return 下载的文件数组
	 */
	public File[] download(String id) {
		Document doc = Factory.Document.fetchInstance(this.objectStore, new Id(id), null);
		ContentElementList contentList = doc.get_ContentElements();
		int count = contentList.size();
		File[] files = new File[count];
		ContentTransfer ce;
		String fileName;
		for (int i = 0; i < count; i++) {
			ce = (ContentTransfer) contentList.get(i);
			fileName = ce.get_RetrievalName();
			try {
				String prefix = FileHelper.getSimpleFileName(fileName);
				if (prefix.length() < 3) prefix += StringHelper.copy("0", 3 - prefix.length());
				files[i] = File.createTempFile(prefix, FileHelper.getFileSuffix(fileName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStream in = doc.accessContentStream(i);
			FileHelper.write(files[i], in);
			MethodHelper.close(in);

		}
		return files;
	}

	/** 路径分隔符 */
	public static final String PATH_SEPARATOR = "/";
	/** 创建目录的名称,文件存放在yyyy-MM目录下的dd目录 */
	public static final String DATE_PATTERN = "yyyy-MM" + PATH_SEPARATOR + "dd";

	/**
	 * 创得到当前的工作目录.
	 * 如果目录不存在,则创建的目录为根目录下名称为yyyy-MM/dd目录,如果已经存在则直接返回
	 * 
	 * @return
	 * 
	 * @see #DATE_PATTERN
	 */
	public Folder getCurrentFolder() {
		String folderName = DateHelper.format(new Date(), DATE_PATTERN);
		String path = root + PATH_SEPARATOR + folderName;
		Folder folder = createFolder(path);
		return folder;
	}

	/**
	 * 创建文件夹
	 * 
	 * @param path 绝对路径,从根目录开始
	 * @return
	 */
	public Folder createFolder(String path) {
		if (!PATH_SEPARATOR.equals("\\")) path = path.replace("\\", PATH_SEPARATOR);
		if (!PATH_SEPARATOR.equals("/")) path = path.replace("/", PATH_SEPARATOR);
		Folder folder = null;
		try {
			folder = Factory.Folder.fetchInstance(objectStore, path, null);
		} catch (Exception ingor) {
			// 尚不存在此目录
		}
		if (folder != null) return folder; // 已经存在

		// 创建父节点
		int index = path.lastIndexOf(PATH_SEPARATOR);
		String folderName;
		if (index <= 0) {
			// 根节点
			folder = Factory.Folder.createInstance(objectStore, "Folder");
			if (index == 0) folderName = path.substring(index + 1);
			else folderName = path;
			folder.set_FolderName(folderName);
			// folder.set_Parent(parentFolder);
		} else {
			folderName = path.substring(index + 1);
			String parentPath = path.substring(0, index);
			Folder parentFolder = createFolder(parentPath);
			folder = parentFolder.createSubFolder(folderName);
			// folder = Factory.Folder.createInstance(objectStore, "Folder");
			// folder.set_FolderName(folderName);
			// folder.set_Parent(parentFolder);
			// folder.save(RefreshMode.REFRESH);
		}
		folder.save(RefreshMode.REFRESH);
		return folder;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStanza() {
		return stanza;
	}

	public void setStanza(String stanza) {
		this.stanza = stanza;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getObjectStoreName() {
		return objectStoreName;
	}

	public void setObjectStoreName(String objectStoreName) {
		this.objectStoreName = objectStoreName;
	}

	public String getDocumentClassId() {
		return documentClassId;
	}

	public void setDocumentClassId(String documentClassId) {
		this.documentClassId = documentClassId;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public ObjectStore getObjectStore() {
		return objectStore;
	}

	public void setObjectStore(ObjectStore objectStore) {
		this.objectStore = objectStore;
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		try {
			FileNetConnection con = new FileNetConnection();
			FileEntry entry = new FileEntry();
			FileIndex fileIndex = new FileIndex();
			File file = new File("d:\\x.png");
			fileIndex.setFileId(StringHelper.uuid());
			fileIndex.setFileContentType(FileHelper.getContentType(file.getName()));
			fileIndex.setFileName(file.getName());
			fileIndex.setFileTitle(file.getName());
			entry.setFileIndex(new FileIndex());
			entry.setFiles(new File[] { file });
			entry.setFilesContentType(new String[] { fileIndex.getFileContentType() });
			entry.setFilesFileName(new String[] { fileIndex.getFileName() });
			con.save(entry);
			con.close();

			System.out.println("fileId=" + fileIndex.getFileId());
			String id = fileIndex.getFileId(); // "17FE2F33-0916-44F6-ADBF-6E489F2CDE86";
			System.out.println(id);
			// c.read(id, new File("d:\\x12.png"));
			id = entry.getFileNetEntity().getId();// "DCB6D0BE-0399-4623-84C2-7227B6961CF8";
			System.out.println(id);
			con.download(id, new File("d:\\XXX"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			time = System.currentTimeMillis() - time;
			System.out.println(time + "ms");
		}
	}
}
