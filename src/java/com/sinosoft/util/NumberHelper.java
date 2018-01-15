package com.sinosoft.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 数字处理工具类
 * 
 * @author LuoGang
 * 
 */
public class NumberHelper {

	/**
	 * 数字正则表达式
	 */
	public static final String NUMBER_REGEX = "[\\+\\-]?(\\d+|(\\d{1,3}(,\\d{3})*))(\\.[0-9]+)?";

	public static boolean isNumber(Class<?> cls) {
		if (cls == Long.TYPE || cls == Integer.TYPE || cls == Float.TYPE || cls == Double.TYPE || cls == Byte.TYPE
				|| cls == Short.TYPE) return true;
		return Number.class.isAssignableFrom(cls);
	}

	/**
	 * 判断o是否可转换为Number类型数据
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isNumber(Object number) {
		if (number == null) return false;
		if (Number.class.isAssignableFrom(number.getClass())) {
			return true;
		}
		return StringHelper.match(number, NUMBER_REGEX);
	}

	/**
	 * 
	 * 转换为大数类型, 默认为0
	 * 
	 * @param number
	 * @return
	 */
	public static BigDecimal toBigDecimal(Object number) {
		return toBigDecimal(number, 0);
	}

	/**
	 * 转换为大数类型
	 * 
	 * @param number
	 * @param defaultValue 默认值
	 * @return
	 */
	public static BigDecimal toBigDecimal(Object number, double defaultValue) {
		if (!isNumber(number)) {
			return new BigDecimal(defaultValue);
		}

		BigDecimal bigDecimal = null;
		try {
			bigDecimal = new BigDecimal(number.toString().replace(",", ""));
		} catch (Exception e) {
			return new BigDecimal(defaultValue);
		}
		return bigDecimal;
	}

	/**
	 * 将其他类型返回double,转换失败则返回0
	 * 
	 * @param number
	 * @return
	 */
	public static double doubleValue(Object number) {
		return doubleValue(number, 0);
	}

	/**
	 * 将其他类型返回double,转换失败则返回defaultValue
	 * 
	 * @param number
	 * @param defaultValue 如果number不是数字则返回该数字
	 * @return
	 */
	public static double doubleValue(Object number, double defaultValue) {
		if (ObjectHelper.isEmpty(number)) {
			return defaultValue;
		}

		if (!isNumber(number)) {
			return defaultValue;
		}

		if (Number.class.isAssignableFrom(number.getClass())) {
			return ((Number) number).doubleValue();
		}
		BigDecimal bigDecimal = null;
		try {
			bigDecimal = new BigDecimal(number.toString().replace(",", ""));
		} catch (Exception e) {
			return defaultValue;
		}
		return bigDecimal.doubleValue();
	}

	/**
	 * 将其他类型返回float
	 * 
	 * @param number
	 * @return
	 */
	public static float floatValue(Object number) {
		return (float) doubleValue(number);
	}

	/**
	 * 将其他类型返回float
	 * 
	 * @param number
	 * @param defaultValue number不为数字时的返回值
	 * @return
	 */
	public static float floatValue(Object number, float defaultValue) {
		return (float) doubleValue(number, defaultValue);
	}

	/**
	 * 将其他类型返回long
	 * 
	 * @param number
	 * @return
	 */
	public static long longValue(Object number) {
		return (long) doubleValue(number);
	}

	/**
	 * 将其他类型返回long,如果不是数字则返回defaultValue
	 * 
	 * @param number
	 * @param defaultValue number不为数字时的返回值
	 * @return
	 */
	public static long longValue(Object number, long defaultValue) {
		return (long) doubleValue(number, defaultValue);
	}

	/**
	 * 将其他类型返回int
	 * 
	 * @param number
	 * @return
	 */
	public static int intValue(Object number) {
		return (int) doubleValue(number);
	}

	/**
	 * 将其他类型返回int
	 * 
	 * @param number
	 * @param defaultValue number不为数字时的返回值
	 * @return
	 */
	public static int intValue(Object number, int defaultValue) {
		return (int) doubleValue(number, defaultValue);
	}

	/**
	 * 提供两个大数的加法,如果其中一个为null，则返回另一个，都为null则返回0. arg0 + arg1
	 * 
	 * @param number
	 * @param another
	 * @return
	 */
	public static BigDecimal add(Object number, Object another) {
		BigDecimal decimal0 = toBigDecimal(number);
		BigDecimal decimal1 = toBigDecimal(another);
		return decimal0.add(decimal1);
	}

	public static BigDecimal add(double number, double another) {
		return add(Double.valueOf(number), Double.valueOf(another));
	}

	/**
	 * 提供两个大数的减法,如果不为数字则以0计算.arg0 - arg1
	 * 
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static BigDecimal subtract(Object number, Object another) {
		BigDecimal decimal0 = toBigDecimal(number);
		BigDecimal decimal1 = toBigDecimal(another);
		return decimal0.subtract(decimal1);
	}

	public static BigDecimal subtract(double number, double another) {
		return subtract(Double.valueOf(number), Double.valueOf(another));
	}

	/**
	 * 提供两个大数的乘法,如果其中一个为null，则返回值=0. arg0 * arg1
	 * 
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static BigDecimal multiply(Object number, Object another) {
		BigDecimal decimal0 = toBigDecimal(number);
		BigDecimal decimal1 = toBigDecimal(another);
		return decimal0.multiply(decimal1);
	}

	public static BigDecimal multiply(double number, double another) {
		return multiply(Double.valueOf(number), Double.valueOf(another));
	}

	/**
	 * 提供两个大数的除法 arg0 / arg1
	 * 
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static BigDecimal divide(Object number, Object another) {
		BigDecimal decimal0 = toBigDecimal(number);
		BigDecimal decimal1 = toBigDecimal(another);
		return decimal0.divide(decimal1);
	}

	public static BigDecimal divide(double number, double another) {
		return divide(Double.valueOf(number), Double.valueOf(another));
	}

	/**
	 * 使用格式化文本对小数进行格式化
	 * 
	 * @param number
	 * @param pattern
	 * @return
	 */
	public static String format(Object number, String pattern) {
		if (number == null) return "";
		NumberFormat format = new DecimalFormat(pattern);
		// format.setRoundingMode(RoundingMode.HALF_UP);
		if (number instanceof Number) {
			return format.format(((Number) number).doubleValue());
		} else {
			BigDecimal bigDecimal = toBigDecimal(number);
			return format.format(bigDecimal);
		}
	}

	// /**
	// * 格式化小数的对象
	// */
	// private static final DecimalFormat numberFormat = new DecimalFormat();
	// static {
	// try {
	// // JDK1.6
	// numberFormat.setRoundingMode(RoundingMode.HALF_UP);
	// } catch (Throwable e) {
	// // TODO
	// e.printStackTrace();
	// }
	// }

	/**
	 * 格式化小数的对象
	 */
	private static final NumberFormat numberFormat = NumberFormat.getInstance();// new

	/**
	 * 格式华数字
	 * 
	 * @param number
	 * @param minDecimal 最少保留的小数位数
	 * @param maxDecimal 最大保留的小数位数
	 * @param separator 分隔符,为空则不使用,为null则使用,
	 * @return
	 */
	public static String format(Object number, int minDecimal, int maxDecimal, String separator) {
		if (!isNumber(number)) {
			return "";
		}
		BigDecimal bigDecimal = toBigDecimal(number);
		String v = null;
		synchronized (numberFormat) {
			numberFormat.setMaximumFractionDigits(maxDecimal);
			numberFormat.setMinimumFractionDigits(minDecimal);

			// java的自动舍入机制有问题：0.015舍入后为0.01，
			// 而0.0151为0.02,故先将number多保留2位小数,然后再转换为需要的格式
			bigDecimal = bigDecimal.add(new BigDecimal((bigDecimal.doubleValue() < 0 ? "-" : "") + "0."
					+ StringHelper.copy("0", numberFormat.getMaximumFractionDigits() + 1) + "1"));

			v = numberFormat.format(bigDecimal);
		}
		if (separator == null) return v;

		if (numberFormat instanceof DecimalFormat) {
			String c = String.valueOf(((DecimalFormat) numberFormat).getDecimalFormatSymbols().getGroupingSeparator());
			return v.replace(c, separator);
		}
		return v;
	}

	/**
	 * 格式化小数
	 * 
	 * @param number
	 * @param decimal 小数位数
	 * @param separator 千分符
	 * @see #format(Object, int, int, String)
	 * @return
	 */
	public static String format(Object number, int decimal, String separator) {
		return format(number, decimal, decimal, separator);

	}

	/**
	 * 格式化小数,使用默认千分符
	 * 
	 * @param number
	 * @param decimal 小数位数
	 * @see #format(Object, int, String)
	 * @see #format(Object, int, int, String)
	 * @return
	 */
	public static String format(Object number, int decimal) {
		return format(number, decimal, null);
	}

	/**
	 * 格式化小数
	 * 
	 * @param number
	 * @param decimal 小数位数
	 * @param separator 是否保留分隔符
	 * @return
	 */
	public static String format(Object number, int decimal, boolean separator) {
		return format(number, decimal, separator ? "," : "");
	}

	/**
	 * 格式化小数,保留2位小数
	 * 
	 * @param number
	 * @see #format(Object, int)
	 * @see #format(Object, int, String)
	 * @see #format(Object, int, int, String)
	 * @return
	 */
	public static String format(Object number) {
		return format(number, 2);
	}

	/**
	 * 格式化小数,保留2位小数
	 * 
	 * @param number
	 * @param separator 是否保留分隔符
	 * @return
	 */
	public static String format(Object number, boolean separator) {
		return format(number, 2, separator);
	}

	/**
	 * 格式化为整数形式
	 * 
	 * @param number
	 * @return
	 */
	public static String formatInteger(Object number) {
		return format(number, 0);
	}

	/**
	 * 格式化为整数形式
	 * 
	 * @param number
	 * @return
	 */
	public static String formatInteger(Object number, boolean separator) {
		return format(number, 0, separator);
	}

	public static final String[] CHINESE_NUMBER = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };

	public static final String[] CHINESE_UNIT = { "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿" };

	public static final String CHINESE_POINT = "点";

	/**
	 * 获得第n位的单位,个位为"",位数从0开始
	 * 
	 * @param n 位数,个位为0,十位为1,以此递增
	 * @return
	 */
	static final String getChineseUnit(int n) {
		return n == 0 ? "" : CHINESE_UNIT[(n - 1) % 8];
	}

	/**
	 * 得到1位中文数字
	 * 
	 * @param n 大于10则转换为n%10
	 * @return
	 */
	static final String getOneChineseNumber(int n) {
		return CHINESE_NUMBER[n % 10];
	}

	/**
	 * 将数字转换为中文的大写
	 * 
	 * @param number
	 * @return
	 */
	public static String formatChinese(Object number) {
		if (!isNumber(number)) {
			return "";
		}
		BigDecimal bigDecimal = toBigDecimal(number);
		String numberString = bigDecimal.toString();
		String sign = "";
		if (numberString.startsWith("-")) {
			sign = "负";
			numberString = numberString.substring(1);
		} else if (numberString.startsWith("+")) {
			sign = "正";
			numberString = numberString.substring(1);
		}

		int p = 0;
		if ((p = numberString.indexOf(".")) < 0 && numberString.length() == 1) {
			/* 0 - 9的整数 */
			return sign + getOneChineseNumber(Integer.parseInt(numberString));
		}

		String integer = p < 0 ? numberString : numberString.substring(0, p);

		StringBuffer chineseNumber = new StringBuffer(sign);
		boolean lastIsZero = false; // 上一位是否为0
		boolean lastIsUnit = false; // 上一位是否为单位,如果上一位为亿,如果当前为万,则不显示万
		int n = 0;
		for (int i = 0, l = integer.length() - 1; i <= l; i++) {
			n = Integer.parseInt(integer.charAt(i) + "");
			if (n != 0) {
				if (lastIsZero) { // 上一位为0
					chineseNumber.append(getOneChineseNumber(0));
					lastIsZero = false;
				}
				chineseNumber.append(getOneChineseNumber(n));
			} else if (!lastIsZero) {
				lastIsZero = true;
			}
			if (l > i && (n != 0 || ((l - i) % 4) == 0)) { // 如果当前位不为0或者当前为4的整数倍的位置,则必须加上单位
				if (lastIsUnit && (l - i) % 4 == 0) {
					lastIsUnit = false;
				} else {
					if ((l - i) % 8 == 0) {
						lastIsUnit = true;
					} else if (lastIsUnit) {
						lastIsUnit = false;
					}
					chineseNumber.append(getChineseUnit(l - i));
				}
			}
		}

		if (p >= 0) {
			chineseNumber.append(CHINESE_POINT);

			String decimal = numberString.substring(p + 1);
			for (int i = 0, l = decimal.length() - 1; i <= l; i++) {
				chineseNumber.append(getOneChineseNumber(Integer.parseInt((decimal.charAt(i) + ""))));
			}
		}
		return chineseNumber.toString();
	}

	/**
	 * @param number 需要产生小于等于15的随机整数
	 * @return
	 */
	public static long randomNumber(int number) {
		return Math.round(Math.random() * number);
	}

	public static void main(String[] args) {
		System.out.println(isNumber(Long.TYPE));
		System.out.println(isNumber(Long.class));

		System.out.println(randomNumber(100));
	}
}
