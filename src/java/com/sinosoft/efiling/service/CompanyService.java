package com.sinosoft.efiling.service;

import java.util.List;

import com.sinosoft.efiling.hibernate.dao.CompanyDao;
import com.sinosoft.efiling.hibernate.entity.Company;
import com.sinosoft.efiling.util.SystemUtils;
import com.sinosoft.util.Helper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.service.ServiceSupport;

public class CompanyService extends ServiceSupport<Company, CompanyDao> {
	/** 是否为公司 */
	public static boolean isCompany(Company department) {
		return SystemUtils.COMPANY_FLAG.equals(department.getCenterFlag());
	}

	/**
	 * 得到company部门所在的公司
	 * 
	 * @param department
	 * @return
	 */
	public static Company getCompany(Company department) {
		// Company company = this;
		try {
			while (!isCompany(department)) {
				department = department.getParent();
				if (department == null) {
					// 未找到或者循环到根节点
					return null;
				}

				if (department.getParent().getId().equals(department.getId())) {
					// 防止进入死循环
					return isCompany(department) ? department : null;
				}
			}
		} catch (Exception e) {
			// TODO
			return null;
		}
		return department;
	}

	/**
	 * 根据部门编号得到所在的分公司
	 * 
	 * @param departmentId
	 * @return
	 */
	public Company getCompany(String departmentId) {
		Company department = companyDao.get(departmentId);
		if (department == null) return null;
		return getCompany(department);
	}

	/**
	 * 查询所有内部机构
	 * 
	 * @return
	 */
	public List<Company> getInternal() {
		// "status", SystemUtils.STATUS_VALID },
		return this.dao.queryByProperty(new Object[][] { { "comAttribute", SystemUtils.COMPANY_INTERNAL },
				{ "ORDER BY", "id" } });
	}

	/**
	 * 查询归属在companyIds下的所有部门信息
	 * 
	 * @param companyIds
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Company> getInternal(String[] companyIds) {
		// "status", SystemUtils.STATUS_VALID },
		String sql = "SELECT DISTINCT C.* FROM PRPDCOMPANY c WHERE c.comAttribute = ? CONNECT BY NOCYCLE c.UPPERCOMCODE = PRIOR c.COMCODE";
		int length = 1;
		if (!Helper.isEmpty(companyIds)) {
			length += companyIds.length;
			sql += " START WITH c.COMCODE IN (?" + StringHelper.copy(", ?", length - 2) + ")";
		}

		Object[] parameters = new Object[length];
		parameters[0] = SystemUtils.COMPANY_INTERNAL;
		if (length > 1) System.arraycopy(companyIds, 0, parameters, 1, length - 1);

		sql += " ORDER BY c.COMCODE";
		return (List<Company>) this.dao.querySQL(sql, parameters, Company.class);
	}

	/**
	 * 查询归属在companies下的所有部门信息
	 * 
	 * @param companies
	 * @return
	 * @see #getInternal(String[])
	 */
	public List<Company> getInternal(Company[] companies) {
		int length = companies.length;
		String[] companyIds = new String[length];
		int index = 0;
		for (Company company : companies) {
			companyIds[index++] = company.getId();
		}
		return this.getInternal(companyIds);
	}
}
