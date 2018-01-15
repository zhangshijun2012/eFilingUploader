package com.sinosoft.filenet.handler;

import java.util.List;

import com.sinosoft.filenet.FileEntry;
import com.sinosoft.filenet.FileHandler;

/**
 * 默认的处理方式,此类不对上传的文件进行任何处理
 * 
 * @author LuoGang
 * 
 */
public class DefaultFileHandler implements FileHandler {
	private static final DefaultFileHandler handler = new DefaultFileHandler();

	public static final DefaultFileHandler getInstance() {
		return handler;
	}

	private DefaultFileHandler() {
	}

	/**
	 * 默认的实现,不进行任何处理,直接返回
	 */
	public List<FileEntry> handle(List<FileEntry> entries) {
		return entries;
	}
}