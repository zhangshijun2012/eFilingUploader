package com.sinosoft.filenet;

import java.util.List;

/**
 * 处理上传的文件,将List<FileEntry>处理后,返回新的List<FileEntry>对象
 * 
 * @author LuoGang
 * 
 */
public interface FileHandler {
	/**
	 * 处理要保存的文件对象,然后返回新的要保存的对象.可以对文件进行合并等操作.返回的结果永远不能为null
	 * 
	 * @param entries
	 * @return 需要保存的数据,不能为null
	 */
	public List<FileEntry> handle(List<FileEntry> entries);
}
