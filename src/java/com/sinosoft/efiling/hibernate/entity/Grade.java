package com.sinosoft.efiling.hibernate.entity;

// Generated 2013-3-8 17:57:18 by Hibernate Tools 4.0.0

import java.util.HashSet;
import java.util.Set;

import com.sinosoft.util.hibernate.entity.EntitySupport;

/**
 * Grade generated by hbm2java
 */
public class Grade extends EntitySupport<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1866319050119178759L;
	private String name;
	private String remark;
	private String flag;
	private String gradeLevel;
	private String gradeEName;
	private Set<GradeTask> gradeTasks = new HashSet<GradeTask>(0);

	public Grade() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getFlag() {
		return this.flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getGradeLevel() {
		return this.gradeLevel;
	}

	public void setGradeLevel(String gradeLevel) {
		this.gradeLevel = gradeLevel;
	}

	public String getGradeEName() {
		return this.gradeEName;
	}

	public void setGradeEName(String gradeEName) {
		this.gradeEName = gradeEName;
	}

	public Set<GradeTask> getGradeTasks() {
		return gradeTasks;
	}

	public void setGradeTasks(Set<GradeTask> gradeTasks) {
		this.gradeTasks = gradeTasks;
	}

}
