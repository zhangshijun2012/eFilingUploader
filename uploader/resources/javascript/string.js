// JavaScript Document
StringHelper = {
	/* string是否为空 */
	isEmpty: function(string) {
		if (string === null || Object.isUndefined(string)) {
			return true;
		}
		
		return this.trim(string).length <= 0;
	},
	
	leftPad : function (val, size, ch) {
		var result = new String(val);
		if(!ch) ch = " ";
		while (result.length < size) {
         	result = ch + result;
        }
        return result.toString();
	}, 

	/* 去掉前后的空格 */
	trim: function(string) {
		return this.trimLeft(this.trimRight(string));
	},
	
	/* 去掉左边的空格 */
	trimLeft: function(string) {
		if (string === null || Object.isUndefined(string)) {
			return "";
		}
		return String(string).replace(/(^\s*)/g, "");
	},
	
	/* 去掉右边的空格 */
	trimRight: function(string) {
		if (string === null || Object.isUndefined(string)) {
			return "";
		}
		return String(string).replace(/(\s*$)/g, "");
	},
	
	/**
	 * 用replacement替换string中的所有target
	 * isIgnoreCase: 是否忽略大小写
	 */
	replaceAll: function (string, target, replacement, isIgnoreCase) {
		if (string === null || Object.isUndefined(string)) {
			return "";
		}
		var str0 = String(string);
		var str1 = String(target);
		var str2 = String(replacement);
		var rexp;
		if (isIgnoreCase === true || isIgnoreCase === 1 || 
			this.isTrue(isIgnoreCase) || isIgnoreCase == "i" || isIgnoreCase == "I") {
			/* 不区分大小写 */
			rexp = new RegExp(str1, "gi");
		} else {
			rexp = new RegExp(str1, "g");
		}
		return str0.replace(rexp, str2);
	},
	
	/* 替换换行符 */
	nowrap: function(string) {
		return this.replaceAll(this.replaceAll(string, "\n", "", true), "\r", "", true);
	},
	
	/**
	 * 查询字符串string在数组array中的位置
	 * isIgnoreCase: 是否忽略大小写
	 * isIgnoreEmpty: 是否忽略空白
	 */
	indexInArray: function(string, array, isIgnoreCase, isIgnoreEmpty) {
		if (Object.isArray(array)) {
			var arg0 = null;
			var arg1 = array;
			if (string !== null) {
				arg0 = String(string);
				/* 忽略空白 */
				arg0 = isIgnoreEmpty ? arg0.trim() : arg0;

				/* 忽略大小写 */
				arg0 = isIgnoreCase ? arg0.toLowerCase() : arg0;
			}
			var strTmp = null;
			for (var i = 0, l = arg1.length; i < l; i++) {
				if ((arg1[i] === null && arg0 === null)) {
					return i;
				}
				if (arg1[i] === null || arg0 === null) {
					continue;
				}
				/* 忽略空白 */
				strTmp = isIgnoreEmpty ? String(arg1[i]).trim() : String(arg1[i]);
				/* 忽略大小写 */
				strTmp = isIgnoreCase ? strTmp.toLowerCase() : strTmp;
				if (strTmp == arg0) {
					return i;
				}
			}
		}
		return -1;
	},
	
	/**
	 * 查询字符串string在数组array中的是否出现,请参见this.indexInArray
	 * 
	 */
	isInArray: function(string, array, isIgnoreCase, isIgnoreEmpty) {
		return this.indexInArray(string, array, isIgnoreCase, isIgnoreEmpty) > -1;
	},
	
	TRUE: ["true", "t", "yes", "y", "1"],
	FALSE: ["false", "f", "no", "n", "0"],
	isTrue: function(string) {
		return string && this.indexInArray(string, this.TRUE, true, true) > -1;
	},
	isFalse: function(string) {
		return this.indexInArray(string, this.FALSE, true, true) > -1;
	},
	parseBoolean: function(string) {
		return this.isTrue(string);
	},
	/* 转换为整数 */
	parseInt: function(string, returnValue, radix) {
		return NumberHelper.intValue(string, returnValue, radix);
	},
	
	/* 转换为整数 */
	parseLong: function(string, returnValue, radix) {
		return this.parseInt(string, returnValue, radix);
	},
	
	/* 转换为小数 */
	parseDouble: function(string, returnValue) {
		return NumberHelper.diubleValue(string, returnValue);
	},
	
	/**
	 * 返回string的字节长度
	 */
	byteLength: function(string) {
		if (string === null || Object.isUndefined(string)) {
			return 0;
		}
		var str = String(string);
		var strcode;
		var length = 0;
		for (var i = 0, l = str.length; i < l; i = i + 1) {
			strcode = str.charCodeAt(i);
			length += Math.ceil(strcode.parseString(16).length / 2);
		}
		return length;
	},
	
	evalJSON: function(string, sanitize) {
		var json = null;
		try {
			json = this.trim(string).evalJSON(sanitize);
		} catch (error) {
			// alert(error.message);
		}
		return json;
	},
	
	/*允许你自定义含有占位符的字符串，并且传递任意数量的参数去替代这些占位符。*每一个占位符必须是唯一的，并且以{0}、{1}…这种格式递增。   
    
          *用法示例：   
    
     * <pre><code>   
    
 var cls = 'my-class', text = 'Some text';   
    
 var s = String.format('<div class="{0}">{1}</div>', cls, text);   
    
 // s now contains the string: '<div class="my-class">Some text</div>'   
    
 </code></pre>   
    
      * @参数1 {String} string 含有占位符，需要格式化的字符串   
    
      * @参数2 {String} value1 替代占位符 {0}的字符串   
    
      * @参数3 {String} value2  替代占位符{1}的字符串，以此类推   
    
      * @返回值 {String} 格式化好的字符串   
    
      * @静态方法   
    
      */
	format : function(format, args){
    	args = Object.isArray(args) ? args : Array.prototype.slice.call(arguments, 1);
		return format.replace(/\{(\d+)\}/g, function(m, i){
			return args[i];
		});
	},
	escape: function(string) {
		return string.replace(/('|\\|")/g, "\\$1");    
	}
};

Helper.add("StringHelper", StringHelper);	// 为Helper添加成员

/* 此处为string类添加成员 */
Object.extend(String.prototype, {
	trim: function() {
		return StringHelper.trim(this);
	},
	trimLeft: function() {
		return StringHelper.trimLeft(this);
	},
	trimRight: function() {
		return StringHelper.trimRight(this);
	},
	parseBoolean: function() {
		return StringHelper.parseBoolean(this);
	},
	parseInt: function(returnValue, radix) {
		return StringHelper.parseInt(this, radix, returnValue);
	},
	parseLong: function(returnValue, radix) {
		return StringHelper.parseLong(this, radix, returnValue);
	},
	parseDouble: function(returnValue) {
		return StringHelper.parseDouble(this, returnValue);
	},
	replaceAll: function (target, replacement, isIgnoreCase) {
		return StringHelper.replaceAll(this, target, replacement, isIgnoreCase);
	},
	nowrap: function() {
		return StringHelper.nowrap(this);
	},
	indexInArray: function(array, isIgnoreCase, isIgnoreEmpty) {
		return StringHelper.indexInArray(this, array, isIgnoreCase, isIgnoreEmpty);
	},
	isInArray: function(array, isIgnoreCase, isIgnoreEmpty) {
		return this.indexInArray(array, isIgnoreCase, isIgnoreEmpty) > -1;
	},
	byteLength: function() {
		return StringHelper.byteLength(this);
	}
});