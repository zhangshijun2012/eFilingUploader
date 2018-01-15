package com.sinosoft.efiling.struts2.action;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONObject;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.util.LocalizedTextUtil;
import com.sinosoft.efiling.hibernate.dao.UserDao;
import com.sinosoft.efiling.hibernate.entity.Grade;
import com.sinosoft.efiling.hibernate.entity.User;
import com.sinosoft.efiling.service.UserService;
import com.sinosoft.efiling.util.SystemUtils;
import com.sinosoft.efiling.util.UserSessionEntity;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.struts2.action.EntityActionSupport;

public class UserAction extends EntityActionSupport<User, UserDao, UserService, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1976083342528060499L;

	String parentId;

	/** 选择的部门 */
	String currentDepartmentId;

	/**
	 * 加载菜单
	 * 
	 * @return
	 */
	public String loadMenus() {
		UserSessionEntity user = getCurrentUserSession();
		// if (user == null) {
		// return Action.SUCCESS;
		// }
		List<Grade> grades = user.getCurrentGrades();
		if (grades == null || grades.isEmpty()) {
			return Action.SUCCESS;
		}
		this.list = service.queryMenus(user, parentId);
		return Action.SUCCESS;
	}

	/**
	 * 进入首页
	 */
	public String index() {
		SystemUtils.initializeUserSession(getRequest());
		UserSessionEntity user = getCurrentUserSession();
		if (!StringHelper.isEmpty(currentDepartmentId)) {
			// 切换机构
			user = service.changeCurrentDepartment(user, currentDepartmentId);
			SystemUtils.initializeUserSession(getSession(), user);
			getSession().setAttribute(SystemUtils.LOGINED_SESSION_NAME, true);
			// user = getCurrentUserSession();
		}
		user.setLocale(getLocale());
		return INDEX;
	}

	/**
	 * 登录
	 * 
	 * @return
	 */
	public String login() {
		index();
		return "login";
	}

	/**
	 * 登出
	 * 
	 * @return
	 */
	public String logout() {
		getSession().invalidate();
		return "logout";
	}

	/**
	 * 得到当前语言下资源文件中配置的所有信息
	 * 
	 * @return
	 */
	public String getResources() {
		// 得到当前的语言文件
		ResourceBundle resource = LocalizedTextUtil.findResourceBundle(SystemUtils.DEFAULT_RESOURCE_BUNDLE_NAME,
				getLocale());
		Enumeration<String> keys = resource.getKeys();
		String key;
		Map<String, String> map = new LinkedHashMap<String, String>();
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			map.put(key, resource.getString(key));
		}
		return this.dispatchSuccess(new JSONObject(map));
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getCurrentDepartmentId() {
		return currentDepartmentId;
	}

	public void setCurrentDepartmentId(String currentDepartmentId) {
		this.currentDepartmentId = currentDepartmentId;
	}
}
