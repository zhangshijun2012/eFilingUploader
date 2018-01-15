/**
 * org.anywnyu.helper.Helper
 * author: LuoGang
 */
var Helper = {
	version: "1.0",
	
	/** 页面请求参数 */
	request: {
		/** 初始化 */
		init: function() {
			this.queryString = window.location.search || '';
			var parameters = this.parameters = { };
			if (!this.queryString) return this.queryString;
			this.queryString = this.queryString.substring(1);
			var values = this.queryString.split('&');
			values.each(function(value, index) {
				if (!value) return;
				var pair = value.split('=');
				var key = pair[0] || '';
				if (!key) return;
				value = pair[1] || '';
				key = decodeURIComponent(key);
				value = decodeURIComponent(value);
				if (parameters[key]) {
					parameters[key].push(value);
				} else {
					parameters[key] = [ value ];
				}
			});
			return this.queryString;
		},
		queryString: '',
		getQueryString: function() {
			return this.queryString;
		},
		parameters: null,
		getParameter: function(key) {
			var values = this.parameters[key];
			return values ? values[0] : null;
		},
		getParameters: function(key) {
			var values = this.parameters[key];
			return values || null;
		}
	},
	
	/* 为clazz添加子对象,name中不要带. */
	add: function(name, object, clazz) {
		if (!object) {
			return;
		}
		if (!clazz) {
			clazz = Helper;
		}
		
		if (!clazz[name]) {
			clazz[name] = { };
		}
		
		Object.extend(clazz[name], object);
	},
	
	isObject: function(o) {
		return !!o && Object.getClass(o) == "Object";
	},
	isBoolean: function(o) {
		return !!o && Object.getClass(o) == "Boolean";
	},
	/**
	 * 将source对象clone.
	 * source: 要clone的对象,必须
	 * flag: 为true则进行深度clone,即对source的属性也进行clone 
	 */
	clone: function(source, flag) {
		if (!source) return source;
		if (!Object.isUndefined(flag) && !flag) return Object.clone(source);
		if (Object.isFunction(source['clone'])) {
			return source.clone();
		}
		var destination = { };
		for (var property in source) {
			if (Object.isElement(source[property])) {
				destination[property] = source[property];
			} else if (Object.isArray(source[property])) {
				destination[property] = source[property].clone();
			} else if (this.isObject(source[property])) {
				destination[property] = this.clone(source[property], flag);
			} else {
				destination[property] = source[property];
			}
		}
	    return destination;
	},
	
	/**
	 * 使destination继承对象source
	 * flag: 为true则进行深度extend,即对source的属性也进行extend
	 */
	extend: function (destination, source, flag) {
		if (!source) return destination;
		if (!Object.isUndefined(flag) && !flag) return Object.extend(destination, source);
		for (var property in source) {			// 复制属性
			if (Object.isElement(source[property])) {
				destination[property] = source[property];
			} else if (Object.isArray(destination[property])) {
				destination[property] = source[property].clone();
			} else if (this.isObject(destination[property])) {
				this.extend(destination[property], source[property], flag);
			} else {
				destination[property] = source[property];
			}
		}
		
		return destination;
	},
	
	FileHelper: 	{ }, 	// 文件处理工具
	EventHelper: 	{ },	// 事件处理工具
	ElementHelper:	{ },	// html元素处理工具
	GridHelper: 	{ }, 	// 表格处理工具
	StringHelper: 	{ },	// 字符串处理工具
	NumberHelper: 	{ },	// 数字处理工具
	DateHelper: 	{ }		// 日期处理工具
};
Object.isObject = Helper.isObject;
Object.isBoolean = Helper.isBoolean;
Helper.request.init();
/*------ FileHelper = Helper.FileHelper -----------------------------------------------*/
var FileHelper = { }; // 文件处理工具
/**
 * 在窗口加载完成前调用,用于引入css或javascript文件.<b>
 * 主要调用document.write
 */
FileHelper.Includer = {
	/**
	 * 加载过的文件对象
	 */
	files: { },
	include: function(files, type, options) {
		if (!files) {
			return false;
		}
		
		var _files = [];
		if (Object.isString(files)) {
			_files[0] = files;
		} else {
			_files = $A(files);
		}
		
		var _options = {
			directory: null,
			absolute: null,
			always: null,
			path: null
		};
		options = options || { };
		Object.extend(_options, options || { });
		if (_options.directory && !_options.directory.endsWith("/")) {
			_options.directory = _options.directory + "/";
		}
		if (!_options.path) {
			_options.path = (_options.absolute ? SERVER_ROOT : "") + (_options.directory ? _options.directory : "");
		}
		
		type = type ? type.toLowerCase() : "text/javascript";
		var count = 0;
		// 循环加载所有文件
		for (var i = 0, l = _files.length - 1; i <= l; i++) {
			var file = _files[i];
			file = file.replaceAll("\\\\", "\/");	// 用/替换掉\
			while (file.startsWith("\./")) {		// 除去最前面的./
				file = file.substring(2);
			}
			file = _options.path ? _options.path + file : file;
			if (this.files[file] && !_options.always) {
				return;
			}
			
			this.files[file] = null;

			// text/css和text/javascript
		
			if (type.include("css")) {	// css
				document.write("<link type=\"" + type + "\" href=\"" + file + "\" rel=\"stylesheet\" />");
			} else if (type.include("script")) { // javascript
				document.write("<script type=\"" + type + "\" src=\"" + file + "\"></script>");
			}
			count++;
    	}
    	return count;
	}
};

/**
 * 文件处理工具中的文件加载器
 * 判断文件是否加载完成的方法:
 *  1:onload，支持者有firefox、chrome、safari、opera
 *  2:onreadystatechange，支持者有ie、opera。IE和opera支持的还不一样。在加载时，IE的readyState是 loading，opera的是interactive
 */
FileHelper.Loader = {
	loader: function() {
		var files = null;			// 加载的文件
		var loadedFiles = { };		// 已加载过的文件
		var count = 0;				// 每一次加载的数量.只有count与要加载的数量一致才表示成功
		var successCount = 0;		// 成功的数量
		var completeCount= 0;		// 完成的数
		
		var options = null;			// 加载时的参数
		var type = null;			// 加载的文件类型
		var attributes = null;		// 加载的属性
		
		this.isSuccess = function() {	// 是否加载成功
			return successCount == count;
		};
		
		this.isComplete = function() {	// 是否加载完成
			return completeCount == count;
		};
		/**
		 * 设置参数
		 */
		this.setOptions = function(_options) {
			_options = _options || { };
			options = Object.clone(_options);
			
			if (options.directory && !options.directory.endsWith("/")) {
				options.directory = options.directory + "/";
			}
			if (!options.path) {
				options.path = (options.absolute ? SERVER_ROOT : "") + (options.directory ? options.directory : "");
			}
			
			/**
			 * file: 某一个文件成功时的事件
			 * element: 节点对象
			 */
			options.onSuccess = (function(file, element) {
				if (loadedFiles[file]) {
					// 已经加载完成
					return;
				}
				try {
					if (!Base.logined) {
						alert("你尚未登录或者登录已超时,请重新登录!");
						return Base.login();
					}
					
					if (Object.isFunction(options["onSuccess" + file])) {
						options["onSuccess" + file](file, element);
					}
					successCount++;
					options.onComplete(file, element);
				} catch (error) {
					options.onError(error, file, element);
				}
			}).bind(this);
			
			var onError = _options.onError;
			options.onError = (function(error, file, element) {
				try {
					if (!Base.logined) {
						alert("你尚未登录或者登录已超时,请重新登录!");
						return Base.login();
					}
					
					if (file && Object.isFunction(options["onError" + file])) {
						options["onSuccess" + file](file, element);
						return;
					}
					
					if (Object.isFunction(onError)) {
						onError(error, file, element);
					}
				} catch (error) {
				}
				options.onComplete(file, element);
			}).bind(this);
			
			
			var onSuccess = _options.onSuccess;
			var onFailure = _options.onFailure;
			var onComplete = _options.onComplete;
			options.onComplete = (function(file, element) {
				if (!loadedFiles[file]) {
					// 防止重复调用
					completeCount++;
					loadedFiles[file] = true;
				}
				if (this.isComplete()) {
					if (Object.isFunction(onComplete)) {
						onComplete(files, this.isSuccess());
					}
					if (!this.isSuccess()) {
						// 失败
						if (Object.isFunction(onFailure)) {
							onFailure(files);
						}
						return;
					}
					
					if (Object.isFunction(onSuccess)) {
						onSuccess(files);
					}
					
					for (var i = 0, l = files.length; i < l; i++) {
						FileHelper.Loader.files[files[i]] = true;
					}
				}
			}).bind(this);
			
		};
		
		/**
		 * 动态加载文件,可加载script或css等
		 * type: 加载的文件类型.暂时只支持text/css和text/javascript
		 * scripts: 要加载的文件,即src=script
		 * attributes: 节点的属性
		 * options {
		 *  onsuccess: 加载完成之后执行的事件
		 *  onFailure: 加载失败之后执行的事件
		 * 	directory: 加载directory目录中的文件，必须以/结尾.此参数有效,则src=directory + script
		 * 	absolute: 是否加上绝对路径,默认为false.若为true则加上SERVER_ROOT,则src=SERVER_ROOT + directory + "/" + script.
		 * 	always: 是否总是重新加载,默认为false,一旦加载过该文件则不再进行加载
		 * }
		 */
		this.load = function(_files, _type, _attributes, _options) {
			// this.loadedSuccess = 0;
			if (!_files) {
				// 未指定加载对象
				return false;
			}
			
			if (Object.isString(_files)) {
				files = _files.split(",");
			} else {
				files = $A(_files);
			}
			count = files.length;
			successCount = 0;		// 成功的数量
			completeCount= 0;		// 完成的数
			
			type = _type ? _type.toLowerCase() : "text/javascript";			
			attributes = Object.clone(_attributes || { });
			this.setOptions(_options);
			
			// 循环加载所有文件
			for (var i = 0; i < count; i++) {
				var file = files[i];
				file = file.replaceAll("\\\\", "\/");	// 用/替换掉\
				while (file.startsWith("\.\/")) {		// 除去最前面的./
					file = file.substring(2);
				}
				
				file = options.path ? options.path + file : file;
				
				loadOneLile(files[i] = file);
	    	}
		};
		
		/**
		 * 加载单个文件.私有方法不在外部调用
		 */
		var loadOneLile = function(file) {
			// alert('file = ' + file);
			if (FileHelper.Loader.files[file] && !options.always) {	// 文件存在,并且不需要重新加载
				options.onSuccess(file, element);
				return;
			}
			
			var element = null;
			
			if (type.include("css")) {	//处理css
				element = document.createElement("link");
				element.rel = "stylesheet";
				element.href = file + '?DATE=' + new Date();
			} else if (type.include("script")) {
				element = document.createElement("script");
				element.src = file + '?DATE=' + new Date();
			}
			element.id = 'loadedFile_' + file;
			element.type = type;
			for (var p in attributes) {
				element.setAttribute(p, attributes[p]);
			}
			
			element.onreadystatechange = function () {
				// ie、opera
				var state = element.readyState;
				if(state == "complete" || state == "loaded") {
					options.onSuccess(file, element);
				}
			};
			
			/**
			 * 加载出错的调用,onerror与onload在IE中不触发
			 */
			element.onerror = function(error) {
				options.onError(error, file, element);
			}
			
			element.onload = function () {
				// firefox、chrome、safari、opera
				options.onSuccess(file, element);
			};
			
			// 节点加入之后在FF中才会触发onLoad事件.
			document.getElementsByTagName("head")[0].appendChild(element);
		};
		
	},
	/**
	 * 加载过的文件对象,访问时使用FileHelper.Loader.files
	 */
	files: { },
	
	load: function(files, type, attributes, options) {
		var loader = new this.loader();
		return loader.load(files, type, attributes, options);
	}
};

/**
 * 可加载的文件类型
 */
FileHelper.Loader.Types = FileHelper.Includer.Types = {
	css: "text/css",				// css
	javascript: "text/javascript"	// javascript
};
/* 为Helper添加子对象 */
Helper.add("FileHelper", FileHelper);
/*------ FileHelper = Helper.FileHelper -----------------------------------------------*/
/*-------------------------------------------------------------------------------------*/


/*-------------------------------------------------------------------------------------*/
/*------ EventHelper = Helper.EventHelper ---------------------------------------------*/
var EventHelper = {
	/**
	 * 获取触发的事件
	 * top: 获取最顶层的事件.主要用于FF的事件获取到原始的事件
	 */
	get: function (top) {
		if (Prototype.Browser.IE) {
			return Event.extend(window.event);
		}
		var caller = this.get.caller;
		var event, arg;
		while (caller != null) {
			arg = caller.arguments[0];
			if (arg && ((arg.constructor == Event || arg.constructor == MouseEvent) 
				|| (typeof (arg) == "object" && arg.preventDefault && arg.stopPropagation))) {
				if (top) {
					event = arg;
				} else {
					return  Event.extend(arg);
				}
			}
			caller = caller.caller;
		}
		return event ? Event.extend(event) : null;
	},
	
	
	/**
	 * 获取触发事件的对象
	 */
	getElement: function() {
		try {
			return Event.element(this.get());
		} catch (error) {
		}
		return null;
	},
	
	/**
	 * 获取触发事件的键值
	 */
	keyCode: function () {
		var event = this.get();
		return event.keyCode || event.which;
	},
	
	/**
	 * 是否按下数字键
	 */
	isNumberKey: function() {
		var keyCode = this.keyCode();
		return (keyCode >= 48) && (keyCode <= 57);
	},
	
	/**
	 * 是否按下回车键
	 */
	isEnter: function() {
		return this.keyCode() == Event.KEY_RETURN;
	},
	
	/**
	 * 是否按下功能键
	 */
	isFunctionKey: function() {
		var keyCode = this.keyCode();
		return keyCode == Event.KEY_BACKSPACE ||
				keyCode == Event.KEY_TAB ||
				keyCode == Event.KEY_RETURN ||
				keyCode == Event.KEY_ESC ||
				keyCode == Event.KEY_LEFT ||
				keyCode == Event.KEY_UP ||
				keyCode == Event.KEY_RIGHT ||
				keyCode == Event.KEY_DOWN ||
				keyCode == Event.KEY_DELETE ||
				keyCode == Event.KEY_HOME ||
				keyCode == Event.KEY_END ||
				keyCode == Event.KEY_PAGEUP ||
				keyCode == Event.KEY_PAGEDOWN ||
				keyCode == Event.KEY_INSERT;
	}
};

/* 为Helper添加子对象 */
Helper.add("EventHelper", EventHelper);
/*------ EventHelper = Helper.EventHelper ---------------------------------------------*/
/*-------------------------------------------------------------------------------------*/


/*-------------------------------------------------------------------------------------*/
/*------ ElementHelper = Helper.ElementHelper -----------------------------------------*/
var ElementHelper = {
	/**
	 * 创建一个html节点
	 * tagName: 节点类型
	 * attributes: 要添加的属性,使用json数据,如{id: "id", name: "name"}
	 * parent: 父节点,如果指定了父节点,则将此节点加入父节点中
	 * onCreate: 创建完成之后执行的方法,参数为创建的节点和attributes
	 * flag: 参见节点的setAttribute方法.0:覆盖同名属性,1:默认值,为属性添加指定的值.
	 */
	createElement: function(tagName, attributes, parent, onCreate, flag) {
		var e = document.createElement(tagName);
		if (attributes) {
			for (p in attributes) {
				var attribute = attributes[p];
				if (Object.isString(attribute) || Object.isNumber(attribute) 
						|| Object.isBoolean(attribute) || Object.isDate(attribute)) {
					if ('style' != p.toLowerCase() && Object.isUndefined(e[p])) {
						e.setAttribute(p, attribute, flag);
					} else {
						e[p] = attribute;
					}
				} else if ('style' != p.toLowerCase() && Object.isUndefined(e[p])) {
					e.setAttribute(p, attribute, flag);
				} else {
					var attr = e[p];
					for (p in attribute) {
						attr[p] = attribute[p];
					}
				}
				/*
				if (p == "style" && !Object.isString(attributes[p])) {
					// 设定样式
					for (style in attributes[p]) {
						e[p][style] = attributes[p][style];
					}
				} else if (Object.isUndefined(e[p])) {
					e.setAttribute(p, attributes[p], flag);
				} else {
					e[p] = attributes[p];
				}
				*/
			}
		}
		
		if (parent = $(parent)) {
			parent.appendChild(e);
		}
		
		if (Object.isFunction(onCreate)) {
			onCreate(e, attributes, parent);
		}
		return $(e);
	},
	/**
	 * 向元素element中设定属性
	 * @param element
	 * @param attributes
	 */
	setAttributes: function(element, attributes) {
		if (!attributes) return;
		for (p in attributes) {
			if (p == "style" && !Object.isString(attributes[p])) {
				// 设定样式
				for (style in attributes[p]) {
					element['style'][style] = attributes[p][style];
				}
			} else if (Object.isUndefined(element[p])) {
				element.setAttribute(p, attributes[p], flag);
			} else {
				element[p] = attributes[p];
			}
		}
	},
	
	/* 创建一个文本节点 */
	createTextNode: function(data, parent, onCreate, flag) {
		var e = document.createTextNode(data);
		
		if (parent = $(parent)) {
			parent.appendChild(e);
		}
		
		if (Object.isFunction(onCreate)) {
			onCreate(e, parent);
		}
		return e;
	},
	
	
	/**
	 * 判断元素e是否为指定的tagName
	 * e: 要判断的元素
 	 * tagName: 指定的节点名称
	 */
	isTagName: function (e, tagName) {
		e = $(e);
		return e && !StringHelper.isEmpty(tagName) && Object.isElement(e) && e.tagName.toLowerCase() == tagName.trim().toLowerCase();
	},
	
	/**
	 * 得到节点e的名为attributeName的属性值,属性名不区分大小写
	 */
	attributeValue: function(e, attributeName) {
		/* 得到e的attributeName属性值,没有则返回null */
		if ((e = $(e)) && Object.isElement(e)) { // && e.attributes[attributeName]) {
			return e.getAttribute(attributeName);
			//return e.attributes[attributeName].nodeValue;
		}
		return null;
	},
	/**
	 * 得到元素的滚动条的位置
	 */
	getScroll: function (el){
		var doc = document;
		if(!el || el == doc || el == doc.body || el == doc.documentElement) {
			var l, t;
			l = doc.documentElement.scrollLeft || doc.body.scrollLeft || window.pageXOffset || 0;
			t = doc.documentElement.scrollTop || doc.body.scrollTop || window.pageYOffset || 0;
			return { left: l, top: t };
		} else {
		    return { left: el.scrollLeft, top: el.scrollTop };
		}
	},
   
	/**
	 * 得到元素的位置
	 * scroll: 是否加上屏幕的滚动条
	 * return {
	 *	top: 距离屏幕上边的位置
	 *	right: 距离屏幕右边的位置
	 *	bottom: 距离屏幕下边的位置
	 *	left: 距离屏幕左边的位置
	 * }
	 */
	getOffset: function(el, scroll) {
		var doc = document;
		if(!el || el == doc || el == doc.body || el == doc.documentElement) {
			return {
				top: 0,
				right: document.body.clientWidth || document.documentElement.clientWidth,
				bottom: document.body.clientHeight || document.documentElement.clientHeight,
				left: 0
			}
		}
		scroll = Object.isUndefined(scroll) ? true : scroll;
		var offsets;
		if (el.getBoundingClientRect) {
			var b = el.getBoundingClientRect();
			scroll = scroll ? this.getScroll(document) : { top: 0, left: 0 };
			offsets = {
				top: b.top + scroll.top,
				right: b.right + scroll.left,
				bottom: b.bottom + scroll.top,
				left: b.left + scroll.left
			}
			return offsets;
		}
		
		//scroll = !scroll ? this.getScroll(document) : { top: 0, left: 0 };
		
		var offset = scroll ? Element.cumulativeOffset(el) : Element.viewportOffset(el)
		offsets = {
			top: offset[1],
			right: offset[0] + el.offsetWidth,
			bottom: offset[1] + el.offsetHeight,
			left: offset[0]
		}
		
		return offsets;
	},
	/* 
	 * 设置e的html 
	 * 防止在特殊清空下IE中出现的BUG,在IE中,对象的宽度会与最初加载时一致,不会随着外部的元素变化而变化,即css中的width:100%未起作用.
	 * 但是如果在innerHTML之后访问offsetWidth,offsetHeight,则不会出现问题
	 */
	innerHTML: function(e, html) {
		if (!(e = $(e))) {
			return;
		}
		
		if (html !== false) {
			e.innerHTML = html;
		}
		return;
		if (!Prototype.Browser.IE) {
			return;
		}
		
		var node = e;
		while(node && !Object.isUndefined(node.offsetWidth)) {
			try {
				//$("header").innerHTML += " " + node.id + "=" + node.offsetWidth; 
				node.offsetWidth;
				node.offsetHeight;
			} catch (error) {
				break;
			}
			node = node.parentNode;
		}
	},
	
	getTextContent: function(e) {
		if (!(e = $(e))) {
			return false;
		}
		return Try.these (
			function () {	/*IE浏览器*/;
				if (!Object.isUndefined(e.innerText)) {
					return e.innerText;
				} else {
					throw "The Browser is not IE";
				}
			},
			
			function () {
				return e.textContent;
			}
		) || "";
	},
	
	setTextContent: function(e, text) {
		if (!(e = $(e))) {
			return false;
		}
		return Try.these (
			function () {	/*IE浏览器*/;
				if (!Object.isUndefined(e.innerText)) {
					e.innerText = text;
					return true;
				} else {
					throw "The Browser is not IE";
				}
			},
			
			function () {
				if (!Object.isUndefined(e.textContent)) {
					e.textContent = text;
					return true;
				} else {
					throw "The Browser is not Netscape";
				}
			}
		) || false;
	},
	
	tab: function(field, form) {
		field = Form.getField(field, form);
		if (field && Event.isEnter()) {
			field.focus();
		}
		return false;
	},
	
	enter: function(fn, form) {
		if (Event.isEnter()) {
			if (Object.isFunction(fn)) {
				fn();
			} else if (form = Form.getForm(form)) {
				form.submit();
			}
		}
		return false;
	}
};

/*------ ElementHelper = Helper.ElementHelper -----------------------------------------*/
/*-------------------------------------------------------------------------------------*/
