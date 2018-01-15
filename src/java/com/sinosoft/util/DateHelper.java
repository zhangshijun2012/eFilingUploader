package com.sinosoft.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 主要是对日期/时间的处理,包括对传入的字符串进行特定格式的转换
 * 
 * @author LuoGang
 */
public class DateHelper {
	/**
	 * 系统默认的日期格式 yyyy-MM-dd
	 */
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * 默认的时间格式 HH:mm:ss
	 */
	public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	/**
	 * 默认的日期时间格式 yyyy-MM-dd HH:mm:ss
	 */
	public static final String DEFAULT_DATETIME_FORMAT = DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;

	/**
	 * 默认的带毫秒的日期时间格式 yyyy-MM-dd HH:mm:ss.SSS
	 */
	public static final String DEFAULT_MILLISECOND_FORMAT = DEFAULT_DATETIME_FORMAT + ".SSS";

	/**
	 * 所有可能进行转换的日期格式
	 */
	private static final List<String> ALL_FORMATS = new ArrayList<String>();

	static {
		ALL_FORMATS.add("yyyy年MM月dd日HH时mm分ss秒SSS毫秒");
		ALL_FORMATS.add("yyyy年MM月dd日HH时mm分ss秒");
		ALL_FORMATS.add("yyyy-MM-dd HH:mm:ss.SSS");
		ALL_FORMATS.add("yyyy-MM-dd HH:mm:ss");
		ALL_FORMATS.add("yyyy-MM-dd HH:mm");
		ALL_FORMATS.add("yyyy-MM-dd");
		ALL_FORMATS.add("yyyy-MM");
		ALL_FORMATS.add("yyyyMMdd");
		ALL_FORMATS.add("yyyyMM");
	}

	/**
	 * 
	 * 将日期字符串source按指定的格式pattern转换为Date类型
	 * 
	 * @param source 要转换的日期字符串
	 * @param pattern 日期格式字符串
	 * @return 转换后的日期
	 * @return
	 * @throws ParseException 无法按指定格式转换
	 */
	public static Date parse(String source, String pattern) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Date date = new SimpleDateFormat(pattern).parse(source, pos);

		if (pos.getIndex() == 0 || pos.getIndex() < source.length()) // 必须是全字匹配
		throw new ParseException("Unparseable date: \"" + source + "\"", pos.getErrorIndex());
		return date;
	}

	/**
	 * 转换source为日期类型,尝试所有默认的日期格式
	 * 
	 * @param source 日期字符串
	 * @return 转换后的日期
	 * @see #parse(String, String)
	 */
	public static Date parse(String source) {
		if (StringHelper.isEmpty(source)) return null;
		Date date;
		for (String pattern : ALL_FORMATS) {
			try {
				date = parse(source, pattern);
				return date;
			} catch (ParseException e) {
				// continue;
				// 无法按指定格式转换，则继续使用下一种格式进行转换
			}
		}
		return null;
	}

	/**
	 * 转换source为日期类型,尝试所有默认的日期格式
	 * 
	 * @param source 日期字符串
	 * @param defaultDate 如果source无法转换,则返回此日期
	 * @return 转换后的日期
	 * @see #parse(String)
	 */
	public static Date parse(String source, Date defaultDate) {
		if (StringHelper.isEmpty(source)) return defaultDate;
		Date date;
		for (String pattern : ALL_FORMATS) {
			try {
				date = parse(source, pattern);
				return date;
			} catch (ParseException e) {
				// continue;
				// 无法按指定格式转换，则继续使用下一种格式进行转换
			}
		}
		return defaultDate;
	}

	/**
	 * 格式化date为pattern格式的字符串
	 * 
	 * @param date Date 日期
	 * @param pattern String 日期时间格式
	 * @return 转换后的日期时间字符串
	 * @see SimpleDateFormat#SimpleDateFormat(String)
	 * @see SimpleDateFormat#format(Date)
	 */
	public static String format(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	/**
	 * 将source表示的日期转换为pattern格式的字符串
	 * 
	 * @param source 表示日期的字符串
	 * @param pattern 转换后的格式
	 * @return 转换后的日期时间字符串
	 */
	public static String format(String source, String pattern) {
		Date date = parse(source);
		if (date == null) return "";
		return format(date, pattern);
	}

	/**
	 * 
	 * 将date日期转换为默认日期格式yyyy-MM-dd的字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String format(Date date) {
		return format(date, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 
	 * 将source表示的日期转换为默认日期格式yyyy-MM-dd的字符串
	 * 
	 * @param source 字符串表示的日期
	 * @return
	 */
	public static String format(String source) {
		return format(source, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 将date转换为默认的yyyy-MM-dd HH:mm:ss格式
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDateTime(Date date) {
		return format(date, DEFAULT_DATETIME_FORMAT);
	}

	/**
	 * 将source转换为默认的yyyy-MM-dd HH:mm:ss格式
	 * 
	 * @param source
	 * @return
	 */
	public static String formatDateTime(String source) {
		return format(source, DEFAULT_DATETIME_FORMAT);
	}

	public static String now() {
		return formatDateTime(new Date());
	}

	public static String nowDate() {
		return format(new Date());
	}

	public static String nowMs() {
		return format(new Date(), DEFAULT_MILLISECOND_FORMAT);
	}

	/**
	 * 返回当前年份
	 * 
	 * @return
	 */
	public static int getYear() {
		return get(Calendar.YEAR);
	}

	/**
	 * 当前月份,从1开始
	 * 
	 * @return
	 */
	public static int getMonth() {
		return get(Calendar.MONTH) + 1;
	}

	/**
	 * 当前日
	 * 
	 * @return
	 */
	public static int getDay() {
		return get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 时
	 * 
	 * @return
	 */
	public static int getHour() {
		return get(Calendar.HOUR_OF_DAY);
	}

	public static int get(int field) {
		return get(null, field);
	}

	/**
	 * 得到日期date中的key数据段
	 * 
	 * @param date
	 * @param key 参考Calendar.get()的参数
	 * @return
	 */
	public static int get(Date date, int field) {
		Calendar c = Calendar.getInstance();
		c.setTime(date == null ? new Date() : date);
		return c.get(field);
	}

	/**
	 * 在日期对象上增加天数等.
	 * 
	 * @param date 要改变的日期
	 * @param field 表示增加年/月/日/时/分/秒等
	 * @param amount 可以为负数
	 * @return
	 */
	public static Date add(Date date, int field, int amount) {
		date = date == null ? new Date() : date;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(field, amount);
		return date = c.getTime();
	}

	public static Date addDays(Date date, int days) {
		return add(date, Calendar.DAY_OF_MONTH, days);
	}

	/**
	 * 得到在当前日期基础上增加后的日期
	 * 
	 * @param field
	 * @param amount
	 * @return
	 */
	public static Date add(int field, int amount) {
		return add(null, field, amount);
	}

	public static Date set(Date date, int field, int amount) {
		date = ((date == null) ? new Date() : date);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(field, amount);
		return date = c.getTime();
	}

	public static Date set(int field, int amount) {
		return set(null, field, amount);
	}

	public static Date clear(Date date, int field) {
		date = ((date == null) ? new Date() : date);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.clear(field);
		return date = c.getTime();
	}

	/**
	 * @param date
	 * @param field
	 * @return
	 */
	public static Date clear(Date date, String field) {
		date = ((date == null) ? new Date() : date);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		field = StringHelper.trim(field);
		if (field.equals("")) {
			c.clear();
		} else if (field.toLowerCase().charAt(0) == 'd') {
			c.clear(Calendar.ERA);
			c.clear(Calendar.YEAR);
			c.clear(Calendar.MONTH);
			c.clear(Calendar.WEEK_OF_YEAR);
			c.clear(Calendar.WEEK_OF_MONTH);
			c.clear(Calendar.DATE);
			// c.clear(Calendar.DAY_OF_MONTH);
			c.clear(Calendar.DAY_OF_YEAR);
			c.clear(Calendar.DAY_OF_WEEK);
			c.clear(Calendar.DAY_OF_WEEK_IN_MONTH);
		} else if (field.toLowerCase().charAt(0) == 't') {
			c.clear(Calendar.AM_PM);
			c.clear(Calendar.HOUR);
			c.clear(Calendar.HOUR_OF_DAY);
			c.clear(Calendar.MINUTE);
			c.clear(Calendar.SECOND);
			c.clear(Calendar.MILLISECOND);
			c.clear(Calendar.DST_OFFSET);
			c.clear(Calendar.ZONE_OFFSET);
		}
		return date = c.getTime();
	}

	/**
	 * 清除date的时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date clear(Date date) {
		return clear(date, "t");
	}

	/**
	 * 将date的时间变为23:59:59
	 * 
	 * @param date
	 * @return
	 */
	public static Date clearToEnd(Date date) {
		return parse(format(clear(date, "t")) + " 23:59:59");
	}

	public static Date clear(String field) {
		return clear(null, field);
	}

	/**
	 * 返回今天的零点整的日期对象
	 * 
	 * @return
	 */
	public static Date clear() {
		return clear((Date) null);
	}

	/**
	 * 当天23:59:59
	 * 
	 * @return
	 */
	public static Date clearToEnd() {
		return clearToEnd(null);
	}

	/**
	 * 一天的毫秒数
	 */
	public static final int DAY = 24 * 60 * 60 * 1000;

	/**
	 * 判断cls是否是一个日期类型
	 * 
	 * @param cls
	 * @return
	 */
	public static boolean isDate(Class<?> cls) {
		return cls != null && Date.class.isAssignableFrom(cls);
	}

	/**
	 * 判断date是否为日期对象
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isDate(Object date) {
		try {
			return date == null ? false : date instanceof Date;
		} catch (Exception e) {
			return isDate(StringHelper.trim(date));
		}
	}

	/**
	 * 判断指定的日期date与日期格式dateformat是否匹配
	 * 
	 * @param date
	 * @param pattern
	 * @return ture 如果date能解析为pattern的日期格式
	 */
	public static boolean isDate(String date, String pattern) {
		try {
			return null != parse(date, pattern);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return false;
		}
	}

	/**
	 * date是否能解析为日期
	 * 
	 * @param date
	 * @return true 如果date为已知的日期格式
	 * @see #isDate(String, String)
	 */
	public static boolean isDate(String date) {
		return isDate(date, null);
	}

	/**
	 * year, month, day是否日期
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static boolean isDate(int year, int month, int day) {
		if ((month < 0 || month > 12) || (day < 0 || day > 31)) {
			return false;
		}

		return day < getDays(year, month);
	}

	/**
	 * 是否闰年
	 * 
	 * @param year
	 * @return
	 */
	public static boolean isLeap(int year) {
		return (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0));
	}

	/**
	 * 返回一个月的天数
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static int getDays(int year, int month) {
		return (month == 2) ? (isLeap(year) ? 29 : 28)
				: ((((month < 7) && (month % 2 == 0)) || ((month > 8) && (month % 2 == 1))) ? 30 : 31);
	}

	/**
	 * 取较小的日期
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static Date min(Date date1, Date date2) {
		Date d1 = date1 == null ? new Date() : date1;
		Date d2 = date2 == null ? new Date() : date2;
		return d1.compareTo(d2) < 0 ? date1 : date2;
	}

	/**
	 * 取较大的日期
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static Date max(Date date1, Date date2) {
		Date d1 = date1 == null ? new Date() : date1;
		Date d2 = date2 == null ? new Date() : date2;
		return d1.compareTo(d2) < 0 ? date2 : date1;
	}

	/**
	 * date1与date2相差的天数date1-date2,不足一天按一天计算
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDays(Date date1, Date date2) {
		// Date d1 = date1 == null ? new Date() : date1;
		// Date d2 = date2 == null ? new Date() : date2;
		double day = date1.getTime() - date2.getTime();
		return Math.round(day / DAY);
	}

	/**
	 * @param date 需要在年份上增加的日期
	 * @param year 年份增加多少
	 * @return 在年份上增加了的一个新的日期
	 */
	public static Date addYear(Date date, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + year);
		return calendar.getTime();
	}

	/**
	 * @param date 需要在月份上增加的日期
	 * @param month 月份增加多少
	 * @return 在月份上增加了的一个新的日期
	 */
	public static Date addMonth(Date date, int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + month);
		return calendar.getTime();
	}

	/**
	 * @param date 日期
	 * @return
	 */
	public static int getYearByDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	//
	// /**
	// * 将日期转为中文大写 2009-10-08: 二零零玖年壹拾月零捌日
	// *
	// * @param date
	// * @return
	// */
	// public static String getChineseDate(Object date) {
	// String v = null;
	// try {
	// v = getDate(StringHelper.trim(date), "yyyy-MM-dd");
	// String[] d = v.split("-");
	// String year = d[0];
	// String month = d[1];
	// String day = d[2];
	// StringBuffer chineseDate = new StringBuffer();
	// int i = 0;
	// int l = year.length();
	// for (i = 0, l = year.length(); i < l; i++) {
	// chineseDate.append(NumberHelper.toChineseNumber(year.charAt(i)));
	// }
	// chineseDate.append("年");
	//
	// v = NumberHelper.toChineseNumber(month.charAt(0));
	// chineseDate.append(v).append(month.charAt(0) == '0' ? "" : NumberHelper.CHINESE_UNIT[0]);
	// chineseDate.append(month.charAt(1) == '0' ? "" : NumberHelper.toChineseNumber(month.charAt(1)));
	// chineseDate.append("月");
	//
	// v = NumberHelper.toChineseNumber(day.charAt(0));
	// chineseDate.append(v).append(day.charAt(0) == '0' ? "" : NumberHelper.CHINESE_UNIT[0]);
	// chineseDate.append(day.charAt(1) == '0' ? "" : NumberHelper.toChineseNumber(day.charAt(1)));
	// chineseDate.append("日");
	// return chineseDate.toString();
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }
	// return "";
	//
	// }

	public static void main(String a[]) {
		// setTimeFormat("hh:mm:ss");
		// setTimeFormat("hh:ss:mm");
		// System.out.println(TIME_NO_SECOND_FORMAT);
		// setTimeFormat("ss:mm:hh");
		// System.out.println(isDate("2008-07-09"));
		// System.out.println(nowMs());
		// long date = getDays(new Date(2013, 8, 3), new Date(2013, 8, 5));
		// System.out.println(date);

		Date date = new Date();

		System.out.println(getYearByDate(date));

	}

}