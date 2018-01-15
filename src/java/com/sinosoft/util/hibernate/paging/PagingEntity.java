package com.sinosoft.util.hibernate.paging;

import java.util.List;

import com.sinosoft.util.DateHelper;
import com.sinosoft.util.hibernate.entity.Entity.JSONConverter;
import com.sinosoft.util.json.JSONObject;

public class PagingEntity<E> {
	/** 查询的结果 */
	List<E> list;

	/** 当前页显示的数量,等于list.size */
	int size;

	/** 总数量 */
	int total;

	/** 当前页码 */
	int pageIndex;

	/** 总页数 */
	int pageCount;

	// /** 第一条数据的索引，参见query.setFirstResult */
	// int firstResult;

	/** 每页数量 */
	int maxResults;

	public List<E> list() {
		return list;
	}

	public int size() {
		return size;
	}

	public int total() {
		return total;
	}

	public List<E> getList() {
		return list;
	}

	public void setList(List<E> list) {
		this.list = list;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * 日期的输出格式，在列表中默认仅显示年月日
	 */
	private String datePattern = DateHelper.DEFAULT_DATE_FORMAT;

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String toJSONString() {
		JSONObject json = new JSONObject();
		json.setDatePattern(datePattern);
		json.from(this);
		json.put("success", true); // 标识操作成功为true
		return json.toString();
	}

	public String toJSONString(JSONConverter converter) {
		throw new RuntimeException("尚未实现toJSONString(JSONConverter)方法!");
	}
	// public String toJSON() {
	// StringBuilder json = new StringBuilder();
	// json.append("{");
	// json.append("size:").append(size).append(",");
	// json.append("total:").append(total).append(",");
	// json.append("pageIndex:").append(pageIndex).append(",");
	// json.append("maxResults:").append(maxResults).append(",");
	// json.append("pageCount:").append(pageCount).append(",\n");
	// json.append("list:").append(pageCount).append(",");
	// if (this.list != null) {
	// json.append("[");
	// for (Object o : list) {
	// if (o == null)
	// continue;
	// if (o.getClass().isArray()) {
	// // 数组
	// for (int i = 0, l = Array.getLength(o); i < l; i++) {
	// json.append("'").append(StringHelper.trim(Array.get(o, i)).replace("\n", "\\n"));
	// }
	// }
	// }
	// json.append("]");
	// } else {
	// json.append("null");
	// }
	// json.append("}");
	// return json.toString();
	// }
}
