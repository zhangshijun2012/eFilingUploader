package com.sinosoft.efiling.hibernate.entity;

import java.io.Serializable;

/**
 * 配置表的主键
 * 
 * @author LuoGang
 * 
 */
public class ConfigureId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4240544394156958114L;

	public ConfigureId() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ConfigureId(String type, String key) {
		super();
		this.type = type;
		this.key = key;
	}

	private String type;
	private String key;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
