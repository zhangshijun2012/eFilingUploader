package com.sinosoft.util.struts2.action;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sinosoft.efiling.hibernate.entity.Company;
import com.sinosoft.efiling.hibernate.entity.User;
import com.sinosoft.efiling.service.CompanyService;
import com.sinosoft.efiling.service.UserService;
import com.sinosoft.efiling.util.SystemUtils;
import com.sinosoft.efiling.util.UserSessionEntity;
import com.sinosoft.util.CollectionHelper;
import com.sinosoft.util.DateHelper;
import com.sinosoft.util.FieldHelper;
import com.sinosoft.util.Helper;
import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.ObjectHelper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.hibernate.dao.EntityDao;
import com.sinosoft.util.hibernate.entity.Entity;
import com.sinosoft.util.hibernate.paging.PagingEntity;
import com.sinosoft.util.json.JSONArray;
import com.sinosoft.util.service.Service;

/**
 * struts2的action
 * 
 * @author LuoGang
 * 
 * @param <E>
 *            对应的实实例类，即要操作的数据库实例
 * @param <D>
 *            数据库操作类
 * @param <S>
 *            Service类
 * @param <I>
 *            E的主键类型,应当为为String类型，若不是，对id需要进行转换
 */
@SuppressWarnings("rawtypes")
public class EntityActionSupport<E extends Entity<I>, D extends EntityDao<E>, S extends Service<E, D>, I extends Serializable>
		extends ActionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7691499153191254209L;

	public static final String INDEX = "index";
	public static final String LIST = "list";
	public static final String QUERY = "list";
	public static final String VIEW = "view";
	public static final String APPEND = "append";
	public static final String EDIT = "edit";

	public EntityActionSupport() {
		super();
		getLogger();
	}

	/**
	 * 实体类的类型.如果子类在继承此类时明确指定了E,且未覆盖getEntityClass方法,则不需要指定此属性的值.<br />
	 * 否则如果子类中未指定继承的E的明确类型,则需要指定此属性的值或覆盖getEntityClass方法
	 */
	protected Class<E> entityClass;

	/**
	 * 得到实体类型E的Class对象<br />
	 * 注意此方法只有在子类中调用才不会出错.且子类中需要指定E的类型.<br />
	 * 如果子类使用泛型则不需要覆盖此方法，否则需要指定entityClass的值，或覆盖此方法
	 * 
	 * @return E的class，相当与E.class
	 */
	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass() {
		if (entityClass == null) {
			// 得到此类的父类的泛型参数.
			// 因为只能在子类中调用，则getGenericSuperclass即是EntityBaseDao类.
			// 取得本类的泛型参数的类型
			entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		return entityClass;
	}

	/** 创建实例对象 */
	public E createEntity() {
		if (entity == null) {
			try {
				// 根据泛型参数初始化实体对象.
				// 因为struts2的自动注入在entity为null是，自动创建的entity的实体对象的类为Entity，而不是泛型参数传递的类型
				entity = getEntityClass().newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return entity;
	}

	/** 操作的实例对象 */
	protected E entity;
	// protected D dao;
	protected S service;
	protected UserService userService;
	protected CompanyService companyService;

	/** 主键ID */
	protected String id;
	protected String name;
	/** 批量操作时的主键，主要用于批量删除等操作 */
	protected String[] ids;

	/** 查询语句 */
	protected StringBuilder queryString;
	/** 排序语句 */
	protected StringBuilder orderString;
	/** 查询参数 */
	protected List queryParameters;
	/** 分页操作后返回的对象 */
	protected PagingEntity pagingEntity;
	/** 分页查询时的页码 */
	protected int pageIndex;
	/** 分页查询时，每页数量 */
	protected int maxResults;
	/** 查询操作返回的数据对象.若为分页操作，则等于pagingEntity.list() */
	protected List list;

	public void validateIndex() {
	}

	/** 进入首页时的调用 */
	public String index() {

		return INDEX;
	}

	/**
	 * view方法访问前会调用
	 */
	public void validateView() {
		this.entity = service.get(id);
	}

	/**
	 * 查看数据明细
	 * 
	 * @return
	 */
	public String view() {
		return VIEW;
	}

	/**
	 * append方法访问前会调用
	 */
	public void validateAppend() {

	}

	/**
	 * 新建数据
	 * 
	 * @return
	 */
	public String append() {
		return APPEND;
	}

	public void validateSave() {

	}

	/**
	 * 新建后保存数据
	 * 
	 * @return
	 */
	public String save() {
		service.save(entity);
		return dispatchSaveSuccess();
	}

	public void validateEdit() {
		validateView();
	}

	/**
	 * 编辑数据
	 * 
	 * @return
	 */
	public String edit() {
		return EDIT;
	}

	public void validateUpdate() {
	}

	/**
	 * 编辑后保存数据
	 * 
	 * @return
	 */
	public String update() {
		service.update(entity);
		return dispatchSaveSuccess();
	}

	public void validateDelete() {

	}

	/**
	 * 删除数据
	 * 
	 * @return
	 */
	public String delete() {
		service.deleteById(ids);
		return dispatchSaveSuccess();
	}

	/**
	 * <pre>
	 * 主要用于查询方法组装查询语句等操作.
	 * 在子类中涉及到查询方法时,最好在覆盖此方法时调用super.validateQuery,
	 * 用于初始化queryString和parameters对象.
	 * </pre>
	 */
	public void validateQuery() {
		queryString = new StringBuilder(100);
		queryParameters = new ArrayList<Object>();
	}

	/**
	 * 查询数据
	 * 
	 * @return
	 */
	public String query() {
		String queryString = this.queryString.toString();
		if (orderString != null) {
			queryString += (" " + orderString.toString());
		}
		Object[] parameters = this.queryParameters == null || this.queryParameters.isEmpty() ? null
				: this.queryParameters.toArray();
		pagingEntity = service.query(queryString, parameters, pageIndex, maxResults);
		list = pagingEntity.list();
		return LIST;
	}

	/**
	 * 添加一个排序语句
	 * 
	 * @param orderString
	 * @return
	 */
	public StringBuilder addOrderString(String orderString) {
		if (this.orderString == null) {
			this.orderString = new StringBuilder();
			if (!orderString.trim().toUpperCase().startsWith("ORDER BY ")) {
				this.orderString.append("ORDER BY ");
			}
		}
		this.orderString.append(orderString);
		return this.orderString;
	}

	/**
	 * 增加排序条件
	 * 
	 * @param column
	 *            排序的列名
	 * @param desc
	 *            是否降序
	 * @return
	 */
	public StringBuilder addOrderString(String column, boolean desc) {
		String orderString = column + " ";
		orderString += desc ? "DESC" : "ASC";
		return this.addOrderString(orderString);
	}

	/**
	 * 增加排序条件
	 * 
	 * @param column
	 *            排序的列名
	 * @param desc
	 *            "ASC"或"DESC"
	 * @return
	 */
	public StringBuilder addOrderString(String column, String desc) {
		String orderString = column + " " + StringHelper.trim(desc);
		return this.addOrderString(orderString);
	}

	/**
	 * 增加一个类似于between x and y的查询语句.围为了防止数据库的差异,使用property >= x and property <= y的语句
	 * 
	 * @param property
	 * @param values
	 *            长度最多为2的数组
	 * @return
	 */
	public StringBuilder addBetweenQuery(String property, Object[] values) {
		if (values == null || values.length <= 0) return this.queryString;
		String queryString = null;
		List<Object> parameters = new ArrayList<Object>();
		if (!ObjectHelper.isEmpty(values[0])) {
			queryString = property + " >= ?";
			parameters.add(values[0]);
		}
		if (values.length > 1 && !ObjectHelper.isEmpty(values[1])) {
			queryString += (queryString == null ? "" : " AND ") + property + " <= ?";
			parameters.add(values[1]);
		}
		if (queryString == null) return this.queryString;
		queryString = (StringHelper.isEmpty(this.queryString) ? "" : "AND ") + "(" + queryString + ")";

		return addQueryString(queryString, parameters.toArray());
	}

	/**
	 * 增加一个类似于between x and y的查询语句.围为了防止数据库的差异,使用property >= x and property <= y的语句
	 * 
	 * @param property
	 * @param values
	 *            长度最多为2的数组
	 * @param cls
	 *            参数values要转换为的类型.在action中，数组都是String[],需要指定要转换的类型
	 * @return
	 */
	public StringBuilder addBetweenQuery(String property, Object[] values, Class<?> cls) {
		if (values == null || values.length <= 0) return this.queryString;
		String queryString = null;
		List<Object> parameters = new ArrayList<Object>();
		if (!ObjectHelper.isEmpty(values[0])) {
			queryString = property + " >= ?";
			parameters.add(ObjectHelper.cast(cls, values[0]));
		}
		if (values.length > 1 && !ObjectHelper.isEmpty(values[1])) {
			queryString += (queryString == null ? "" : " AND ") + property + " <= ?";
			Object value = ObjectHelper.cast(cls, values[1]);
			if (value != null && value.getClass() == Date.class) {
				// 日期
				Date date = (Date) value;
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int min = c.get(Calendar.MINUTE);
				int sec = c.get(Calendar.SECOND);
				int ms = c.get(Calendar.MILLISECOND);
				if (hour == 0 && min == 0 && sec == 0 && ms == 0) {
					// 查询日期的期间时,如果末尾的日期没有指定时分秒,则指定时间为23:59:59
					value = DateHelper.clearToEnd(date);
				}
			}
			parameters.add(value);
		}
		if (queryString == null) return this.queryString;
		queryString = (StringHelper.isEmpty(this.queryString) ? "" : "AND ") + "(" + queryString + ")";

		return addQueryString(queryString, parameters.toArray());
	}

	/**
	 * 增加一个like查询语句. AND UPPER(property) LIKE %value.toUpperCase()%
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public StringBuilder addLikeQuery(String property, String value) {
		if (StringHelper.isEmpty(value)) return this.queryString;
		String queryString = (StringHelper.isEmpty(this.queryString) ? "" : "AND ") + "UPPER(";
		queryString += property + ") LIKE ?";
		return addQueryString(queryString, "%" + value.toUpperCase() + "%");
	}

	/**
	 * 增加关于分公司查询条件的权限
	 * 
	 * @param property company的属性名
	 * @param desc
	 * @return
	 */
	public StringBuilder addCompanyQuery(String property) {
		UserSessionEntity user = getCurrentUserSession();
		// companies = user.getCurrentCompanies();
		return addQuery(property, user.getCurrentCompanies().toArray());
	}

	/**
	 * 增加关于分公司id的查询条件的权限
	 * 
	 * @param column companyId的列名
	 * @param desc
	 * @return
	 */
	public StringBuilder addCompanyIdQuery(String column) {
		UserSessionEntity user = getCurrentUserSession();
		List<Company> companies = user.getCurrentCompanies();
		Object[] ids = new Object[companies.size()];
		int index = 0;
		for (Company com : companies) {
			ids[index++] = com.getId();
		}
		return addQuery(column, ids);
	}

	/**
	 * 增加查询条件,默认查询语句为AND property=value.<br>
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public StringBuilder addQuery(String property, Object value) {
		// if (StringHelper.isEmpty(value)) return this.queryString;
		// String queryString = StringHelper.isEmpty(this.queryString) ? "" : "AND ";
		// queryString += property + " = ?";
		// return addQueryString(queryString, value);
		return this.addQuery("AND", property, "=", value);
	}

	/**
	 * 
	 * 增加查询条件.增加的条件为andOr + " " + property + " " + operator + " ? ";
	 * 
	 * @param andOr
	 * @param property
	 * @param operator
	 * @param value
	 * @return
	 */
	public StringBuilder addQuery(String andOr, String property, String operator, Object value) {
		if (ObjectHelper.isEmpty(value)) return this.queryString;
		if (value.getClass().isArray()) return this.addQuery(andOr, property, operator, Helper.toObjectArray(value));
		if (value instanceof Collection) return this
				.addQuery(andOr, property, operator, ((Collection) value).toArray());
		String queryString = (StringHelper.isEmpty(this.queryString) ? "" : andOr);
		queryString = queryString + " " + property + " " + operator + " ? ";
		return addQueryString(queryString, value);
	}

	/**
	 * 增加查询条件.增加的条件为AND property IN (?, ?...)
	 * 
	 * @param property
	 * @param values
	 * @return
	 */
	public StringBuilder addQuery(String property, Object[] values) {
		if (ObjectHelper.isEmpty(values)) return this.queryString;
		String queryString = (StringHelper.isEmpty(this.queryString) ? "" : "AND ") + property + " IN (?"
				+ StringHelper.copy(", ?", values.length - 1) + ") ";
		return addQueryString(queryString, values);
	}

	/**
	 * 增加查询条件.增加的条件为andOr + " " + property + " " + operator + " (?, ?...) ";
	 * 
	 * @param andOr
	 * @param property
	 * @param operator
	 * @param values
	 * @return
	 */
	public StringBuilder addQuery(String andOr, String property, String operator, Object[] values) {
		if (ObjectHelper.isEmpty(values)) return this.queryString;
		String queryString = (StringHelper.isEmpty(this.queryString) ? "" : andOr);
		queryString = queryString + " " + property + " " + operator + " (?"
				+ StringHelper.copy(", ?", values.length - 1) + ") ";
		return addQueryString(queryString, values);
	}

	/**
	 * 增加查询语句
	 * 
	 * @param queryString
	 * @return
	 */
	public StringBuilder addQueryString(String queryString) {
		if (this.queryString == null) this.queryString = new StringBuilder();
		if (queryString.trim().toUpperCase().startsWith("ORDER BY ")) {
			return this.addOrderString(queryString);
		}
		return this.queryString.append(" ").append(queryString);
	}

	/**
	 * 增加一个查询条件.如果参数parameter为空则不会增加查询
	 * 
	 * @param queryString
	 * @param parameter
	 * @return
	 */
	public StringBuilder addQueryString(String queryString, Object parameter) {
		if (!StringHelper.isEmpty(parameter)) {
			return addQueryString(queryString, new Object[] { parameter });
		}
		return this.queryString;
	}

	/**
	 * 增加一个查询条件
	 * 
	 * @param queryString
	 * @param parameters
	 * @return
	 */
	public StringBuilder addQueryString(String queryString, Object[] parameters) {
		if (this.queryString == null) this.queryString = new StringBuilder();
		if (this.queryParameters == null) this.queryParameters = new ArrayList<Object>();

		if (!ObjectHelper.isEmpty(parameters)) {
			this.queryString.append(" ").append(queryString);
			int index = 0;
			for (Object o : parameters) {
				// 处理字符串,去掉前后的空格
				if (o != null && o instanceof String) {
					parameters[index] = StringHelper.trim(o);
				}
				index++;
			}
			CollectionHelper.add(this.queryParameters, parameters);
		}
		return this.queryString;
	}

	public E getEntity() {
		return entity;
	}

	public void setEntity(E entity) {
		this.entity = entity;
	}

	public S getService() {
		return service;
	}

	public void setService(S service) {
		this.service = service;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public StringBuilder getQueryString() {
		return queryString;
	}

	public List getQueryParameters() {
		return queryParameters;
	}

	public String[] getIds() {
		return ids;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}

	public PagingEntity getPagingEntity() {
		return pagingEntity;
	}

	public void setPagingEntity(PagingEntity pagingEntity) {
		this.pagingEntity = pagingEntity;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public List getList() {
		return list;
	}

	public void setList(List results) {
		this.list = results;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public CompanyService getCompanyService() {
		return companyService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	/**
	 * 输出HTML内容
	 * 
	 * @param s
	 */
	public void write(String s) {
		try {
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding(SystemUtils.ENCODING);
			getResponse().setHeader("Content-Type", "text/html; charset=" + SystemUtils.ENCODING);
			getResponse().getWriter().write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 异常处理
	 * 
	 * @param e
	 * @return
	 */

	public String dispatchException(Exception e) {
		if (e != null) e.printStackTrace();
		if (!this.hasActionErrors()) this.addActionError("操作失败,请稍候重试...");
		return Action.ERROR;
	}

	/**
	 * 保存成功执行的调用.默认情况下返回null,使strut2不进行跳转.直接使用response进行输出
	 * 
	 * @param json
	 *            输出的语句
	 * @return null 表示成功
	 */
	public String dispatchSuccess(Object json) {
		try {
			getResponse().setContentType("text/plain"); // 设置为普通文本
			getResponse().setCharacterEncoding(SystemUtils.ENCODING);
			getResponse().getWriter().write(StringHelper.trim(json));
		} catch (IOException e) {
			e.printStackTrace();
			this.addActionError("遇到I/O异常,但是操作可能已经成功,请检查数据是否已更改!");
			return Action.ERROR;
		}
		return null;
	}

	private StringBuilder outputMessages;

	/**
	 * 添加一个输出消息
	 * 
	 * @param message
	 * @return
	 */
	public StringBuilder addOutputMessage(String message) {
		if (outputMessages == null) outputMessages = new StringBuilder();
		if (message != null) outputMessages.append(message);
		return outputMessages;
	}

	/**
	 * 得到输出消息
	 * 
	 * @return
	 */
	public String getOutputMessage() {
		return outputMessages == null ? null : outputMessages.toString();
	}

	/**
	 * 保存成功的调用.提示消息通过getActionMessages得到，如果没有则使用getText("global.success")
	 * 
	 * @return
	 */
	public String dispatchSaveSuccess() {
		JSONObject json = new JSONObject();
		json.put("success", true);
		json.put("id", entity == null ? id : entity.getId());
		if (getOutputMessage() != null) json.put("message", getOutputMessage());
		return dispatchSuccess(json.toString());
	}

	/**
	 * 保存成功后的调用
	 * 
	 * @param message
	 *            提示消息
	 * @return
	 */
	public String dispatchSaveSuccess(String message) {
		JSONObject json = new JSONObject();
		json.put("success", true);
		json.put("id", entity == null ? id : entity.getId());
		json.put("message", message);
		return dispatchSuccess(json.toString());
	}

	/**
	 * 得到异常信息
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		StringBuilder msg = new StringBuilder();
		for (Entry<String, List<String>> error : getFieldErrors().entrySet()) {
			// String key = error.getKey();
			for (String value : error.getValue()) {
				// msg.append(key).append(": ");
				msg.append(value).append("\n");
			}
		}

		for (String value : getActionErrors()) {
			msg.append(value).append("\n");
		}
		return msg.toString();
	}

	/**
	 * 将异常信息转换为JSON对象
	 * 
	 * @return
	 */
	public JSONObject getErrorJSONObject() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", false);
		map.put("message", getErrorMessage());
		return new JSONObject(map);
	}

	/**
	 * 得到request
	 * 
	 * @return
	 */
	public HttpServletRequest getRequest() {
		return ServletActionContext.getRequest();
	}

	/**
	 * 得到path的绝对路径
	 * 
	 * @param path 相对路径
	 * @return
	 */
	public String getContextPath(String path) {
		path = StringHelper.trim(path);
		if (path.charAt(0) == '/') path = path.substring(1);
		String contextPath = getRequest().getContextPath();
		if (!contextPath.endsWith("/")) contextPath += "/";
		return contextPath + path;
	}

	/**
	 * 得到response
	 * 
	 * @return
	 */
	public HttpServletResponse getResponse() {
		return ServletActionContext.getResponse();
	}

	/**
	 * 得到session
	 * 
	 * @return
	 */
	public HttpSession getSession() {
		return this.getRequest().getSession();
	}

	/** 当前登录user对象 */
	private User currentUser;

	/**
	 * 得到当前登录的用户对象
	 * 
	 * @return User 从数据库中重新查询出的User对象
	 */
	public User getCurrentUser() {
		if (currentUser == null) {
			currentUser = userService.get(getCurrentUserSession().getId());
		}
		return currentUser;
	}

	/**
	 * 得到当前登录的用户的session中的对象
	 * 
	 * @return User 在session中保存的UserSessionEntity对象
	 */
	public UserSessionEntity getCurrentUserSession() {
		UserSessionEntity u = null;
		try {
			u = (UserSessionEntity) getSession().getAttribute(SystemUtils.USER_SESSION_NAME);
		} catch (Exception e) {
			// 可能是没有session
			// UserSessionEntity u = new UserSessionEntity();
			// return u;
		}
		if (u == null) u = new UserSessionEntity();
		return u;
	}

	/**
	 * 得到当前登录公司下的所有内部部门
	 * 
	 * @return
	 */
	public List<Company> getCurrentInternalDepartments() {
		return this.getCurrentInternalDepartments(false);
	}

	/**
	 * 
	 * 得到当前登录公司下的所有内部部门
	 * 
	 * @param refresh
	 *            是否强制刷新数据,如果为true则不会从session缓存中读取数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Company> getCurrentInternalDepartments(boolean refresh) {
		UserSessionEntity u = getCurrentUserSession();
		Company c = u.getCurrentCompany();
		if (c == null) return null;
		String sessionName = "CurrentInternalDepartments" + c.getId();
		List<Company> departments = (List<Company>) getSession().getAttribute(sessionName);
		if (refresh || departments == null) {
			departments = companyService.getInternal(u.getCurrentCompanies().toArray(new Company[1]));
			getSession().setAttribute(sessionName, departments);
		}
		return departments;
	}

	/**
	 * 从缓存中读取当前部门的JSON数组
	 * 
	 * @return
	 */
	public JSONArray getCurrentInternalDepartmentsJSON() {
		return getCurrentInternalDepartmentsJSON(false);
	}

	/**
	 * 将当前的部门数据转换为json数组
	 * 
	 * @param refresh
	 *            是否强制刷新数据,如果为true则不会从session缓存中读取数据
	 * @return
	 */
	public JSONArray getCurrentInternalDepartmentsJSON(boolean refresh) {
		UserSessionEntity u = getCurrentUserSession();
		Company c = u.getCurrentCompany();
		if (c == null) return null;
		String sessionName = "CurrentInternalDepartmentsJSON" + c.getId();
		JSONArray json = (JSONArray) getSession().getAttribute(sessionName);
		if (refresh || json == null) {
			try {
				List<Company> departments = getCurrentInternalDepartments(refresh); // companyService.getInternal();
				json = new JSONArray();
				for (Company dept : departments) {
					JSONObject o = dept.toJSONObject();
					o.put("parentId", dept.getParent() == null ? null : dept.getParent().getId());
					dept = CompanyService.getCompany(dept); // 所在分公司
					o.put("companyId", dept == null ? null : dept.getId());
					json.put(o);
				}
			} catch (Exception e) {
				// 可能是getCurrentInternalDepartments缓存造成的无法读取上级节点的数据,因此重新从数据库中读取数据
				e.printStackTrace();
				return getCurrentInternalDepartmentsJSON(true);
			}
			getSession().setAttribute(sessionName, json);
		}
		return json;
	}

	/**
	 * 创建一个资源Map类
	 * 
	 * @param m
	 * @return
	 */
	public ResourcesMap createResourcesMap(Map<? extends String, ? extends String> m) {
		return new ResourcesMap(m);
	}

	/**
	 * 资源文件Map对象。当调用put方法时，则会自动会调用getText(value)作为存放到map中的value
	 * 
	 * @author LuoGang
	 * 
	 */
	public class ResourcesMap extends LinkedHashMap<String, String> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6813160466592767472L;

		public ResourcesMap() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ResourcesMap(Map<? extends String, ? extends String> m) {
			super();
			putAll(m);
		}

		@Override
		public String put(String key, String value) {
			return super.put(key, getText(value));
		}

		@Override
		public void putAll(Map<? extends String, ? extends String> m) {
			if (m == null || m.isEmpty()) return;
			String text;
			for (Map.Entry<? extends String, ? extends String> e : m.entrySet()) {
				text = getText(e.getValue());
				put(e.getKey(), text == null ? e.getValue() : text);
			}
		}
	}

	private static Map<Class<?>, Logger> LOGGERS = new HashMap<Class<?>, Logger>();

	/**
	 * 得到日志记录对象
	 * 
	 * @return
	 */
	public Logger getLogger() {
		if (logger == null) {
			Class<?> cls = this.getClass();
			logger = LOGGERS.get(cls);
			if (logger == null) {
				logger = LoggerFactory.getLogger(cls);
				LOGGERS.put(cls, logger);
			}
		}
		return logger;
	}

	/** 日志记录对象 */
	protected Logger logger;

	/** 定义默认数字样式的配置 */
	public static final String DEFAULT_DATE_FORMAT_KEY = "global.format.date";

	/**
	 * 格式化日期
	 * 
	 * @param date
	 *            日期对象
	 * @param formatKey
	 *            资源文件中配置日期的格式的关键字或者日期格式本身
	 * @return
	 */
	public String formatDate(Date date, String formatKey) {
		if (date == null) return null;
		String pattern;
		if (StringHelper.isEmpty(formatKey)) pattern = getText(DEFAULT_DATE_FORMAT_KEY);
		else pattern = getText(formatKey, formatKey);
		return StringHelper.isEmpty(pattern) ? DateHelper.format(date) : DateHelper.format(date, pattern);
	}

	/**
	 * 用默认的格式对日期进行格式化
	 * 
	 * @param date
	 * @return
	 */
	public String formatDate(Date date) {
		return formatDate(date, DEFAULT_DATE_FORMAT_KEY);
	}

	/** 定义默认数字样式的配置 */
	public static final String DEFAULT_NUMBER_FORMAT_KEY = "global.format.double";

	/** 定义默认整数样式的配置 */
	public static final String DEFAULT_INTEGER_FORMAT_KEY = "global.format.int";

	/** 定义默认百分比样式的配置 */
	public static final String DEFAULT_PERCENT_FORMAT_KEY = "global.format.percent";

	/**
	 * 格式化数字
	 * 
	 * @param number
	 *            数字
	 * @param formatKey
	 *            资源文件中配置日期的格式的关键字或者格式本身
	 * @return
	 */
	public String formatNumber(Object number, String formatKey) {
		if (number == null) return "";
		String pattern;
		if (StringHelper.isEmpty(formatKey)) pattern = getText(DEFAULT_NUMBER_FORMAT_KEY);
		else pattern = getText(formatKey, formatKey);
		return StringHelper.isEmpty(pattern) ? NumberHelper.format(number) : NumberHelper.format(number, pattern);
	}

	/**
	 * 用默认的格式初始化数字
	 * 
	 * @param number
	 * @return
	 */
	public String formatNumber(Object number) {
		return formatNumber(number, DEFAULT_NUMBER_FORMAT_KEY);
	}

	/**
	 * 格式化整数
	 * 
	 * @param number
	 * @return
	 */
	public String formatInteger(Object number) {
		return formatNumber(number, DEFAULT_INTEGER_FORMAT_KEY);
	}

	/**
	 * 格式化百分数
	 * 
	 * @param number
	 * @return
	 */
	public String formatPercent(Object number) {
		return formatNumber(number, DEFAULT_PERCENT_FORMAT_KEY);
	}

	/** 是否 */
	public static final Map<String, String> YES_NO = new LinkedHashMap<String, String>();

	/**
	 * 得到propertyName属性的资源文件描述
	 * 
	 * @param propertyName
	 *            map类型的属性名
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getTextMap(String propertyName) {
		// if ("STATUS".equalsIgnoreCase(propertyName)) {
		// return getTextMap(STATUS);
		// }
		if ("YES_NO".equalsIgnoreCase(propertyName)) {
			return getTextMap(SystemUtils.YES_NO);
		}
		Object value = FieldHelper.getFieldValue(this, propertyName);
		return value == null ? null : getTextMap((Map<String, String>) value);
	}

	/**
	 * 根据map数据,将起value转换为资源文件中的描述
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, String> getTextMap(Map<String, String> map) {
		Map<String, String> textMap = new LinkedHashMap<String, String>();
		String value;
		String text;
		for (String key : map.keySet()) {
			value = map.get(key);
			text = getText(value);
			textMap.put(key, text == null ? value : text);
		}
		return textMap;
	}
}
