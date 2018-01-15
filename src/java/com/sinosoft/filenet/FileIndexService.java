package com.sinosoft.filenet;

import java.io.File;

import com.sinosoft.efiling.hibernate.dao.ConfigureDao;
import com.sinosoft.util.Helper;
import com.sinosoft.util.MethodHelper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.service.ServiceSupport;

public class FileIndexService extends ServiceSupport<FileIndex, FileIndexDao> {
	/** 配置表中的type字段 */
	public static final String CONFIGURE_TYPE_FILE_NET = "eFiling.filenet";
	private ConfigureDao configureDao;
	private FileSystemService fileSystemService;

	/**
	 * 保存文件,并上传至FileNet.当文件已经上传至FileNet之后,才保存本地数据库
	 * 
	 * @param entry
	 * @return
	 */
	public FileEntry save(FileEntry entry) {
		FileNetConnection con = new FileNetConnection();
		// FileNetSocket con = new FileNetSocket();
		entry = con.save(entry);
		con.close();
		// FileIndex fileIndex = entry.getFileIndex();
		// if (!Helper.isEmpty(fileIndex.getId())) fileIndex.setId(StringHelper.uuid());
		save(entry.getFileIndex());

		getLogger().debug("FileIndexService.save(FileEntry) success!id=" + entry.getFileIndex().getId());
		return entry;
	}

	/**
	 * 
	 * 保存多个文件,并上传至FileNet.当文件已经上传至FileNet之后,才保存本地数据库
	 * 
	 * @param entries
	 * @return entries
	 */
	public FileEntry[] save(FileEntry[] entries) {
		FileNetConnection con = new FileNetConnection();
		// FileNetSocket con = new FileNetSocket();
		entries = con.save(entries);
		con.close();
		FileIndex fileIndex;
		for (FileEntry entry : entries) {
			fileIndex = entry.getFileIndex();
			// if (!Helper.isEmpty(fileIndex.getId())) fileIndex.setId(StringHelper.uuid());
			save(fileIndex);
			getLogger().debug("FileIndexService.save(FileEntry[]) success!id=" + entry.getFileIndex().getId());
		}
		getLogger().debug("FileIndexService.save(FileEntry[]) success!");
		return entries;
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param fileId
	 * @return
	 */
	public boolean exists(String fileId) {
		FileNetConnection con = new FileNetConnection();
		try {
			return con.exists(fileId);
		} finally {
			con.close();
		}
	}

	/**
	 * 读取entity在FileNet中的文件到一个临时文件中
	 * 
	 * @param entity
	 * @return 生成的临时文件
	 */
	public File read(FileIndex entity) {
		FileNetConnection con = null;
		// FileNetSocket con = new FileNetSocket();
		File file = null;
		try {
			con = new FileNetConnection();
			file = File.createTempFile("read", entity.getFileName());
			file = con.read(entity.getFileId(), file);
			// System.out.println(file.length());
			con.close();
			long fileSize = file.length();
			if (entity.getFileSize() != fileSize) {
				// 文件的大小与实际大小不一致,更新文件大小
				entity.setFileSize(fileSize);
				dao.update(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MethodHelper.close(con);
		}
		return file;

	}

	/**
	 * 复制一条文件索引信息
	 * 
	 * @param source 被复制的对象
	 * @param target 需要保存的新属性
	 * @return
	 */
	public FileIndex copy(FileIndex source, FileIndex target) {
		FileIndex e = new FileIndex();
		Helper.copyValues(source, e, new String[] { "id" });
		Helper.copyValues(target, e, new String[] { "fileSize", "fileCount", "pageCount" });
		Helper.copyValues(e, target);
		if (StringHelper.isEmpty(target.getId())) target.setId(StringHelper.uuid());
		return save(target);
	}

	/**
	 * 复制文件索引信息
	 * 
	 * @param sourceId 被复制的id
	 * @param target 要保存的新属性
	 * @return
	 */
	public FileIndex copy(String sourceId, FileIndex target) {
		return copy(get(sourceId), target);
	}

	/**
	 * 对所有接口上传影像资料的时候,进行域校验
	 * 
	 * @param systemCode 系统名称
	 * @param password 校验密码
	 * @return
	 */
	public boolean validateSecurity(String systemCode, String password) {
		FileSystem fileSystem = fileSystemService.get(systemCode);
		if (!Helper.isEmpty(fileSystem) && fileSystem.getPassword().equals(password)) return true;
		return false;
	}

	public FileSystemService getFileSystemService() {
		return fileSystemService;
	}

	public void setFileSystemService(FileSystemService fileSystemService) {
		this.fileSystemService = fileSystemService;
	}

	public ConfigureDao getConfigureDao() {
		return configureDao;
	}

	public void setConfigureDao(ConfigureDao configureDao) {
		this.configureDao = configureDao;
	}
}
