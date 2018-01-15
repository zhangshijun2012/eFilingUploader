/** 一些基础函数 */
var SERVER_ROOT = window['SERVER_ROOT'] || '/uploader/';
Object.extend = function(destination, source) {
	for ( var property in source)
		destination[property] = source[property];
	return destination;
};
Object.extend(Object, {
	/** object的对象类型 */
	getClass : function(object) {
		return Object.prototype.toString.call(object).match(/^\[object\s(.*)\]$/)[1];
	},
	/** object是否为Object类型 */
	isObject : function(object) {
		return !!object && Object.getClass(object) == "Object";
	},
	/** object是否未定义 */
	isUndefined : function(object) {
		return typeof object === "undefined";
	},
	isFunction : function(object) {
		return Object.getClass(object) === "Function";
	},
	isBoolean : function(object) {
		return Object.getClass(object) === "Boolean";
	},
	isString : function(object) {
		return Object.getClass(object) === "String";
	},
	isNumeric : function(object) {
		return Object.isNumber(object);
	},
	isNumber : function(object) {
		return Object.getClass(object) === "Number";
	},
	isDate : function(object) {
		return Object.getClass(object) === "Date";
	},
	isArray : function(object) {
		return Object.getClass(object) === "Array";
	},
	/** object是否为HTML的DOM节点 */
	isElement : function(object) {
		return !!(object && object.nodeType == 1);
	}
});

Object.extend(Array.prototype, {
	/**
	 * 迭代调用数组
	 * 
	 * @param iterator 迭代函数,有3个参数,value,index,this数组对象
	 * @param context iterator的调用对象
	 */
	each : function(iterator, context) {
		for ( var i = 0, length = this.length >>> 0; i < length; i++) {
			if (i in this) iterator.call(context, this[i], i, this);
		}
	},
	indexOf : function(value) {
		for ( var i = 0, l = this.length; i < l; i++) {
			if (this[i] === value) return i;
		}
		return -1;
	},
	include : function(value) {
		return this.indexOf(value) >= 0;
	}
});
(function() {
	var TRUE = [ 'true', 'yes', '1', 't', 'y' ];
	Object.extend(Boolean, {
		/**
		 * value转换为boolean对象
		 * 
		 * @param value
		 * @returns {Boolean}
		 */
		parseBoolean : function(value) {
			if (value === true || value === false || value === 1 || value === 0 || value === null
					|| Object.isUndefined(value)) return !!value;
			return !!value && TRUE.include(String.trim(value).toLowerCase());
		}
	});
})();
/** 除去字符串前后的空格 */
String.prototype.trim = function() {
	return this.replace(/^\s+/, '').replace(/\s+$/, '');
};
String.trim = function(value) {
	return value == null ? '' : String(value).trim();
};
if (window['Ext']) {
	String.toJSON = function(value) {
		return Ext.util.JSON.decode(value);
	};
}
(function($) {
	window['NumberFormat'] = function(pattern) {
		if (pattern && Object.isString(pattern)) this.applyPattern(pattern);
		else if (Object.isNumber(pattern)) {
			// 第一个参数为数字,表示保留的小数的最小位数
			this.minimumFractionDigits = pattern;
			this.maximumFractionDigits = arguments[1] ? Number.intValue(arguments[1], this.minimumFractionDigits)
					: this.minimumFractionDigits;
			if (!Object.isUndefined(arguments[2])) this.groupingSize = (!!arguments[2] ? 3 : 0);
		} else if (Object.isBoolean(pattern)) {
			// 第一个参数为boolean类型
			this.groupingSize = (pattern ? 3 : 0);
			if (!Object.isUndefined(arguments[1])) {
				this.minimumFractionDigits = Number.intValue(arguments[1]);
				this.maximumFractionDigits = arguments[2] ? Number.intValue(arguments[2], this.minimumFractionDigits)
						: this.minimumFractionDigits;
			}
		}
		return this;
	};
	$.extend(NumberFormat, {
		PATTERN_ZERO_DIGIT : '0',
		PATTERN_GROUPING_SEPARATOR : ',',
		PATTERN_DECIMAL_SEPARATOR : '.',
		PATTERN_PER_MILLE : '\u2030',
		PATTERN_PERCENT : '%',
		PATTERN_DIGIT : '#',
		PATTERN_SEPARATOR : ';',
		PATTERN_EXPONENT : "E",
		PATTERN_MINUS : '-'
	});
	$.extend(NumberFormat.prototype, {
		minimumIntegerDigits : 1, // 数字格式化的最小整数位数
		maximumIntegerDigits : 40,
		minimumFractionDigits : 0,
		maximumFractionDigits : 3,
		groupingSize : 3, // 大于0,分组大小,如123,456.78 -> groupingSize = 3
		groupingSeparator : ',', // 分组符号
		prefix : '', // 前缀符号
		suffix : '', // 后缀符号
		multiplier : 1, // 比例,百分比为100,千分比为1000
		minusSign : '-', // 负数的符号
		plusSign : '', // 整数的符号,默认省略
		decimalSeparator : '.',
		decimalSeparatorAlwaysShown : false, // 是否总是显示小数点
		round : true, // 是否进行四舍五入,如果为false则直接进行截断,不会进行四舍五入的处理

		applyPattern : function(pattern) {
			var digitLeftCount = 0, zeroDigitCount = 0, digitRightCount = 0;
			var groupingCount = -1;
			var decimalPos = -1;
			var prefixIndex = 0;
			var prefix = ''; // 前缀符号
			var suffix = ''; // 后缀符号
			var multiplier = 1; // 比例,1/100(有百分号%)/1000(有千分号)
			var minusSign = '-';
			var plusSign = '';
			var ch;
			for ( var i = 0, l = pattern.length; i < l; i++) {
				ch = pattern.charAt(i);
				switch (ch) {
				case NumberFormat.PATTERN_DIGIT: // if (ch == '#') {
					if (zeroDigitCount > 0) {
						++digitRightCount;
					} else {
						++digitLeftCount;
					}
					if (groupingCount >= 0 && decimalPos < 0) {
						++groupingCount;
					}
					break;
				case NumberFormat.PATTERN_ZERO_DIGIT: // } else if (ch == '0') {
					if (digitRightCount > 0) {// 不能出现0#0的形式
						throw ("Unexpected '0' in pattern \"" + pattern + '"');
					}
					++zeroDigitCount;
					if (groupingCount >= 0 && decimalPos < 0) {
						++groupingCount;
					}
					break;
				case NumberFormat.PATTERN_GROUPING_SEPARATOR: // } else if (ch == ",") { //
					// 出现分组符号,则重新计算分组数量
					groupingCount = 0;
					break;
				case NumberFormat.PATTERN_DECIMAL_SEPARATOR: // } else if (ch == '.') { // 小数点
					if (decimalPos >= 0) { throw ("Multiple decimal separators in pattern \"" + pattern + '"'); }
					decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
					break;
				case '+': // } else if (ch == '+' || ch == '-') {
					plusSign = '+';
					if (prefixIndex == i) prefixIndex++;
					break;
				case '-':
					minusSign = '-';
					if (prefixIndex == i) prefixIndex++;
					break;
				default: // } else {
					if (ch == NumberFormat.PATTERN_PERCENT) { // 百分比
						if (multiplier != 1) { throw ("Too many percent/per mille characters in pattern \""
								+ pattern + '"'); }
						multiplier = 100;
					} else if (ch == NumberFormat.PATTERN_PER_MILLE) { // 千分比
						if (multiplier != 1) { throw ("Too many percent/per mille characters in pattern \""
								+ pattern + '"'); }
						multiplier = 1000;
					}
					if (prefixIndex == i) {
						prefix += ch;
						prefixIndex++;
					} else {
						suffix += ch;
					}
				}
			}

			// Handle patterns with no '0' pattern character. These patterns
			// are legal, but must be interpreted. "##.###" -> "#0.###".
			// ".###" -> ".0##".
			/*
			 * We allow patterns of the form "####" to produce a zeroDigitCount of zero (got that?);
			 * although this seems like it might make it possible for format() to produce empty strings,
			 * format() checks for this condition and outputs a zero digit in this situation. Having a
			 * zeroDigitCount of zero yields a minimum integer digits of zero, which allows proper
			 * round-trip patterns. That is, we don't want "#" to become "#0" when toPattern() is called
			 * (even though that's what it really is, semantically).
			 */
			if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
				// Handle "###.###" and "###." and ".###"
				var n = decimalPos;
				if (n == 0) { // Handle ".###"
					++n;
				}
				digitRightCount = digitLeftCount - n;
				digitLeftCount = n - 1;
				zeroDigitCount = 1;
			}

			// alert(decimalPos + ',d:' + digitLeftCount + ',' + zeroDigitCount + ',' + digitRightCount
			// + ',digitRightCount=' + digitRightCount);
			if ((decimalPos < 0 && digitRightCount > 0)
					|| (decimalPos >= 0 && (decimalPos < digitLeftCount || decimalPos > (digitLeftCount + zeroDigitCount)))
					|| groupingCount == 0) { throw ("Malformed pattern \"" + pattern + '"'); }
			this.minusSign = minusSign;
			this.plusSign = plusSign;
			this.prefix = prefix;
			this.suffix = suffix;
			this.multiplier = multiplier;

			var digitTotalCount = digitLeftCount + zeroDigitCount + digitRightCount;
			/*
			 * The effectiveDecimalPos is the position the decimal is at or would be at if there is no
			 * decimal. Note that if decimalPos<0, then digitTotalCount == digitLeftCount +
			 * zeroDigitCount.
			 */
			var effectiveDecimalPos = decimalPos >= 0 ? decimalPos : digitTotalCount;
			this.minimumIntegerDigits = effectiveDecimalPos - digitLeftCount;
			// this.maximumIntegerDigits = 100; // 整数的最大位数,使用默认的40位
			this.maximumFractionDigits = decimalPos >= 0 ? (digitTotalCount - decimalPos) : 0;
			this.minimumFractionDigits = (decimalPos >= 0 ? (digitLeftCount + zeroDigitCount - decimalPos)
					: 0);
			// alert('minimumFractionDigits=' + this.minimumFractionDigits);
			this.groupingSize = (groupingCount > 0) ? groupingCount : 0; // 为0则不分组
			// 小数点是否需要显示,如果是小数,则总为true,否则看是否在表达式中指定了.
			this.decimalSeparatorAlwaysShown = (decimalPos == 0 || decimalPos == digitTotalCount);
		},

		/**
		 * 格式化数字
		 * 
		 * @param number
		 */
		format : function(number) {
			number *= this.multiplier;
			var isNegative = number < 0; // 是否小于0

			var value = String(number); // 转换为字符串类型
			var decimalPos = value.indexOf(".");
			if (this.round && decimalPos >= 0
					&& this.maximumFractionDigits < (value.length - decimalPos - 1)) {
				// 进行四舍五入,则加上0.(...)5,小数点后的0的个数为this.maximumFractionDigits
				number = number + Math.pow(10, 0 - this.maximumFractionDigits - 1) * 5;
			}

			number = Math.abs(number);
			value = String(number); // 转换为字符串类型
			// alert('value=' + value);
			decimalPos = value.indexOf(".");
			var integer = decimalPos >= 0 ? value.substring(0, decimalPos) : value;
			// alert('integer=' + integer);
			// alert('this.minimumIntegerDigits=' + this.minimumIntegerDigits +
			// ',this.maximumIntegerDigits=' + this.maximumIntegerDigits);
			if (integer.length < this.minimumIntegerDigits) {
				integer = String.leftPad(integer, '0', this.minimumIntegerDigits);
			} else if (integer.length > this.maximumIntegerDigits) {
				integer = integer.substring(0, this.maximumIntegerDigits);
			}
			if (this.groupingSize > 0 && this.groupingSeparator && integer.length > this.groupingSize) {
				var integerCopy = '';
				for ( var i = 0, l = integer.length - 1; i <= l; i++) {
					if (i > 0 && i % this.groupingSize == 0) integerCopy = this.groupingSeparator
							+ integerCopy;
					integerCopy = integer.charAt(l - i) + integerCopy;
				}
				integer = integerCopy;
			}
			// alert('integer2=' + integer);
			integer = this.prefix + integer;
			// alert('integer2=' + integer);
			if (isNegative) integer = this.minusSign + integer;
			else integer = this.plusSign + integer;
			var decimal = decimalPos >= 0 ? value.substring(decimalPos + 1) : '';
			if (decimal.length < this.minimumFractionDigits) {
				// alert('d1' + decimal + ' ' + decimal.length + ' ' + this.minimumFractionDigits);
				decimal = String.rightPad(decimal, this.minimumFractionDigits, '0');
				// alert('d2' + decimal + ' ' + decimal.length + ' ' + this.minimumFractionDigits);
			} else if (decimal.length > this.maximumFractionDigits) {
				decimal = decimal.substring(0, this.maximumFractionDigits);
			}

			if (decimal) {
				// 有小数
				integer = integer + this.decimalSeparator + decimal;
			} else if (this.decimalSeparatorAlwaysShown) {
				// 总是显示小数点
				integer = integer + this.decimalSeparator;
			}

			return integer + this.suffix;
		}
	});

	/** 进制,每个单位之间的基数 */
	var RADIX = 1024;
	/** 字节 */
	var B = 1;
	/** 1KB=1024B */
	var KB = B * RADIX;
	/** 1MB=1024KB=1024*1024B */
	var MB = KB * RADIX;
	/** GB */
	var GB = MB * RADIX;
	/** TB */
	var TB = GB * RADIX;
	/** 显示的文件单位 */
	var UNITS = [ "B", "KB", "MB", "GB", "TB" ];

	$.extend(Number, {
		NUMBER : /^[\+\-]?(\d+)(\.\d+)?$/, // 数字的正则表达式
		isNumber : function(value) {
			if (Object.isNumber(value)) return !isNaN(value) && isFinite(value);
			return this.NUMBER.test(String.trim(value));
		},
		/**
		 * 
		 * 字符串转换为整数
		 * 
		 * @param value 要转换的值
		 * @param defaultValue 如果不为数字返回的默认值
		 * @param radix 进制,默认为10
		 * @returns 转换后的整数
		 */
		parseInt : function(value, defaultValue, radix) {
			var intNumber = parseInt(value, radix || 10);
			return isNaN(intNumber) || !isFinite(intNumber) ? defaultValue || 0 : intNumber;
		},
		/**
		 * 字符串value转换为整数
		 * 
		 * @param value 要转换的值
		 * @param defaultValue 如果不为数字返回的默认值
		 * @param radix 进制,默认为10
		 * @returns 转换后的整数
		 */
		intValue : function(value, defaultValue, radix) {
			return this.parseInt(value, defaultValue, radix);
		},
		/**
		 * 字符串转换为小数
		 * 
		 * @param value 要转换的值
		 * @param defaultValue 如果不为数字返回的默认值
		 */
		parseFloat : function(value, defaultValue) {
			var floatNumber = parseFloat(value);
			return isNaN(floatNumber) || !isFinite(floatNumber) ? defaultValue || 0 : floatNumber;
		},
		/**
		 * 将字符串转换为小数
		 * 
		 * @param value 要转换的值
		 * @param defaultValue 如果不为数字返回的默认值
		 * @returns
		 */
		parseDouble : function(value, defaultValue) {
			return this.parseFloat(value, defaultValue);
		},
		floatValue : function(value, defaultValue) {
			return this.parseFloat(value, defaultValue);
		},
		doubleValue : function(value, defaultValue) {
			return this.parseFloat(value, defaultValue);
		},
		/**
		 * 格式化数字
		 * 
		 * @param number 要格式化的数字
		 * @param pattern {String}.数字的格式,可参考java.text.DecimalFormat {Number}.小数的位数,此时,args依次为小数最大位数,是否显示千分符
		 *            {Boolean}.是否显示千分符,此时,args依次为小数最小位数,小数最大位数
		 * @param args. 如果pattern不为String,则根据pattern的类型不同而不同
		 * 
		 */
		format : function(number, pattern, args1, args2) {
			var numberFormat = new NumberFormat(pattern, args1, args2);
			if (Object.isUndefined(pattern)) {
				// 默认保留2位小数
				numberFormat.minimumFractionDigits = 2;
				numberFormat.maximumFractionDigits = 2;
			}
			return numberFormat.format(Number.doubleValue(number));
		},

		/**
		 * 格式化整数
		 * 
		 * @param number
		 * @param pattern
		 * @param args1
		 * @param args2
		 * @returns
		 */
		formatInteger : function(number, pattern, args1, args2) {
			var numberFormat = new NumberFormat(pattern, args1, args2);
			// 不显示小数
			numberFormat.decimalSeparatorAlwaysShown = false;
			numberFormat.minimumFractionDigits = 0;
			numberFormat.maximumFractionDigits = 0;
			return numberFormat.format(Number.doubleValue(number));
		},
		/**
		 * 格式化文件大小
		 * 
		 * @param size
		 * @param pattern 数字格式,缺省则为'0.#'
		 * @returns {String}
		 */
		formatFileSize : function(size, pattern) {
			var u = 0;
			var unit = UNITS[u++];
			var min = size;
			while (min > RADIX && u < UNITS.length) {
				unit = UNITS[u++];
				min = min / RADIX;
			}
			min = this.format(min, pattern || "0.#");
			return min + " " + unit;
		}
	});
})(Object);

/** 根据id查询元素 */
var $ = function (element) {
	if (Object.isString(element)) element = document.getElementById(element);
	return (element);
};

if (!window['AjaxHelper']) {
	var AjaxHelper = {
		request: function(url, options) {
			options = Object.extend({
				method : 'POST',
				url: url
			}, options || { });
			if (options['onSuccess']) options['success'] = options['onSuccess'];
			if (options['onComplete']) {
				var onComplete = options['onComplete'];
				options['callback'] = function(options, success, xhq) {
					onComplete(xhq, options, success);
				};
			}
			if (options['onFailure']) options['failure'] = options['onFailure'];
			if (options['parameters']) options['params'] = options['parameters'];
			 
			Ext.Ajax.request(options);
		}
	};
}