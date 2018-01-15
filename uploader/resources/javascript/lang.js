/** 当前使用的资源文件,用于国际化 */
var Language = {
	URL : Base.SERVER_ROOT + 'getResources.do',
	/** 所有资源文件 */
	resources : {
		get : Prototype.emptyFunction
	},
	readyStatus : false,
	listeners : [],
	ready : function(listener) {
		if (listener) this.listeners.push(listener);
		if (!document.body) {
			// 尚未加载完成
			// alert('xx');
			return window.setTimeout((function() {
				this.ready();
			}).bind(this), 100);
		}
		if (this.readyStatus) {
			// 执行程序
			while (listener = this.listeners.shift()) {
				listener.apply(this);
			}
		}
	},
	/** 加载所有国际化语言信息 */
	load : function(url) {
		var onSuccess = (function(response) {
			var text = response.responseText;
			// alert(text + ' ' + document.body);
			this.resources = new Hash(StringHelper.evalJSON(text));
			this.readyStatus = true;
			this.ready();
		}).bind(this);
		AjaxHelper.send(url || this.URL, {
			onSuccess : onSuccess
		});
	},
	/**
	 * 得到key对应的描述，如果key不存在则返回null
	 * 
	 * @param key
	 * @param args 格式化占位符的参数
	 * @returns {String}
	 */
	find : function(key, args) {
		var value = this.resources.get(key);
		if (Object.isUndefined(value)) return null;
		if (value && args) {
			args = Object.isArray(args) ? args : Array.prototype.slice.call(arguments, 1);
			return StringHelper.format(value, args);
		}
		return value;
	},

	/**
	 * 得到key对应的描述, 如果key未找到则返回key本身
	 * 
	 * @param key
	 * @param args 格式化占位符的参数
	 * @returns {String}
	 */
	get : function(key, args) {
		var value = this.resources.get(key);
		if (Object.isUndefined(value)) value = key;
		if (value && args) {
			args = Object.isArray(args) ? args : Array.prototype.slice.call(arguments, 1);
			return StringHelper.format(value, args);
		}
		return value;
	},

	/**
	 * 得到key对应的描述, 如果key未找到则返回defaultValue
	 * 
	 * @param key
	 * @param defaultValue 返回的默认值
	 * @param args 格式化占位符的参数
	 * @returns {String}
	 */
	getText : function(key, defaultValue, args) {
		var value = this.resources.get(key);
		if (Object.isUndefined(value)) value = defaultValue;
		if (value && args) {
			args = Object.isArray(args) ? args : Array.prototype.slice.call(arguments, 2);
			return StringHelper.format(value, args);
		}
		return value;
	}
};

/** 加载资源文件中的数据 */
Language.load();
