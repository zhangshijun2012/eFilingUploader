package com.sinosoft.filenet;

import org.json.JSONObject;

import com.sinosoft.util.struts2.action.EntityActionSupport;

/**
 * 用于文件上传校验的Action
 * @author LuoGang
 */
public class FileSystemAction extends EntityActionSupport<FileSystem, FileSystemDao, FileSystemService, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5528252747335427780L;
	
 
	public String index() {
		return SUCCESS;
	}
	/**
	 * 修改密码
	 * @return
	 */
	public String modify() {
		String message = service.modify(uniqueCode, oldPassword, newPassword);
		JSONObject json = new JSONObject();
		if (message == null) {
			json.put("success", true);
			json.put("message", this.systemName + "安全密码修改成功");
		} else {
			json.put("success", false);
			json.put("message", message);
		}
		return dispatchSuccess(json);
	}
	/**
	 * 外部系统通过传入的系统id来获取么个系统对应的password
	 * @return
	 */
	public String verify() {
		StringBuffer hql = new StringBuffer();
		hql.append("");
		return hql.toString();
	}
	/**
	 * 增加一个模块进行校验
	 * @return
	 */
	public String append() {
		
		return null;
	}
	public String getUniqueCode() {
		return uniqueCode;
	}
	public void setUniqueCode(String uniqueCode) {
		this.uniqueCode = uniqueCode;
	}
	 
	public String getSystemName() {
		return systemName;
	}
 
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	 
	public String getOldPassword() {
		return oldPassword;
	}
 
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
 
	public String getNewPassword() {
		return newPassword;
	}
 
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	//唯一标识码
	private String uniqueCode;
	//系统名称
	private String systemName;
	//旧密码
	private String oldPassword;
	//新密码
	private String newPassword;
	
	
}
