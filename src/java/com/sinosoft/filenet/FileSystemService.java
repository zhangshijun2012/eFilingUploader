package com.sinosoft.filenet;

import com.sinosoft.util.Helper;
import com.sinosoft.util.service.ServiceSupport;

public class FileSystemService extends ServiceSupport<FileSystem, FileSystemDao> {
	/**
	 * 修改系统校验的密码
	 * @param uniqueCode  唯一标示
 	 * @param oldPassword 原来密码
	 * @param newPassword 新密码
	 * @return
	 */
	public String modify(String uniqueCode, String oldPassword, String newPassword) {
		StringBuffer hql = new StringBuffer();
		hql.append(" FROM ").append(FileSystem.class.getName()).append(" f ");
		hql.append(" WHERE f.id=?");
		Object [] parameters = new Object [] {uniqueCode};
		FileSystem fileSystem = (FileSystem) dao.uniqueResult(hql.toString(), parameters);
		if (Helper.isEmpty(fileSystem)) return "没有" + uniqueCode + "对应的密码！";
		if (!fileSystem.getPassword().equals(oldPassword)) return "请输入" + uniqueCode + "的正确密码后才能够修改！"; 
		fileSystem.setPassword(newPassword);
		dao.update(fileSystem);
		return null;
	}
	/**
	 * 新增密码校验
	 * @return
	 */
	public String append() {
		return null;
	}
}
