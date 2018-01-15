package com.sinosoft.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;
import org.hibernate.annotations.common.util.StringHelper;

import com.opensymphony.xwork2.XWorkException;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

/**
 * 日期转换器,注意在struts2中,要转换的value是一个String数组
 * 
 * @author LuoGang
 * @see StrutsTypeConverter
 * 
 */
public class DateConverter extends com.opensymphony.xwork2.conversion.impl.DateConverter {
	/** 配置文件中指定的日期格式 */
	public static final String[] PATTERN_KEYS = { "global.format.date", "global.format.datetime",
			"global.format.millisecond" };

	/**
	 * 在struts2中,value总是为String[]
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object convertValue(Map<String, Object> context, Object target, Member member, String propertyName,
			Object value, Class toType) {
		if (Helper.isEmpty(value)) return null;
		if (value instanceof Object[]) {
			Object[] objArray = (Object[]) value;
			if (objArray.length == 1) {
				return convertValue(context, null, null, null, objArray[0], toType);
			}

			// else {
			// return super.convertValue(context, target, member, propertyName, value, toType);
			// }
		}

		if (!(value instanceof String)) return super.convertValue(context, target, member, propertyName, value, toType);
		/** toType不为日期 */
		if (!Date.class.isAssignableFrom(toType)) return super.convertValue(context, target, member, propertyName,
				value, toType);

		Locale locale = super.getLocale(context);
		String source = ((String) value).trim();
		Date date = null;
		for (String key : PATTERN_KEYS) {
			String pattern = LocalizedTextUtil.findDefaultText(key, locale);
			if (StringHelper.isEmpty(pattern)) continue;
			try {
				date = DateHelper.parse(source, pattern);
				break;
			} catch (ParseException ignore) {
				// 由异常,继续处理下一格式
			}
		}

		if (date == null) date = DateHelper.parse(source); // 尝试转换所有日期
		if (date == null) return null;
		if (Date.class == toType) return date;

		try {
			// 返回的是Date的子类
			Constructor constructor = toType.getConstructor(new Class[] { long.class });
			return constructor.newInstance(date.getTime());
		} catch (Exception e) {
			throw new XWorkException("Couldn't create class " + toType + " using default (long) constructor", e);
		}
	}
}
