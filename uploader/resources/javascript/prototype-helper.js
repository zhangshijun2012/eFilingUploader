(function() {
	var browser = {
		msie: false, 
		firefox: false, 
		opera: false, 
		safari: false, 
		chrome: false, 
		netscape: false, 
		appname: 'unknown', 
		version: 0
	};
	var userAgent = window.navigator.userAgent.toLowerCase();
	
    if (/(msie|firefox|opera|chrome|netscape)\D+(\d[\d.]*)/.test( userAgent ) ){
        browser[RegExp.$1] = true;
        browser.appname = RegExp.$1;
        browser.version = RegExp.$2;
    } else if ( /version\D+(\d[\d.]*).*safari/.test( userAgent ) ){ // safari
        browser.safari = true;
        browser.appname = 'safari';
        browser.version = RegExp.$2;
    }
    browser.IE7 = browser.msie && browser.version == '7.0';
    Object.extend(Prototype.Browser, Prototype.browser = browser);
	var ABORT = {
		abort : function() {
			// 取消调用
			this.transport.onreadystatechange = Prototype.emptyFunction;
			this.transport.abort();
			try {
				var response = new Ajax.Response(this);
				if (this.options['onAbort']) {
					this.options['onAbort'](response, response.headerJSON);
				}
				Ajax.Responders.dispatch('onAbort', this, response, response.headerJSON);
			} catch (e) {
				this.dispatchException(e);
			}
		}
	};
	// ajax增加abort方法
	Object.extend(Ajax.Request.prototype, ABORT);
	Ajax.Request.subclasses.each(function(subclass) {
		Object.extend(subclass.prototype, ABORT);
	});

	/** Object对象 */
	Object.extend(Object, {
		/** object的对象类型 */
		getClass : function(object) {
			return Object.prototype.toString.call(object).match(/^\[object\s(.*)\]$/)[1];
		},
		/** object是否为Object类型 */
		isObject : function(object) {
			return !!object && Object.getClass(object) == "Object";
		}
	});

	/** 数组 */
	Object.extend(Array.prototype, {
		/**
		 * 
		 * 在数组中删除第index个元素
		 * 
		 * @param index
		 *            要删除的元素.可以为负数,表示倒数第index个元素.如果index >= this.length 或index < -this.length,不会删除任何数据
		 * @param howmany 删除的数量，默认为1
		 * @returns
		 */
		removeAt : function(index, howmany) {
			if (index < -this.length) return null;
			return this.splice(index, howmany || 1);
		},

		/**
		 * 
		 * 在数组中删除值为value的元素
		 * 
		 * @param value
		 * @returns
		 */
		remove : function(value) {
			var index = this.indexOf(value);
			if (index < 0) return null; // 数组中没有value这个值
			this.splice(index, 1);
			return true;
		},
		/**
		 * 在第index个索引后面插入元素.此方法会返回数组自身
		 * 
		 * @param index
		 * @param args 要插入的数据或则数组
		 * @returns this
		 */
		insert : function(index, args) {
			if (Object.isArray(args)) {
				args.splice(0, 0, index);
			} else {
				args = $A(arguments);
			}
			args.splice(1, 0, 0);
			this.splice.apply(this, args);
			return this;
		}
	});

	var TRUE = ['true', 'yes', '1', 't', 'y'];
	Object.extend(Boolean, {
		/**
		 * value转换为boolean对象
		 * @param value
		 * @returns {Boolean}
		 */
		parseBoolean: function(value) {
			if (value === true || value === false || 
					value === 1 || value === 0 || value === null || 
					Object.isUndefined(value)) return !!value;
			return !!value && TRUE.include(String.trim(value).toLowerCase());
		}
	});
	
	var NATIVE_JSON_PARSE_SUPPORT = Try.these(
		function() { return !!window.JSON && typeof JSON.parse === 'function' && !!JSON.parse('{"test": new Date() }').test; },
		function() { return false; }
	);
	/** 为字符串添加函数 */
	Object.extend(String.prototype, {
		/** 除去前后的空格 */
		trim : function() {
			return this.strip();
		},
		/** 转换为boolean */
		parseBoolean: function() {
			return Boolean.parseBoolean(this);
		},

		/** 覆盖evalJSON方法,在IE9,IE10中，对于json字符串中new Object()的形式会报错 */
		evalJSON: function(sanitize) {
			var json = this.unfilterJSON();
			if (NATIVE_JSON_PARSE_SUPPORT) return JSON.parse(json);
			
			var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g;
			if (cx.test(json)) {
				json = json.replace(cx, function (a) {
					return '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
				});
			}
			try {
				if (!sanitize || json.isJSON()) return eval('(' + json + ')');
			} catch (e) { }
			throw new SyntaxError('Badly formed JSON string: ' + this.inspect());
		},
		
		/** 转换为json对象,不抛出异常，无法转换则返回null */
		toJSON: function(sanitize) {
			return Try.these(
				(function() { return this.evalJSON(sanitize); }).bind(this), 
				function() { return null; }
			);
		},
		/**
		 * 
		 * 更改String的toUpperCase方法,可传入参数指定要大写的起止位置
		 * 
		 * @param fromIndex
		 * @param endIndex
		 * @returns
		 */
		toUpperCaseSubstring : function(fromIndex, endIndex) {
			if (Object.isUndefined(fromIndex)) fromIndex = 0; // return this.toUpperCase();
			if (!endIndex) endIndex = fromIndex + 1;
			var value = this.substring(fromIndex, endIndex);
			if (!value) return this;
			return this.substring(0, fromIndex) + value.toUpperCase()
					+ (endIndex < this.length ? this.substring(endIndex) : '');
		},

		/**
		 * 更改String的toLowerCase方法,可传入参数指定要小写的起止位置
		 * 
		 * @param fromIndex
		 * @param endIndex
		 * @returns
		 */
		toLowerCaseSubstring : function(fromIndex, endIndex) {
			if (Object.isUndefined(fromIndex)) fromIndex = 0; //return this.toLowerCase();
			if (!endIndex) endIndex = fromIndex + 1;
			var value = this.substring(fromIndex, endIndex);
			if (!value) return this;
			return this.substring(0, fromIndex) + value.toLowerCase()
					+ (endIndex < this.length ? this.substring(endIndex) : '');
		}
	});

	/** 为字符串添加静态函数 */
	Object.extend(String, {
		trim : function(str) {
			return String.interpret(str).trim();
		},
		/** 将字符串string转换为json对象,不抛出异常，无法转换则返回null */
		toJSON : function(string, sanitize) {
			var json = this.trim(string).toJSON(sanitize);
			return json;
		}
	});
	
	var show = Element.show;
	// var hide = Element.hide;
	var CLASS_NAME_HIDDEN = ['hidden', 'hide'];	// 隐藏元素的样式
	// var CLASS_NAME_SHOW = ['show', 'visible'];	// 显示元素的样式
	Element.addMethods({
		getTextContent: function(element) {
			return String.trim(element.innerText || element.textContent);
		},
		visible: function(element) {
			return Element.getStyle(element, 'display') !== 'none';
		},
		show: function(element) {
			show(element);
			CLASS_NAME_HIDDEN.each(function(value){
				Element.removeClassName(element, value);
			});
		},
		/**
		 * 设置样式
		 * @param element
		 * @param className
		 * @returns
		 */
		setClassName: function(element, className) {
			if (!(element = $(element))) return;
			return element;
		},
		
		/**
		 * 得到一个boolen型的属性。如果属性key存在，且为指定值，则视为true
		 * @param element
		 * @param key
		 */
		getBoolean: function(element, key) {
			if (!(element = $(element))) return;
			var attribute = element.getAttribute(key);
			return !attribute && element.hasAttribute(key) || attribute && Boolean.parseBoolean(attribue);
		}
		/*,
		hide: function(element) {
			hide(element);
			CLASS_NAME_SHOW.each(function(value){
				Element.removeClassName(element, value);
			});
		}*/
	});
	
	Object.extend(Form, {
		/**
		 * 设置元素element的值
		 * @param element form或form的field
		 * @param values 要设定的值
		 * @param index 如果是values为数组,指定元素取值的
		 * @returns
		 */
		setValue : function(element, values, index) {
			if (!Object.isArray(values) && typeof values == 'object') {
				// 如果values为JSON数据,则element为表单
				var elements = element.elements;
				for (var name in values) {
					Form.setValue(elements[name], values[name]);
				}
		    	return;
		    }
			if (!Object.isElement(element)) {
		    	// element为多个元素
				for (var i = 0, length = element.length; i < length; i++) {
		    		Form.setValue(element[i], values, i);
				}
				return;
			}
		    var tagName = element.tagName.toLowerCase();
		    /* select下拉列表 */
		    if ('select' == tagName) return Form.selectValue(element, values);
		    switch (element.type.toLowerCase()) {
	    		case 'file':
	    			return;
		    	case 'checkbox':
		    	case 'radio':
		    		return Form.checkValue(element, values);
		    	default:
		    		return element.value = (Object.isArray(values) ? values[index || 0] : values) || '';
		    }
		},

		/**
		 * checkbox或者radio选择值
		 * 
		 * @param element html元素,可能是元素数组
		 * @param value 需要选中的值,可能是数组
		 */
		checkValue: function(element, value) {
			var opt, currentValue, single = !Object.isArray(value);
			if (Object.isElement(element)) {
				currentValue = element.value;
				element.checked = single ? currentValue == value : value.include(currentValue);
			}
			var elements = element;
			for (var i = 0, length = elements.length; i < length; i++) {
				element = elements[i];
				currentValue = element.value;
				element.checked = single ? currentValue == value : value.include(currentValue);
			}
		},
		
		/** select下拉列表选择值 */
		selectValue: function(element, value) {
			var opt, currentValue, single = !Object.isArray(value);
			for (var i = 0, length = element.length; i < length; i++) {
				opt = element.options[i];
				currentValue = opt.value;
				opt.selected = single ? currentValue == value : value.include(currentValue);
			}
		}
	});
	


	/** 重写$$方法，使之第一个参数可以传入父节点 */
	window.$$ = function(root) {
		var args = $A(arguments);
		if (Object.isElement(root)) {
			args.removeAt(0);
		} else root = null;
		var expression = args.join(', ');
		return Prototype.Selector.select(expression, root || document);
	};
})();