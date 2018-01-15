package com.sinosoft.filenet;

import java.io.File;

import com.sinosoft.util.FileHelper;

/**
 * 用于外部操作的对象
 * 
 * @author LuoGang
 * 
 */
public class FileEntry {
	public FileEntry() {
		super();
	}

	public FileEntry(FileIndex fileIndex, File file) {
		super();
		this.fileIndex = fileIndex;
		this.files = new File[] { file };
		this.filesFileName = new String[] { fileIndex.getFileName() };
		this.filesContentType = new String[] { fileIndex.getFileContentType() };
	}

	public FileEntry(FileIndex fileIndex, File[] files) {
		super();
		this.fileIndex = fileIndex;
		this.files = files;
		int l = files.length;
		this.filesFileName = new String[files.length];
		for (int i = 0; i < l; i++) {
			filesFileName[i] = files[i].getName();
		}
		// this.filesContentType = new String[] { fileIndex.getFileContentType() };
		this.filesContentType = new String[files.length];
		for (int i = 0; i < l; i++) {
			filesContentType[i] = FileHelper.getContentType(files[i].getName());
		}
	}

	public FileEntry(FileIndex fileIndex, File[] files, String[] filesFileName, String[] filesContentType) {
		super();
		this.fileIndex = fileIndex;
		this.files = files;
		this.filesFileName = filesFileName;
		this.filesContentType = filesContentType;
	}

	/** 文件索引对象 */
	private FileIndex fileIndex;
	/** 真实的文件对象 */
	private File[] files;
	private String[] filesFileName;
	private String[] filesContentType;

	/** 上传fileNet之后返回的filenet属性 */
	private FileNetEntity fileNetEntity;

	public FileNetEntity getFileNetEntity() {
		return fileNetEntity;
	}

	public void setFileNetEntity(FileNetEntity fileNetEntity) {
		this.fileNetEntity = fileNetEntity;
	}

	public FileIndex getFileIndex() {
		return fileIndex;
	}

	public void setFileIndex(FileIndex fileIndex) {
		this.fileIndex = fileIndex;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public String[] getFilesFileName() {
		return filesFileName;
	}

	public String getFileName(int i) {
		if (filesFileName == null || filesFileName.length <= i) return null;
		return filesFileName[i];
	}

	public void setFilesFileName(String[] filesFileName) {
		this.filesFileName = filesFileName;
	}

	public String[] getFilesContentType() {
		return filesContentType;
	}

	public String getFileContentType(int i) {
		if (filesContentType == null || filesContentType.length <= i) return null;
		return filesContentType[i];
	}

	public void setFilesContentType(String[] filesContentType) {
		this.filesContentType = filesContentType;
	}
}
