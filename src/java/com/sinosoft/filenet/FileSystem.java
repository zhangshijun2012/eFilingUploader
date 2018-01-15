package com.sinosoft.filenet;

import com.sinosoft.util.hibernate.entity.EntitySupport;

/**
 * 
 * 对各个系统接口访问eFiling系统做一个域校验的基础类
 * @author LuoGang
 * 
 */
public class FileSystem extends EntitySupport<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8616973018585309114L;

	private String name;
	private String password;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
}
