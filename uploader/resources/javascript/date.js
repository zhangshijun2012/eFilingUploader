/**
 * 日期处理工具.主要用于日期的格式化和将字符串转为日期
 */
DateHelper = {
	isDate: function(date, format) {
		if (Object.isString(date)) date = this.parse(date, format);
		return date && Object.getClass(date) == "Date";
	},
	/* 默认在转换字符串为日期时尝试匹配的所有格式 */
	defaults: ['YYYY-MM-DD HH:MI:SS.MS', 'YYYY-MM-DD HH:MI:SS', 'YYYY-MM-DD HH:MI', 'YYYY-MM-DD'],
	
	/* 缺省的日期格式 */
	defaultFormat: 'YYYY-MM-DD',
	
	/* 所有使用过的格式 */
	allFormats: ['YYYY-MM-DD HH:MI:SS.MS', 'YYYY-MM-DD HH:MI:SS', 'YYYY-MM-DD HH:MI', 'YYYY-MM-DD'],
	addFormat: function(format) {
		if (format && !format.isInArray(this.allFormats)) {
			this.allFormats.push(format);
		}
	},
	/**
	 * 检查是否可为日期
	 */
	isValid: function(y, m, d, h, mi, s, ms) {
		if (Object.isString(y) && Object.isUndefined(d)) return !!this.parse(y, m);
        // setup defaults
        h = h || 0;
        mi = mi || 0;
        s = s || 0;
        ms = ms || 0;

        var dt = new Date(y, m - 1, d, h, mi, s, ms);

        return y == dt.getFullYear() &&
            m == dt.getMonth() + 1 &&
            d == dt.getDate() &&
            h == dt.getHours() &&
            mi == dt.getMinutes() &&
            s == dt.getSeconds() &&
            ms == dt.getMilliseconds();
    },
	formatFunctions: { },
	createFormat: function(formatText) {
		//var text = String(formatText);
		var format, formats, values, c, f, v, p, b;
		var i = 0, l = formatText.length;
		var codes = [];
		while (i < l) {
			c = formatText.charAt(i);
			if (c == "\\") {	// 定义\\为转义字符
				i++;
				c = formatText.charAt(i);
			} else {
				b = false;
				for (var property in DateHelper.Formats) {
					format = DateHelper.Formats[property];
					formats = format.formats;
					values = format.values;
					for (var j = 0, m = Math.min(formats.length, values.length); j < m; j++) {
						//text = text.replaceAll(formats[i], "' + " + values[i] + " + '", format.ignoreCase);
						p = formats[j].length;
						f = formatText.substr(i, p);
						if (f == formats[j] || (format.ignoreCase && f.toLowerCase() == formats[j].toLowerCase())) {
							codes.push(values[j]);
							i += p;
							b = true;
							break;
						}
					}
					if (b) break;
				}
				if (b) continue;
			}
			codes.push("'" + StringHelper.escape(c) + "'");
			i++;
		}
		
		DateHelper.addFormat(formatText);
		return this.formatFunctions[formatText] = new Function("return " + codes.join('+'));
	},
	
	/**
	 * 格式化日期,
	 * date: 日期/日期字符串
	 * format: 格式化的字符串
	 * formats: 如果date不是Date对象,用次formats格式将date转换为日期对象
	 */
	format: function(date, format, formats) {
		if (!format) format = 'YYYY-MM-DD';
		
		if (Object.getClass(date) != "Date") date = this.parse(date, formats);
		
		if (Object.getClass(date) != "Date") return "";
		var p = this.formatFunctions;
        if (!p[format]) {
			this.createFormat(format);
		}
		
		return p[format].call(date);
	},

	/**
	 * 将字符串转换为日期的正则表达式
	 */
	parseRegexes: { },
	/**
	 * 将字符串转换为日期的函数
	 */
	parseFunctions: { },
	createParser : function() {
		var code = ["var y = 0, m = 1, d = 1, h = 0, mi = 0, s = 0, ms = 0, date = null; ",
					"var results = String(input).match(DateHelper.parseRegexes['{0}']);", // either null, or an array of matched strings

					"if (results) {",
						"{1}",	// 根据正则表达式的匹配结果取年月日等信息
						"if (DateHelper.isValid(y, m, d, h, mi, s, ms)) {", // i.e. unix time is defined
							"date = new Date(y, m - 1, d, h, mi, s, ms);",
						"}",
					"}",
					"return date;"
		].join('\n');

        return function(formatText) {
			var format, formats, values, c, f, v, p, b;
			var i = 0, l = formatText.length, k = 1;
			var codes = [], calc = [];
			while (i < l) {
				c = formatText.charAt(i);
				if (c == "\\") {	// 定义\\为转义字符
					i++;
					c = formatText.charAt(i);
				} else {
					b = false;
					for (var property in DateHelper.Formats) {
						if (!DateHelper.Parsers[property]) continue;
						format = DateHelper.Formats[property];
						formats = format.formats;
						values = DateHelper.Parsers[property].values;
						for (var j = 0, m = Math.min(formats.length, values.length); j < m; j++) {
							//text = text.replaceAll(formats[i], "' + " + values[i] + " + '", format.ignoreCase);
							p = formats[j].length;
							f = formatText.substr(i, p);
							if (f == formats[j] || (format.ignoreCase && f.toLowerCase() == formats[j].toLowerCase())) {
								codes.push(DateHelper.Parsers[property].parsers[j]);
								calc.push(StringHelper.format(values[j], k));
								i += p;
								k++;
								b = true;
								break;
							}
						}
						if (b) break;
					}
					if (b) continue;
				}
				codes.push(StringHelper.escape(c));
				i++;
			}
			
			DateHelper.addFormat(formatText);
            DateHelper.parseRegexes[formatText] = new RegExp("^" + codes.join('') + "$");
            DateHelper.parseFunctions[formatText] = new Function("input", StringHelper.format(code, formatText, calc.join('')));
        }
    }(),
    /**
     * 将字符串input转换为日期对象.
     * input: 日期或日期字符串
     * formats: 将要尝试的所有格式
     */
	parse: function(input, formats) {
		if (Object.getClass(input) == "Date") return input;
		if (!formats) formats = this.allFormats;
		if (Object.isArray(formats)) {
			var date = null;
			for (var i = 0, l = formats.length; i < l; i++) {
				if (date = this.parse(input, formats[i])) return date;
			}
			return;
		}
        try {
	        var p = this.parseFunctions;
	        if (!p[formats]) {
	            this.createParser(formats);
	        }
	        
        	return p[formats](input);
        } catch (e) { }
        
        return null;
	},
    
	 /**
     * Checks if the current date falls within a leap year.
     * @return {Boolean} True if the current date falls within a leap year, false otherwise.
     */
    isLeapYear: function(date) {
    	date = this.parse(date);
    	if (!date) return null;
        var year = date.getFullYear();
        return !!((year & 3) == 0 && (year % 100 || (year % 400 == 0 && year)));
    },
    /**
     * date与another是否是同一天.同年同月同日
     */
    isSameDay: function(date, another) {
    	date = this.parse(date);
    	if (!date) return false;
    	another = this.parse(another);
    	if (!another) return false;
    	return date.getFullYear() == another.getFullYear() 
    			&& date.getMonth() == another.getMonth() && date.getDate() == another.getDate();
    }
};

/**
 * 日期格式
 */
DateHelper.Formats = {
	/**
	 * 定义年的格式
	 * formats: 2位的数组,第1位为全格式,第2位为精简格式
	 * value: 对应的格式转换为的值.this表示要格式化的时间对象
	 * ignoreCase: 是否忽略大小写
	 */
	YEAR: {
		formats: ["yyyy", "yy"],
		values: ["this.getFullYear()", "('' + this.getFullYear()).substring(2, 4)"],
		ignoreCase: true
	},
	DAY: {
		formats: ["dd", "d"],
		values: ["StringHelper.leftPad(this.getDate(), 2, '0')", "this.getDate()"],
		ignoreCase: true
	},
	
	/**
	 * 定义小时的格式
	 * formats: 4位的数组,第1位为24小时全格式,第2位为24小时的精简格式.后面2位为12小时的格式
	 * ignoreCase: 是否忽略大小写
	 */
	HOUR: {
		formats: ["HH", "H", "hh", "h"],
		values: ["StringHelper.leftPad(this.getHours(), 2, '0')", "this.getHours()", 
				"StringHelper.leftPad((this.getHours() > 12) ? this.getHours() - 12 : this.getHours(), 2, '0')",
				"(this.getHours() > 12 ? this.getHours() - 12 : this.getHours())"],
		ignoreCase: false
	},
	
	MINUTE: {
		formats: ["MI", "mi"],
		values: ["StringHelper.leftPad(this.getMinutes(), 2, '0')", "this.getMinutes()"],
		ignoreCase: false
	},
	
	SENCOND: {
		formats: ["ss", "s"],
		values: ["StringHelper.leftPad(this.getSeconds(), 2, '0')", "this.getSeconds()"],
		ignoreCase: true
	},
	
	MILLISENCOND: {
		formats: ["MS", "ms"],
		values: ["StringHelper.leftPad(this.getMilliseconds(), 3, '0')", "this.getMilliseconds()"],
		ignoreCase: false
	},
	
	/**
	 * 月的格式,因为m与MINUTE有冲突,所以放在后面
	 */
	MONTH: {
		formats: ["mm", "m"],
		values: ["StringHelper.leftPad(this.getMonth() + 1, 2, '0')", "this.getMonth() + 1"],
		ignoreCase: true
	},
	/**
	 * AM/PM.小写使用am/pm
	 */
	A: {
		formats: ["A", "a"],
		values: ["(this.getHours() < 12 ? 'AM' : 'PM')", "(this.getHours() < 12 ? 'am' : 'pm')"],
		ignoreCase: false
		
	}
};

/* 与DateFormats相对应.每个DateFormats中的格式对应转换为相应的正则表达式 */
DateHelper.Parsers = {
	/**
	 * 定义年的格式
	 * formats: 2位的数组,第1位为全格式,第2位为精简格式
	 * value: 对应的格式转换为的值.this表示要格式化的时间对象
	 * ignoreCase: 是否忽略大小写
	 */
	YEAR: {
		parsers: ["(\\d{1,4})", "(\\d{1,2})"],
		values: ["y = NumberHelper.intValue(results[{0}]);\n", "var ty = NumberHelper.intValue(results[{0}]);\n"
                + "y = ty > 50 ? 1900 + ty : 2000 + ty;\n"]
	},
	DAY: {
		parsers: ["([0-3]?\\d)", "([0-3]?\\d)"],
		values: ["d = NumberHelper.intValue(results[{0}]);\n", "d = NumberHelper.intValue(results[{0}]);\n"]
	},
	
	/**
	 * 定义小时的格式
	 * formats: 4位的数组,第1位为24小时全格式,第2位为24小时的精简格式.后面2位为12小时的格式
	 * ignoreCase: 是否忽略大小写
	 */
	HOUR: {
		parsers: ["([0-2]?\\d)", "([0-2]?\\d)", "([0-1]?\\d)", "([0-1]?\\d)"],
		values: ["h = NumberHelper.intValue(results[{0}]);\n", "h = NumberHelper.intValue(results[{0}]);\n",
				"h = NumberHelper.intValue(results[{0}]);\n", "h = NumberHelper.intValue(results[{0}]);\n"]
	},
	
	MINUTE: {
		parsers: ["([0-5]?\\d)", "([0-5]?\\d)"],
		values: ["mi = NumberHelper.intValue(results[{0}]);\n", "mi = NumberHelper.intValue(results[{0}]);\n"]
	},
	
	SENCOND: {
		parsers: ["([0-5]?\\d)", "([0-5]?\\d)"],
		values: ["s = NumberHelper.intValue(results[{0}]);\n", "s = NumberHelper.intValue(results[{0}]);\n"]
	},
	
	MILLISENCOND: {
		parsers: ["(\\d{1,3})", "(\\d{1,3})"],
		values: ["ms = NumberHelper.intValue(results[{0}]);\n", "ms = NumberHelper.intValue(results[{0}]);\n"]
	},
	
	/**
	 * 月的格式,因为m与MINUTE有冲突,所以放在后面
	 */
	MONTH: {
		parsers: ["([0-1]?\\d)", "([0-1]?\\d)"],
		values: ["m = NumberHelper.intValue(results[{0}]);\n", "m = NumberHelper.intValue(results[{0}]);\n"]
	},
	/**
	 * AM/PM.小写使用am/pm
	 */
	A: {
		parsers: ["([aAPp][Mm])", "([aAPp][Mm])"],
		values: ["if (results[{0}].toLowerCase() == 'am') {\n"
	                + "if (!h || h == 12) { h = 0; }\n"
	                + "} else { if (!h || h < 12) { h = (h || 0) + 12; }}", 
                "if (results[{0}].toLowerCase() == 'am') {\n"
	                + "if (!h || h == 12) { h = 0; }\n"
	                + "} else { if (!h || h < 12) { h = (h || 0) + 12; }}"]
	}
};
Helper.add("DateHelper", DateHelper);


Object.extend(Date, {
     /**
     * Date interval constant
     * @static
     * @type String
     */
    MILLISECOND : "ms",

    /**
     * Date interval constant
     * @static
     * @type String
     */
    SECOND : "s",

    /**
     * Date interval constant
     * @static
     * @type String
     */
    MINUTE : "mi",

    /** Date interval constant
     * @static
     * @type String
     */
    HOUR : "h",

    /**
     * Date interval constant
     * @static
     * @type String
     */
    DAY : "d",

    /**
     * Date interval constant
     * @static
     * @type String
     */
    MONTH : "m",

    /**
     * Date interval constant
     * @static
     * @type String
     */
    YEAR : "y"
});

Object.extend(Date.prototype, {
	format: function(format) {
		return DateHelper.format(this, format);
	},
	
    /**
     * Creates and returns a new Date instance with the exact same date value as the called instance.
     * Dates are copied and passed by reference, so if a copied date variable is modified later, the original
     * variable will also be changed.  When the intention is to create a new variable that will not
     * modify the original instance, you should create a clone.
     *
     * Example of correctly cloning a date:
     * <pre><code>
//wrong way:
var orig = new Date('10/1/2006');
var copy = orig;
copy.setDate(5);
document.write(orig);  //returns 'Thu Oct 05 2006'!

//correct way:
var orig = new Date('10/1/2006');
var copy = orig.clone();
copy.setDate(5);
document.write(orig);  //returns 'Thu Oct 01 2006'
</code></pre>
     * @return {Date} The new Date instance.
     */
    clone : function() {
        return new Date(this.getTime());
    },
    
	 /**
     * Checks if the current date falls within a leap year.
     * @return {Boolean} True if the current date falls within a leap year, false otherwise.
     */
    isLeapYear : function() {
        var year = this.getFullYear();
        return !!((year & 3) == 0 && (year % 100 || (year % 400 == 0 && year)));
    },

    /**
     * Get the first day of the current month, adjusted for leap year.  The returned value
     * is the numeric day index within the week (0-6) which can be used in conjunction with
     * the {@link #monthNames} array to retrieve the textual day name.
     * Example:
     * <pre><code>
var dt = new Date('1/10/2007');
document.write(Date.dayNames[dt.getFirstDayOfMonth()]); //output: 'Monday'
</code></pre>
     * @return {Number} The day number (0-6).
     */
    getFirstDayOfMonth : function() {
        var day = (this.getDay() - (this.getDate() - 1)) % 7;
        return (day < 0) ? (day + 7) : day;
    },

    /**
     * Get the last day of the current month, adjusted for leap year.  The returned value
     * is the numeric day index within the week (0-6) which can be used in conjunction with
     * the {@link #monthNames} array to retrieve the textual day name.
     * Example:
     * <pre><code>
var dt = new Date('1/10/2007');
document.write(Date.dayNames[dt.getLastDayOfMonth()]); //output: 'Wednesday'
</code></pre>
     * @return {Number} The day number (0-6).
     */
    getLastDayOfMonth : function() {
        return this.getLastDateOfMonth().getDay();
    },


    /**
     * Get the date of the first day of the month in which this date resides.
     * @return {Date}
     */
    getFirstDateOfMonth : function() {
        return new Date(this.getFullYear(), this.getMonth(), 1);
    },

    /**
     * Get the date of the last day of the month in which this date resides.
     * @return {Date}
     */
    getLastDateOfMonth : function() {
        return new Date(this.getFullYear(), this.getMonth(), this.getDaysInMonth());
    },

    /**
     * Get the number of days in the current month, adjusted for leap year.
     * @return {Number} The number of days in the month.
     */
    getDaysInMonth: function() {
        var daysInMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

        return function() { // return a closure for efficiency
            var m = this.getMonth();

            return m == 1 && this.isLeapYear() ? 29 : daysInMonth[m];
        }
    }(),
    
    /**
     * Provides a convenient method for performing basic date arithmetic. This method
     * does not modify the Date instance being called - it creates and returns
     * a new Date instance containing the resulting date value.
     *
     * Examples:
     * <pre><code>
// Basic usage:
var dt = new Date('10/29/2006').add(Date.DAY, 5);
document.write(dt); //returns 'Fri Nov 03 2006 00:00:00'

// Negative values will be subtracted:
var dt2 = new Date('10/1/2006').add(Date.DAY, -5);
document.write(dt2); //returns 'Tue Sep 26 2006 00:00:00'

// You can even chain several calls together in one line:
var dt3 = new Date('10/1/2006').add(Date.DAY, 5).add(Date.HOUR, 8).add(Date.MINUTE, -30);
document.write(dt3); //returns 'Fri Oct 06 2006 07:30:00'
</code></pre>
     *
     * @param {String} interval A valid date interval enum value.
     * @param {Number} value The amount to add to the current date.
     * @return {Date} The new Date instance.
     */
    add : function(interval, value) {
        var d = this.clone();
        if (!interval || value === 0) return d;
        switch(interval.toLowerCase()) {
            case Date.MILLISECOND:
                d.setMilliseconds(this.getMilliseconds() + value);
                break;
            case Date.SECOND:
                d.setSeconds(this.getSeconds() + value);
                break;
            case Date.MINUTE:
                d.setMinutes(this.getMinutes() + value);
                break;
            case Date.HOUR:
                d.setHours(this.getHours() + value);
                break;
            case Date.DAY:
                d.setDate(this.getDate() + value);
                break;
            case Date.MONTH:
                var day = this.getDate();
                if (day > 28) {
                    day = Math.min(day, this.getFirstDateOfMonth().add(Date.MONTH, value).getLastDateOfMonth().getDate());
                }
                d.setDate(day);
                d.setMonth(this.getMonth() + value);
                break;
            case Date.YEAR:
                d.setFullYear(this.getFullYear() + value);
                break;
        }
        return d;
    },
    /**
     * 与date比较大小
     * 1: !date || this > date
     * 0: this == date
     * -1: this < date
     */
    compareTo: function(date, formats) {
    	var date = DateHelper.parse(date, formats);
    	return (!date || this.getTime() > date.getTime()) ? 1 : (this.getTime() == date.getTime() ? 0 : -1)
    	
    }
});