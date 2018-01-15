package com.sinosoft.efiling.hibernate.entity;

import com.sinosoft.util.hibernate.entity.EntityOperatorSupport;

/**
 * 配置信息表
 * 
 * @author LuoGang
 * 
 */
public class Configure extends EntityOperatorSupport<ConfigureId> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1027305546020549602L;

	private ConfigureId id;
	private String value;
	private String status;
	private String remarks;

	public ConfigureId getId() {
		return id;
	}

	public void setId(ConfigureId id) {
		this.id = id;
	}

	public String getType() {
		return getId().getType();
	}

	public String getKey() {
		return getId().getKey();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
