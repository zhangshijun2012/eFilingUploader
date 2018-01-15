package com.sinosoft.efiling.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sinosoft.efiling.hibernate.dao.GradeDao;
import com.sinosoft.efiling.hibernate.dao.GradeTaskDao;
import com.sinosoft.efiling.hibernate.dao.MenuDao;
import com.sinosoft.efiling.hibernate.dao.TaskDao;
import com.sinosoft.efiling.hibernate.dao.UserDao;
import com.sinosoft.efiling.hibernate.dao.UserGradeDao;
import com.sinosoft.efiling.hibernate.dao.UserGradePowerDao;
import com.sinosoft.efiling.hibernate.dao.UserGradeTaskDao;
import com.sinosoft.efiling.hibernate.entity.Company;
import com.sinosoft.efiling.hibernate.entity.Grade;
import com.sinosoft.efiling.hibernate.entity.GradeTask;
import com.sinosoft.efiling.hibernate.entity.Menu;
import com.sinosoft.efiling.hibernate.entity.User;
import com.sinosoft.efiling.hibernate.entity.UserGrade;
import com.sinosoft.efiling.hibernate.entity.UserGradePower;
import com.sinosoft.efiling.hibernate.entity.UserGradeTask;
import com.sinosoft.efiling.util.SystemUtils;
import com.sinosoft.efiling.util.UserSessionEntity;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.hibernate.entity.EntityIdComparator;
import com.sinosoft.util.hibernate.entity.TreeEntity.Visitor;
import com.sinosoft.util.service.ServiceSupport;

public class UserService extends ServiceSupport<User, UserDao> {
	// CompanyDao companyDao;

	GradeDao gradeDao;
	GradeTaskDao gradeTaskDao;
	MenuDao menuDao;
	TaskDao taskDao;
	UserGradeDao userGradeDao;
	UserGradePowerDao userGradePowerDao;
	UserGradeTaskDao userGradeTaskDao;

	/**
	 * 是否为最终操作菜单
	 * 
	 * @param menu
	 * @return
	 */
	public static boolean isActionMenu(Menu menu) {
		return SystemUtils.ACTION_MENU_FLAG.equals(menu.getFlag());
	}

	/**
	 * 根据userNo查询得到user对象.
	 * 
	 * @param userNo
	 * @return
	 */
	public UserSessionEntity getSessionEntity(String userNo) {
		// User user = dao.get(userId);
		User user = dao.getByNo(userNo);
		UserSessionEntity userSupport = changeCurrentDepartment(user, user.getDepartment().getId());
		return userSupport;
	}

	/** 用于判断岗位是否有操作eFiling系统的权限SQL */
	public static final String GRADE_EXISTS_SQL;
	static {
		StringBuilder b = new StringBuilder(100);
		b.append("SELECT GRADECODE FROM UTIGRADETASK WHERE TASKCODE IN ");
		b.append("( SELECT TASKCODE FROM UTIMENU WHERE SYSTEMCODE = ? AND VALIDSTATUS = ? )");
		b.append(" UNION ");
		b.append("SELECT GRADECODE FROM UTIUSERGRADETASK WHERE TASKCODE IN ");
		b.append("( SELECT TASKCODE FROM UTIMENU WHERE SYSTEMCODE = ? AND VALIDSTATUS = ? )");
		b.insert(0, "SELECT * FROM ( ");
		b.append(") T WHERE GRADECODE = ? ");
		GRADE_EXISTS_SQL = b.toString();
	}
	/** GRADE_EXISTS_SQL的参数 */
	public static final Object[] GRADE_EXISTS_PARAMETERS = new Object[] { SystemUtils.SYSTEM_CODE,
			SystemUtils.STATUS_VALID, SystemUtils.SYSTEM_CODE, SystemUtils.STATUS_VALID, null };

	public UserSessionEntity changeCurrentDepartment(User user, String departmentId) {
		UserSessionEntity userSupport;
		user = dao.get(user.getId()); // 重新读取数据
		// if (user instanceof UserSessionEntity) {
		// userSupport = (UserSessionEntity) user;
		// } else {
		userSupport = new UserSessionEntity(user);
		// }

		Company department = companyDao.get(departmentId);
		userSupport.setCurrentDepartment(department);
		Company company = CompanyService.getCompany(department);
		company.getComName(); // 防止延迟加载
		userSupport.setCurrentCompany(company);

		final List<Company> currentCompanies = new ArrayList<Company>();
		currentCompanies.add(company);
		List<Company> companies = companyDao.queryByProperty("centerFlag", SystemUtils.COMPANY_FLAG);
		for (Company com : companies) {
			if (com.equals(company) || com.getId().equals(company.getId())) continue;
			if (company.isParentOf(com)) {
				currentCompanies.add(com);
			}
		}
		EntityIdComparator.sort(currentCompanies);
		userSupport.setCurrentCompanies(currentCompanies);

		List<UserGrade> userGrades = new ArrayList<UserGrade>();
		List<Company> userGradeDepartments = new ArrayList<Company>();
		List<Company> currentDepartments = new ArrayList<Company>(); // 所有可进行切换的岗位所在部门
		List<Grade> grades = new ArrayList<Grade>();
		List<UserGradePower> userGradePowers = new ArrayList<UserGradePower>();
		Grade grade;
		Company gradeDepartment;

		Object[] parameters = GRADE_EXISTS_PARAMETERS;
		for (UserGrade userGrade : user.getUserGrades()) {
			grade = userGrade.getGrade();
			/* 仅加载电子档案系统的岗位.岗位编号为F开头的则视为电子档案系统中使用的岗位 */
			// if (!grade.getId().startsWith(SystemUtils.GRADE_PREFIX)) {
			// continue;
			// }
			parameters[parameters.length - 1] = grade.getId();
			if (null == dao.uniqueResultSQL(GRADE_EXISTS_SQL, parameters)) continue; // 此岗位没有电子档案的权限

			gradeDepartment = userGrade.getCompany();
			gradeDepartment.getComName();
			if (!currentDepartments.contains(gradeDepartment)) currentDepartments.add(gradeDepartment);

			/* 当前登录机构不是这个岗位所在的机构或其父机构，则不需要该岗位 */
			if (!departmentId.equals(gradeDepartment.getId()) && !department.isParentOf(gradeDepartment)) {
				continue;
			}

			// 读取数据，防止延迟加载造成的BUG
			userGrade.getValidStatus();
			grade.getGradeLevel();
			userGrades.add(userGrade);
			grades.add(grade);
			if (!userGradeDepartments.contains(gradeDepartment)) userGradeDepartments.add(gradeDepartment);

			for (UserGradePower userGradePower : userGrade.getUserGradePowers()) {
				userGradePower.getPermitComCode();
				userGradePowers.add(userGradePower);
			}
		}

		/* 按编号排序 */
		EntityIdComparator.sort(currentDepartments);
		// Collections.sort(currentDepartments, new Comparator<Company>() {
		// public int compare(Company o1, Company o2) {
		// return o1.getId().compareToIgnoreCase(o2.getId());
		// }
		// });
		userSupport.setCurrentDepartments(currentDepartments);

		userSupport.setCurrentGrades(grades);
		userSupport.setCurrentUserGrades(userGrades);
		userSupport.setCurrentGradeDepartments(userGradeDepartments);
		userSupport.setCurrentUserGradePowers(userGradePowers);
		userSupport.toJSONString(); // 防止延迟加载异常
		return userSupport;
	}

	/**
	 * 查询userId所有可登录的机构
	 * 
	 * @param userId
	 * @return
	 */
	public List<Company> queryCompanies(String userId) {
		final List<Company> companies = new ArrayList<Company>();
		UserSessionEntity user = getSessionEntity(userId);
		Company company = user.getCurrentCompany();

		// company下的所有子公司也可以登录
		company.visit(new Visitor<Company>() {
			public void visit(Company e) {
				if (CompanyService.isCompany(e)) {
					companies.add(e);
				}
			}
		});

		// 配置的权限机构中也需要加入

		// 对结果按主键排序
		Collections.sort(companies, new Comparator<Company>() {
			public int compare(Company o1, Company o2) {
				// 按主键排序
				return o1.getId().compareToIgnoreCase(o2.getId());
			}
		});
		return companies;
	}

	/**
	 * 查询当前用户可加载的菜单
	 * 
	 * @param user
	 * @param parentId
	 * @return
	 */
	public List<Menu> queryMenus(UserSessionEntity user, String parentId) {
		List<Grade> grades = user.getCurrentGrades();
		if (grades == null || grades.isEmpty()) {
			return null;
		}

		// hql语句
		StringBuffer hql = new StringBuffer();
		// 参数
		List<Serializable> parameters = new ArrayList<Serializable>();

		hql.append("FROM ").append(Menu.class.getName()).append(" ");
		hql.append("WHERE systemCode = ? AND validStatus = ? ");
		parameters.add(SystemUtils.SYSTEM_CODE);
		parameters.add(SystemUtils.STATUS_VALID);
		hql.append("AND (");

		/* 查询岗位的权限 */
		hql.append("task IN (");
		hql.append("	SELECT task FROM ").append(GradeTask.class.getName()).append(" WHERE value = ?");
		hql.append("	AND grade IN (?").append(StringHelper.copy(", ?", grades.size() - 1)).append(") ) ");
		parameters.add(SystemUtils.STATUS_VALID);
		parameters.addAll(grades);

		/* 查询用户配置的特殊权限 */
		List<Company> gradeDepartments = user.getCurrentGradeDepartments();
		// if (gradeDepartments != null && !gradeDepartments.isEmpty()) {
		// 可能在UserGradeTask表中有特殊权限
		int size = gradeDepartments.size();
		hql.append("OR task IN (");
		hql.append("	SELECT task FROM ").append(UserGradeTask.class.getName()).append(" WHERE value = ?");
		hql.append("	AND userGrade.user = ? ");
		hql.append("	AND userGrade.company IN (?").append(StringHelper.copy(", ?", size - 1)).append(") ) ");

		parameters.add(SystemUtils.STATUS_VALID);
		parameters.add(user);
		parameters.addAll(gradeDepartments);
		// }

		hql.append(") ");

		hql.append("AND parent.id = ");

		if (StringHelper.isEmpty(parentId)) {
			// 加载第一级菜单,如果上级菜单编号与当前菜单是一致的，则表示为顶级菜单
			hql.append("id");
		} else {
			hql.append("? AND parent.id <> id");
			parameters.add(parentId);
		}

		hql.append(" ORDER BY displayNo");
		@SuppressWarnings("unchecked")
		List<Menu> menus = (List<Menu>) dao.query(hql.toString(), parameters.toArray());
		return menus;
	}

	public GradeDao getGradeDao() {
		return gradeDao;
	}

	public void setGradeDao(GradeDao gradeDao) {
		this.gradeDao = gradeDao;
	}

	public GradeTaskDao getGradeTaskDao() {
		return gradeTaskDao;
	}

	public void setGradeTaskDao(GradeTaskDao gradeTaskDao) {
		this.gradeTaskDao = gradeTaskDao;
	}

	public MenuDao getMenuDao() {
		return menuDao;
	}

	public void setMenuDao(MenuDao menuDao) {
		this.menuDao = menuDao;
	}

	public TaskDao getTaskDao() {
		return taskDao;
	}

	public void setTaskDao(TaskDao taskDao) {
		this.taskDao = taskDao;
	}

	public UserGradeDao getUserGradeDao() {
		return userGradeDao;
	}

	public void setUserGradeDao(UserGradeDao userGradeDao) {
		this.userGradeDao = userGradeDao;
	}

	public UserGradePowerDao getUserGradePowerDao() {
		return userGradePowerDao;
	}

	public void setUserGradePowerDao(UserGradePowerDao userGradePowerDao) {
		this.userGradePowerDao = userGradePowerDao;
	}

	public UserGradeTaskDao getUserGradeTaskDao() {
		return userGradeTaskDao;
	}

	public void setUserGradeTaskDao(UserGradeTaskDao userGradeTaskDao) {
		this.userGradeTaskDao = userGradeTaskDao;
	}

}
