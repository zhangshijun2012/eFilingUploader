package com.sinosoft.util.hibernate.entity;

import java.io.Serializable;
import java.util.Date;

import com.sinosoft.efiling.hibernate.entity.Company;
import com.sinosoft.efiling.hibernate.entity.User;
import com.sinosoft.efiling.util.SystemUtils;

/**
 * 有操作员的数据库实例
 * 
 * @author LuoGang
 * 
 * @param <I>
 */
public class EntityOperatorSupport<I extends Serializable> extends
		EntitySupport<I> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4473906373899991517L;

	public EntityOperatorSupport() {
		super();
		this.status = SystemUtils.STATUS_VALID;
	}

	/** 数据状态 */
	protected String status;

	/** 新增时的操作员 */
	protected User user;
	protected Company department;
	/** 新增时间 */
	protected Date insertTime;

	/** 更新人 */
	protected User updateUser;
	protected Company updateDepartment;
	/** 更新时间 */
	protected Date updateTime;

	/** 分公司 */
	protected Company company;

	/** 备注 */
	protected String remarks;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		if (user != null) {
			this.department = user.getDepartment();
			// this.company = department.getRoot();
			this.setUpdateUser(user);
		}
	}

	public Company getDepartment() {
		return department;
	}

	public void setDepartment(Company department) {
		this.department = department;
		// this.company = department.getRoot();
	}

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	public User getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(User updateUser) {
		this.updateUser = updateUser;
		if (updateUser != null)
			this.updateDepartment = updateUser.getDepartment();
	}

	public Company getUpdateDepartment() {
		return updateDepartment;
	}

	public void setUpdateDepartment(Company updateDepartment) {
		this.updateDepartment = updateDepartment;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
