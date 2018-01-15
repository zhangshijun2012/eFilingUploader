package com.sinosoft.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 
 * @author LuoGang 2010-2-3
 */
public class StringHelper {
	/**
	 * 返回str除去前后空格后的字符串
	 * 
	 * @param str
	 * @return 返回str除去前后空格后的字符串,若str==null,返回空字符串
	 */
	public static final String trim(Object str) {
		if (str == null) return "";
		try {
			if (str.getClass().isArray()) return StringHelper.join((Object[]) str).trim();
			if (str instanceof Iterable) StringHelper.join((Iterable<?>) str);
			if (str instanceof Map) StringHelper.join((Map<?, ?>) str);
		} catch (Exception ignore) {
			// TODO
		}
		return str.toString().trim();
	}

	/**
	 * 
	 * 返回str除去前后空格后的字符串，若str为空，则返回defaultValue
	 * 
	 * @param str
	 * @param defaultValue
	 * @return
	 */
	public static final String trim(Object str, String defaultValue) {
		return isEmpty(str) ? defaultValue : str.toString().trim();
	}

	/**
	 * 将数组的值全部转为小写
	 * 
	 * @param values
	 * @return
	 */
	public static final String[] toLowerCase(String[] values) {
		if (Helper.isEmpty(values)) return values;
		String[] lowerValues = new String[values.length];
		int i = 0;
		for (String value : values) {
			lowerValues[i++] = trim(value).toLowerCase();
		}
		return lowerValues;
	}

	/**
	 * 将数组的值全部转为大写
	 * 
	 * @param values
	 * @return
	 */
	public static final String[] toUpperCase(String[] values) {
		if (Helper.isEmpty(values)) return values;
		String[] lowerValues = new String[values.length];
		int i = 0;
		for (String value : values) {
			lowerValues[i++] = trim(value).toUpperCase();
		}
		return lowerValues;
	}

	/**
	 * 除去o的换行字符串
	 * 
	 * @param o
	 * @return
	 */
	public static String nowrap(Object str) {
		return str == null ? "" : str.toString().replace("\n", "").replace("\r", "");
	}

	/**
	 * 字符串的长度,一个中文字符占2字节
	 * 
	 * @return
	 */
	public static int length(Object str) {
		if (str == null) return 0;
		String s = str instanceof String ? (String) str : str.toString();
		try {
			return s.getBytes(SystemHelper.ENCODING).length;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return s.getBytes().length;
		}
	}

	/**
	 * 字符串是否为空
	 * 
	 * @return
	 */
	public static boolean isEmpty(Object str) {
		if (str == null) return true;
		if (str instanceof String) return ((String) str).trim().length() <= 0;
		return ObjectHelper.isEmpty(str);
	}

	/**
	 * 复制字符串times次
	 * 
	 * @param str
	 * @param times
	 * @return
	 */
	public static String copy(Object str, int times) {
		if (times <= 0) {
			return "";
		}

		String s = str == null ? null : str.toString();
		if (s == null || s.length() <= 0) {
			return "";
		}

		StringBuffer v = new StringBuffer(s);
		for (int i = 1; i < times; i++) {
			v.append(s);
		}
		return v.toString();
	}

	/**
	 * 用separator链接数组array
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static String join(Object[] array, String separator) {
		if (ObjectHelper.isEmpty(array)) return "";
		separator = separator == null ? "," : separator;
		StringBuffer v = new StringBuffer();
		Object o = array[0];
		v.append(o == null ? "" : o);
		for (int i = 1, l = array.length; i < l; i++) {
			o = array[i];
			v.append(separator).append(o == null ? "" : o);
		}
		return v.toString();
	}

	public static String join(Object[] array) {
		return join(array, ",");
	}

	public static String join(Iterable<?> iterable) {
		return join(iterable, ",");
	}

	public static String join(Iterable<?> iterable, String separator) {
		if (ObjectHelper.isEmpty(iterable)) return "";
		separator = separator == null ? "," : separator;
		StringBuffer v = new StringBuffer();
		for (Object o : iterable) {
			v.append(separator).append(o == null ? "" : o);
		}
		v.delete(0, separator.length());
		return v.toString();
	}

	/**
	 * 用separator链接map.key1=value1&key2=value2
	 * 
	 * @param map
	 * @param separator
	 * @param equal 等号
	 * @return
	 */
	public static String join(Map<?, ?> map, String separator, String equal) {
		if (map == null || map.isEmpty()) return "";
		Set<?> keys = map.keySet();
		StringBuffer v = new StringBuffer();
		equal = equal == null ? "=" : equal;
		separator = separator == null ? "&" : separator;
		Object o = null;
		for (Object key : keys) {
			v.append(separator);
			o = map.get(key);
			if (o.getClass().isArray()) {
				o = StringHelper.join((Object[]) o, separator + key + equal);
			} else if (Iterable.class.isAssignableFrom(o.getClass())) {
				o = StringHelper.join((Iterable<?>) o, separator + key + equal);
			} else if (Map.class.isAssignableFrom(o.getClass())) {
				o = StringHelper.join((Map<?, ?>) o, separator, equal);
			}
			if (key == null) {
				v.append(o);
			} else {
				v.append(key).append(equal).append(o);
			}
		}
		v.delete(0, separator.length());
		return v.toString();
	}

	public static String join(Map<?, ?> map, String separator) {
		return join(map, separator, "=");
	}

	public static String join(Map<?, ?> map) {
		return join(map, "&", "=");
	}

	/**
	 * 检测o是否为null，若是则返回returnValue
	 * 
	 * @param o
	 * @param returnValue
	 * @return String returnValue(o == nul), 或者o.toString();
	 */
	public static String noNull(Object o, String returnValue) {
		return o == null ? returnValue : o.toString();
	}

	/**
	 * 检测o是否为null，若是则返回""
	 * 
	 * @param o
	 * @return String ""(o == nul), 或者o.toString();
	 * @return
	 */
	public static String noNull(Object o) {
		return o == null ? "" : o.toString();
	}

	/**
	 * 检测o是否为null或""，若是则返回emptyReturnValue
	 * 
	 * @param o
	 * @param emptyReturnValue
	 * @return String emptyReturnValue(o == null或空), 或者o.toString();
	 */
	public static String noEmpty(Object o, String emptyReturnValue) {
		return isEmpty(o) ? emptyReturnValue : o.toString();
	}

	/**
	 * str在数组array中出现的位置.从前往后查找
	 * 
	 * @param str
	 * @param array
	 * @param ignoreCase 是否忽略大小写
	 * @param ignoreEmpty 是否忽略空白
	 * @return
	 */
	public static int indexInArray(Object str, String[] array, boolean ignoreCase, boolean ignoreEmpty) {
		// String value = str == null ? null : str.toString();
		String v = (str == null) ? null : (ignoreEmpty ? str.toString().trim() : str.toString());
		if (array != null) {
			String strTmp = null;
			for (int i = 0, l = array.length; i < l; i++) {
				if ((array[i] == null && v == null)) {
					return i;
				}
				if (array[i] == null || v == null) {
					continue;
				}
				/* 是否忽略空白 */
				strTmp = ignoreEmpty ? array[i].trim() : array[i];

				/* 是否忽略大小写 */
				if (ignoreCase ? strTmp.equalsIgnoreCase(v) : strTmp.equals(v)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * str在数组array中出现的位置,忽略空白
	 * 
	 * @param str
	 * @param array
	 * @param ignoreCase 是否父类大小写
	 * @see #indexInArray(Object, String[], boolean, boolean)
	 * @return
	 */
	public static int indexInArray(Object str, String[] array, boolean ignoreCase) {
		return indexInArray(str, array, ignoreCase, true);
	}

	/**
	 * str在数组array中出现的位置,忽略空白,区分大小写
	 * 
	 * @param str
	 * @param array
	 * @see #indexInArray(Object, String[], boolean, boolean)
	 * @return
	 */
	public static int indexInArray(Object str, String[] array) {
		return indexInArray(str, array, false, true);
	}

	/**
	 * str在数组array中出现的位置.从后往前查找
	 * 
	 * @param str
	 * @param array
	 * @param ignoreCase 是否忽略大小写
	 * @param ignoreEmpty 是否忽略空白
	 * @return
	 */
	public static int lastIndexIn(Object str, String[] array, boolean ignoreCase, boolean ignoreEmpty) {
		String v = (str == null) ? null : (ignoreEmpty ? str.toString().trim() : str.toString());
		if (array != null) {
			String strTmp = null;
			for (int i = array.length - 1; i >= 0; i--) {
				if ((array[i] == null && v == null)) {
					return i;
				}
				if (array[i] == null || v == null) {
					continue;
				}
				/* 忽略空白 */
				strTmp = ignoreEmpty ? array[i].trim() : array[i];
				/* 忽略大小写 */
				if (ignoreCase ? strTmp.equalsIgnoreCase(v) : strTmp.equals(v)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * str在数组array中出现的位置.从后往前查找.忽略空白
	 * 
	 * @param str
	 * @param array
	 * @param ignoreCase 是否忽略大小写
	 * @see #lastIndexIn(Object, String[], boolean, boolean)
	 * @return
	 */
	public static int lastIndexIn(Object str, String[] array, boolean ignoreCase) {
		return lastIndexIn(str, array, ignoreCase, true);
	}

	/**
	 * str在数组array中出现的位置.从后往前查找.忽略空白,区分大小写
	 * 
	 * @param str
	 * @param array
	 * @see #lastIndexIn(Object, String[], boolean, boolean)
	 * @see #lastIndexIn(Object, String[], boolean)
	 * @return
	 */
	public static int lastIndexIn(Object str, String[] array) {
		return lastIndexIn(str, array, false);
	}

	/**
	 * 截取字符串，注意此处字符串的长度为其字节长度，即一个中文字符占2位
	 * 
	 * @param str 源字符串
	 * @param beginIndex 开始位置(从0开始)
	 * @param endIndex 结束位置,最大为字符串的长度
	 * @see #length(Object)
	 * @return
	 */
	public static String substring(Object str, int beginIndex, int endIndex) {
		if (str == null) return "";
		String value = str instanceof String ? (String) str : str.toString();
		int length = length(str);
		if (beginIndex < 0) beginIndex = 0;

		if (endIndex > length) endIndex = length;

		if (beginIndex >= endIndex) return "";

		if (beginIndex == 0 && endIndex >= length) return value;

		StringBuilder buffer = new StringBuilder();
		// char c;
		String c;
		int index = 0;
		int l;
		// length = 0;
		for (int i = 0, len = value.length(); i < len; i++) {
			c = value.substring(i, i + 1);
			l = length(c);
			if (index >= beginIndex && (index += l) <= endIndex) {
				buffer.append(c);
			}
			// index += l;
			if (index >= endIndex) break;
		}
		return buffer.toString();

		// byte[] bytes = new byte[endIndex - beginIndex];
		// System.arraycopy(value.getBytes(), beginIndex, bytes, 0, bytes.length);
		// return new String(bytes);

		// return "";
	}

	/**
	 * 从beginIndex开始截取，注意此处字符串的长度为其字节长度，即一个中文字符占2位
	 * 
	 * @param str 源字符串
	 * @param beginIndex
	 * @see #length(Object)
	 * @see #substring(Object, int, int)
	 * @return
	 */
	public static String substring(Object str, int beginIndex) {
		return substring(str, beginIndex, length(str));
	}

	/**
	 * 保留左边length位，注意此处字符串的长度为其字节长度，即一个中文字符占2位
	 * 
	 * @param str 源字符串
	 * @param length 截取的长度
	 * @see #substring(Object, int, int)
	 * @return
	 */
	public static String substringLeft(Object str, int length) {
		return substring(str, 0, length);
	}

	/**
	 * 保留右边length位，注意此处字符串的长度为其字节长度，即一个中文字符占2位
	 * 
	 * @param str 源字符串
	 * @param length 截取的长度
	 * @see #length(Object)
	 * @see #substring(Object, int, int)
	 * @return
	 */
	public static String substringRight(Object str, int length) {
		int l = length(str);
		return substring(str, l - length, l);
	}

	/**
	 * true
	 */
	public static String[] TRUE = { "true", "t", "yes", "y", "1", "是" };

	/**
	 * false
	 */
	public static String[] FALSE = { "false", "f", "no", "n", "0", "否" };

	public static boolean parseBoolean(Object str) {
		return indexInArray(str, TRUE, true, true) > -1;
	}

	/**
	 * 字符串str是否在TRUE数组中
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isTrue(Object str) {
		return indexInArray(str, TRUE, true, true) > -1;
	}

	/**
	 * 字符串str是否则FALSE数组中
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isFalse(Object str) {
		return indexInArray(str, FALSE, true, true) > -1;
	}

	/**
	 * 输入的字符串input是否与所给的正则表达式regex相匹配
	 * 
	 * @param input 要匹配的字符串
	 * @param regex 正则表达式
	 * @param flags 匹配模式. Match flags, a bit mask that may include {@link Pattern#CASE_INSENSITIVE},
	 *            {@link Pattern#MULTILINE}, {@link Pattern#DOTALL}, {@link Pattern#UNICODE_CASE},
	 *            {@link Pattern#CANON_EQ}, {@link Pattern#UNIX_LINES}, {@link Pattern#LITERAL} and
	 *            {@link Pattern#COMMENTS}
	 * @return
	 */
	public static boolean match(Object input, String regex, int flags) {
		Pattern p = Pattern.compile(regex, flags);
		Matcher m = p.matcher(noNull(input));
		return m.matches();
	}

	/**
	 * 输入的字符串input是否与所给的正则表达式数组regexs中的某个相匹配
	 * 
	 * @param input
	 * @param regexs
	 * @param flags
	 * @return
	 * @see #match(Object, String, int)
	 */
	public static boolean match(Object input, Object[] regexs, int flags) {
		int l = 0;
		if (regexs == null || (l = regexs.length) <= 0) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (match(input, trim(regexs[i]), flags)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 输入的字符串input是否与所给的正则表达式regex相匹配
	 * 
	 * @param input 要匹配的字符串
	 * @param regex 正则表达式
	 * @return
	 */
	public static boolean match(Object input, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(noNull(input));
		return m.matches();
	}

	/**
	 * 输入的字符串input是否与所给的正则表达式数组regexs中的某个相匹配
	 * 
	 * @param input
	 * @param regexs
	 * @return
	 * @see #match(Object, String)
	 */
	public static boolean match(Object input, Object[] regexs) {
		int l = 0;
		if (regexs == null || (l = regexs.length) <= 0) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (match(input, trim(regexs[i]))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 改变o的编码.从encoding->iso8859-1->newEncoding
	 * 
	 * @param o
	 * @param encoding o当前的编码,默认为SystemHelper.ENCODING
	 * @param newEncoding 改编后的编码，默认为SystemHelper.ENCODING_ISO8859_1
	 * @return
	 */
	public static String changeEncoding(Object o, String encoding, String newEncoding) {
		String string = (o == null) ? null : o.toString();

		string = changeToISO88591(o, encoding);
		try {
			byte[] bytes = string.getBytes(SystemHelper.ENCODING_ISO_8859_1);

			string = newEncoding == null ? new String(bytes) : new String(bytes, newEncoding);
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * 改变o的编码为iso8859-1
	 * 
	 * @param o
	 * @param encoding 源字符串的编码
	 * @return
	 */
	public static String changeToISO88591(Object o, String encoding) {
		if (o == null) return null;
		String string = o.toString();

		try {
			byte[] bytes = encoding == null ? string.getBytes() : string.getBytes(encoding);

			string = new String(bytes, SystemHelper.ENCODING_ISO_8859_1);
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * 改变o的编码为iso8859-1
	 * 
	 * @param o
	 * @return
	 */
	public static String changeToISO88591(Object o) {
		return changeToISO88591(o, null);
	}

	/**
	 * 生成32位的UUID,全大写<br>
	 * UUID.randomUUID().toString().replace("-", "").toUpperCase();
	 * 
	 * @see UUID#randomUUID()
	 * @return
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
		// return null;
	}

	/**
	 * 生成36位的UUID,统一使用大写字母
	 * 
	 * @see UUID#randomUUID()
	 * 
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().toUpperCase();
		// return null;
	}

	/**
	 * 编译SQL字符串,将单引号替换为两个单引号
	 * 
	 * @param sql
	 * @return
	 */
	public static final String escapeSQLComponent(String sql) {
		return sql.replace("'", "''");
	}

	/**
	 * 编译SQL字符串,将单引号替换为两个单引号
	 * 
	 * @param sql
	 * @return
	 */
	public static final String escape(Object value) {
		if (value == null) return "";
		return value.toString().replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'").replace("\n", "\\n")
				.replace("\r", "\\r");
	}

	public static void main(String[] args) {
		// for (int i = 0; i < 10; i++)
		// System.out.println(randomUUID());
		// String s = "哈哈";
		// ByteArrayOutputStream wr = new ByteArrayOutputStream();
		// System.out.println(s.getBytes().length);
		// try {
		// OutputStreamWriter w = new OutputStreamWriter(wr, "GBK");
		// w.write("大夫".toCharArray());
		// w.flush();
		// w.close();
		// String s2 = new String(wr.toByteArray(), "GBK");
		// // String s2 = new String(s.getBytes(), "UTF-8");
		// System.out.println(s2 + s2.getBytes("GBK").length);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
