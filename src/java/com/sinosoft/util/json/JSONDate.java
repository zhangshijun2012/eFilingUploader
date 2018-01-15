package com.sinosoft.util.json;

import java.util.Date;

import com.sinosoft.util.DateHelper;
import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.StringHelper;

/**
 * 日期对象
 * 
 * @author LuoGang
 * 
 */
public class JSONDate implements JSON<Date> {

	public JSONDate(Date value) {
		super();
		this.value = value;
	}

	public JSONDate(Date value, String pattern) {
		this(value);
		this.pattern = pattern;
	}

	private Date value;
	private String pattern;

	public String toJSONString() {
		if (StringHelper.isEmpty(pattern))
			return "new Date(" + NumberHelper.formatInteger(value.getTime(), false) + ")";
		return "\"" + DateHelper.format(value, pattern).replace("\"", "\\\"") + "\"";
	}

	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public JSONDate from(Date value) {
		this.value = value;
		return this;
	}

	public JSONDate convert(Date value) {
		JSONDate json = clone();
		return json.from(value);
	}

	@Override
	public JSONDate clone() {
		try {
			return (JSONDate) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONDate(value, pattern);
	}

}
