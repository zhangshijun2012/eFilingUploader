package com.sinosoft.util.hibernate.dialect;

/**
 * 用于防止oracle的分页BUG.在oracle的分页中,如果查询语句中的order by不能进行唯一排序,会造成排序不正确的情况
 * 
 * @author LuoGang
 */
public class Oracle9iDialect extends org.hibernate.dialect.Oracle9iDialect {

	public String getLimitString(String sql, boolean hasOffset) {
		sql = sql.trim();
		boolean isForUpdate = false;
		if (sql.toLowerCase().endsWith(" for update")) {
			sql = sql.substring(0, sql.length() - 11);
			isForUpdate = true;
		}

		StringBuffer pagingSelect = new StringBuffer(sql.length() + 100);
		pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		pagingSelect.append(sql);
		pagingSelect.append(" ) row_ ) where rownum_ <= ?");
		if (hasOffset) {
			pagingSelect.append(" and rownum_ > ?");
		}
		// pagingSelect.append(" order by rownum_");
		if (isForUpdate) {
			pagingSelect.append(" for update");
		}

		return pagingSelect.toString();
	}
}
