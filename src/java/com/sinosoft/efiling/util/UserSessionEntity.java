package com.sinosoft.efiling.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.sinosoft.efiling.hibernate.entity.Company;
import com.sinosoft.efiling.hibernate.entity.Grade;
import com.sinosoft.efiling.hibernate.entity.User;
import com.sinosoft.efiling.hibernate.entity.UserGrade;
import com.sinosoft.efiling.hibernate.entity.UserGradePower;
import com.sinosoft.util.Helper;

/**
 * 用于保存在session中的User对象
 * 
 * @author LuoGang
 * 
 */
public class UserSessionEntity extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1722352076029499368L;
	/** 真实的user对象 */
	private User user;

	/** 当前登录人使用的国际化 */
	private Locale locale;

	/** 当前的登录岗位所在的部门 */
	private List<Company> currentGradeDepartments;
	/** 当前的登录机构 */
	private Company currentDepartment;
	/** 当前的登录所在分公司 */
	private Company currentCompany;
	/** 当前的登录后的所有公司,currentCompany及其下属公司 */
	private List<Company> currentCompanies;
	/** 当前的登录后的所有可进行切换的部门 */
	private List<Company> currentDepartments;
	/** 所有的岗位 */
	private List<Grade> currentGrades;
	/** 所有的岗位权限 */
	private List<UserGradePower> currentUserGradePowers;
	/** 当前所在部门的岗位 */
	private List<UserGrade> currentUserGrades;

	public UserSessionEntity() {
		super();
	}

	public UserSessionEntity(User user) {
		convertFrom(user);
	}

	public UserSessionEntity(User user, Company currentDepartment, Company currentCompany) {
		this(user);
		// this.user = user;
		this.currentDepartment = currentDepartment;
		this.currentCompany = currentCompany;
	}

	/** 得到转换来的实例对象 */
	public User getInstance() {
		return user;
	}

	/**
	 * 
	 * 从User对象转换为当前对象
	 * 
	 * @param user
	 * @return this
	 */
	public UserSessionEntity convertFrom(User user) {
		this.user = user;
		// FieldHelper.copy(user, this);
		Helper.copy(user, this);
		return this;
	}

	public Company getCurrentCompany() {
		return currentCompany;
	}

	public void setCurrentCompany(Company company) {
		this.currentCompany = company;
	}

	public Company getCurrentDepartment() {
		return currentDepartment;
	}

	public void setCurrentDepartment(Company currentDepartment) {
		this.currentDepartment = currentDepartment;
	}

	public List<Grade> getCurrentGrades() {
		return currentGrades;
	}

	public void setCurrentGrades(List<Grade> currentGrades) {
		this.currentGrades = currentGrades;
	}

	public List<UserGradePower> getCurrentUserGradePowers() {
		return currentUserGradePowers;
	}

	public void setCurrentUserGradePowers(List<UserGradePower> currentUserGradePowers) {
		this.currentUserGradePowers = currentUserGradePowers;
	}

	public List<UserGrade> getCurrentUserGrades() {
		return currentUserGrades;
	}

	public void setCurrentUserGrades(List<UserGrade> currentUserGrades) {
		this.currentUserGrades = currentUserGrades;
	}

	public List<Company> getCurrentGradeDepartments() {
		return currentGradeDepartments;
	}

	public void setCurrentGradeDepartments(List<Company> currentGradeDepartments) {
		this.currentGradeDepartments = currentGradeDepartments;
	}

	public List<Company> getCurrentCompanies() {
		return currentCompanies;
	}

	public void setCurrentCompanies(List<Company> currentCompanies) {
		this.currentCompanies = currentCompanies;
	}

	public List<Company> getCurrentDepartments() {
		return currentDepartments;
	}

	public void setCurrentDepartments(List<Company> currentDepartments) {
		this.currentDepartments = currentDepartments;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * 
	 * 从User对象转换为当前对象
	 * 
	 * @param user
	 * @return this
	 */
	public Map<String, String> convert(Company department) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", department.getId());
		map.put("name", department.getName());
		map.put("comAttribute", department.getComAttribute());
		map.put("centerFlag", department.getCenterFlag());
		return map;
	}

	@Override
	public String toJSONString() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("no", no);
		map.put("name", name);
		map.put("locale", locale == null ? "" : locale.toString());
		// Map department = convert(currentDepartment);
		map.put("company", convert(currentCompany)); // 当前公司
		map.put("centerCode", currentCompany.getId()); // 当前公司代码
		map.put("department", convert(currentDepartment)); // 当前部门
		List<Map<String, ?>> departments = new ArrayList<Map<String, ?>>();
		for (Company department : this.currentDepartments) {
			departments.add(convert(department));
		}
		map.put("departments", departments);

		return new JSONObject(map).toString();
	}

}
