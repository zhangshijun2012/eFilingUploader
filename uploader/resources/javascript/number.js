// JavaScript Document
NumberHelper = {
	/**
	 * 判断number是否为数字
	 */
	isNumber: function(number) {
		if ((!number && number !== 0) || number === true || StringHelper.isEmpty(number)) return false;
		return (Object.isNumber(number) && !isNaN(number)) ||
			(!Object.isUndefined(number) && number !== null && !isNaN(StringHelper.trim(number).replaceAll(",", "")));
	},
	
	/**
	 * 将number转换为radix进制的数字
	 * defaultValue: number不是数字或者转换失败时返回的值
	 */
	intValue: function(number, defaultValue, radix) {
		var v = (defaultValue = !this.isNumber(defaultValue) ? 0 : defaultValue);
		if (Object.isUndefined(number) || (!number && number !== 0)) {
			return v;
		}
		
		try {
			/* 将arg以radix进制转换 */
			v = parseInt(String(number).trim().replaceAll(",", ""), radix ? radix : 10);
			if (!this.isNumber(v)) {
				v = defaultValue;
			}
		} catch (error) {
			v = defaultValue;
		}
		return v;
	},
	
	/**
	 * 将number转换为小数
	 * defaultValue: number不是数字或者转换失败时返回的值
	 */
	doubleValue: function(number, defaultValue) {
		var v = (defaultValue = !this.isNumber(defaultValue) ? 0 : defaultValue);
		if (Object.isUndefined(number) || (!number && number !== 0)) {
			return v;
		}
		
		try {
			v = parseFloat(StringHelper.trim(number).replaceAll(",", ""));
			if (!this.isNumber(v)) {
				v = defaultValue;
			}
		} catch (error) {
			v = defaultValue;
		}
		return v;
	},

	/** 格式化数字
	 * number:要格式化的数字
	 * maximumFractionDigits:小数点后保留的最大位数,默认为2
	 * maximumFractionDigits:小数点后保留的最小位数,默认与最大位数一致
	 * separator: 千分位的符号,默认为逗号,
	 */
	format: function(number, maximumFractionDigits, minimumFractionDigits, separator) {
		maximumFractionDigits = (!this.isNumber(maximumFractionDigits)) ? 2 : this.doubleValue(maximumFractionDigits);
		minimumFractionDigits = (!this.isNumber(minimumFractionDigits)) ? 
			maximumFractionDigits : this.doubleValue(minimumFractionDigits);
		minimumFractionDigits = minimumFractionDigits > maximumFractionDigits ? maximumFractionDigits : minimumFractionDigits;
		
		if (Object.isUndefined(separator)) {
			if (!this.isNumber(maximumFractionDigits)) separator = maximumFractionDigits;
			else if (!this.isNumber(minimumFractionDigits)) separator = minimumFractionDigits;
		}
		separator = Object.isUndefined(separator) ? ',' : (separator || '');
		
		number = this.doubleValue(number);
		if (!this.isNumber(number)) {
			return number;
		}
		
		var isNegative = number < 0;	//是否小于0
		number = Math.abs(number);
		
		var value = String(number);
		var decimal = "";
		var p = value.indexOf(".");
		
		if ((minimumFractionDigits <= 0 && p < 0) || maximumFractionDigits <= 0) {
			//不需要小数位
			number = Math.round(number);
			value = String(number);
		} else {
			decimal = p >= 0 ? value.substring(p + 1) : "";
			p = decimal.length;	//计算小数点后应该保留的位数[minimumFractionDigits, maximumFractionDigits]
			
			if (p < minimumFractionDigits) {
				p = minimumFractionDigits;
			} else if (p > maximumFractionDigits) {
				p = maximumFractionDigits;
			}
			decimal = Math.pow(10, 0 - p - 1) * 5;
			number = number + decimal;
			value = String(number);
			
			decimal = value.indexOf(".");
			if (decimal > -1) {
				var i = decimal;
				decimal = value.substring(i);		//小数部份
				value = value.substring(0, i);		//整数部分
			} else {	//没有小数位用0填充
				decimal = ".";
				for(i = 0; i < p; i++) {
					decimal = decimal + "0";
				}
			}
			
			decimal = decimal.substring(0, p + 1);	//整数部分
		}
		
		if (!separator) {
			value = value + decimal;
		} else {
			var v = value;
			value = "";
			for (var l = v.length, i = 0; i < l; i++) {
				if (i > 0 && i % 3 == 0) {
					value = separator + value;
				}
				value = v.charAt(l - 1 - i) + value;
			}
			
			value = value + decimal;
		}
		if (isNegative) {
			value = "-" + value;
		}
		
		return value;
	}
}

Helper.add("NumberHelper", NumberHelper);	// 为Helper添加成员
