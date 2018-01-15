/** 当前正在运行的AJAX实例的数量 */
Ajax.AJAX_COUNT = 0;

var LOGIN = { 
	LOGIN_CONTENT : '尚未登录或会话已经过期',
	LOGIN_HTML: '<!-- LOGIN HTML -->',
	LOGIN_SCRIPT : 'top.location="/platform/portal.do'
};

Ajax.Responders.register({
	onCreate : function(request, transport, json) {
		// 显示加载框
		request.loading = null;
		if (window['Loading']
				&& (request.options['showLoading'] || Object.isUndefined(request.options['showLoading']))) {
			request.loading = Loading;
			request.loading.show();
		}
		request.complete = false;
		request.AJAX_COUNT = 1;
		Ajax.AJAX_COUNT += 1;
	},
	onBeforeComplete : function(request, transport, json) {
		var text = transport.responseText;
    	// alert('onBeforeComplete:' + text);
		if (text) {
			text = text.trim();
			if (text.include(LOGIN.LOGIN_HTML) || text.include(LOGIN.LOGIN_CONTENT) || text.include(LOGIN.LOGIN_SCRIPT)) {
				// alert(text);
				// 需要重新登录
				// alert('尚未登录或会话已经过期,请重新登录!');
				login();
				return LOGIN;
				//	throw $break;
			}
		}
	},
	onComplete : function(request, transport, json) {
		if (request.loading) request.loading.hide();
		request.complete = true;

		Ajax.AJAX_COUNT -= request.AJAX_COUNT;
		request.AJAX_COUNT = 0;
	},
	onAbort : function(request, transport, json) {
		if (request.loading) request.loading.hide();
		request.complete = true;

		Ajax.AJAX_COUNT -= request.AJAX_COUNT;
		request.AJAX_COUNT = 0;
	},
	onException : function(request, exception) {
		if (request.loading) request.loading.hide();
		request.complete = true;
		Ajax.AJAX_COUNT -= request.AJAX_COUNT;
		request.AJAX_COUNT = 0;
		if (exception != $break) {
			// alert('JS脚本错误:' + exception);
			// throw exception;
		}
	}
});

// 抛出异常
Ajax.Responders.dispatch = function(callback, request, transport, json) {
	var exception;
	this.each(function(responder) {
		var v;
		if (Object.isFunction(responder[callback])) {
			try {
				v = responder[callback].apply(responder, [ request, transport, json ]);
			} catch (e) {
				if (e != $break) exception = e;
			}
			
			if (v == LOGIN) throw exception = $break;	// 如果返回的是LOGIN，表示尚未登录，则调出循环，并返回异常，终止后面代码的执行
		}
	});
	if (exception) throw exception;
};

(function() {
	var respondToReadyState = Ajax.Request.prototype.respondToReadyState;
	// alert(respondToReadyState);
	Ajax.Request.prototype.respondToReadyState = function(readyState) {
	    var state = Ajax.Request.Events[readyState];
	    if (state == 'Complete') {
	    	// 执行beforeComplete方法
	    	// alert('onBeforeComplete');
	    	var response = new Ajax.Response(this);
	        Ajax.Responders.dispatch('onBefore' + state, this, response, response.headerJSON);
	    }
		respondToReadyState.apply(this, arguments);
	};
})();
Ajax.Support = Class.create({
	// refresh如果为true,则ajax请求会在url后加上时间戳参数___REFRESH_DATE___
	refresh : true, // StringHelper.parseBoolean(Helper.request.getParameter('refresh')),
	requester : null,
	updater : null,
	initialize : function(requester, updater) {
		if (Object.isUndefined(requester) || requester === true) this.requester = new Ajax.Support.Request();
		else this.requester = requester || null;
		if (Object.isUndefined(requester) || updater === true) this.updater = new Ajax.Support.Updater();
		else this.updater = updater || null;
	},
	/**
	 * 发送ajax请求
	 * 
	 * @param url
	 * @param options
	 * @returns
	 */
	request : function(url, options) {
		return this.requester.request(url, options);
	},
	/** 发送ajax请求，同this.request */
	send : function(url, options) {
		return this.request(url, options);
	},
	/**
	 * 执行ajax调用，然后将内容更新container元素
	 * 
	 * @param container
	 * @param url
	 * @param options
	 * @returns
	 */
	update : function(container, url, options) {
		return this.updater.request(container, url, options);
	},

	/** 取消所有请求 */
	abort : function() {
		if (this.requester) this.requester.abort();
		if (this.updater) this.updater.abort();
	},
	/** 取消所有请求,同this.abort */
	stop : function() {
		this.abort();
	}
});

/** Ajax请求的处理对象 */
Ajax.Support.Request = Class.create(PeriodicalExecuter, {
	transport : null,
	refresh : true, // StringHelper.parseBoolean(Helper.request.getParameter('refresh')),
	queue : [],
	initialize : function($super, callback, frequency) {
		// 默认为0.1秒循环一次
		$super(callback || Prototype.emptyFunction, frequency || 0.05);
	},
	/** 定时器启动 */
	start : function() {
		if (this.timer) return;
		this.registerCallback();
	},
	/**
	 * 接收参数.如果可立即执行则返回true,否则返回false
	 * 
	 * @returns {Boolean}
	 */
	accept : function() {
		if (this.currentlyExecuting) return false;
		if (this.queue.length > 0) {
			this.onTimerEvent(); // 已经空闲，则立即执行一次
			return false;
		}
		if (this.transport && !this.transport.complete) return false;
		return true;
	},
	/**
	 * 外部调用的主要方法，执行ajax请求
	 * 
	 * @param url
	 * @param options
	 * @returns
	 */
	request : function(url, options) {
		var args = $A(arguments);
		if (this.accept()) {
			return this.transport = this.run.apply(this, args);
		} else {
			this.queue.push(args);
			this.start();
		}
	},

	/**
	 * 立即执行请求ajax请求，不判断队列情况.外部最好不要调用此方法
	 * 
	 * @param url 地址
	 * @param options 参数
	 * @returns {Ajax.Request}
	 */
	run : function(url, options) {
		if (this.refresh) {
			url += (url.include('?') ? '&' : '?') + '___REFRESH_DATE___=' + new Date();
		}
		return new Ajax.Request(url, options);
	},

	/**
	 * 定时任务的执行函数
	 * 
	 * @returns
	 */
	execute : function($super) {
		$super();
		if (this.queue.length <= 0) return this.stop(); // 没有需要处理的队列
		if (this.transport && !this.transport.complete) return; // 当前任务尚未处理完毕
		this.run.apply(this, this.queue.shift());
	},

	/**
	 * 取消所有请求
	 */
	abort : function() {
		if (this.transport) this.transport.abort();
		this.stop();
		this.queue.clear();
	}
});
/** Ajax直接更新元素的处理对象 */
Ajax.Support.Updater = Class.create(Ajax.Support.Request, {
	/**
	 * 执行请求
	 * 
	 * @param container 容器
	 * @param url 地址
	 * @param options 参数
	 * @returns {Ajax.Request}
	 */
	run : function(container, url, options) {
		if (this.refresh) {
			url += (url.include('?') ? '&' : '?') + '___REFRESH_DATE___=' + new Date();
		}
		options = options || {};
		var _onComplete = options['onComplete'];
		options['onComplete'] = function() {
			if (_onComplete) _onComplete.apply(this, $A(arguments));
			CalendarHelper.observe(container);
		};
		return new Ajax.Updater(container, url, options);
	}
});

/** 实用工具,一些基本的静态方法.可以进行排队处理的. */
var AjaxHelper = new Ajax.Support();
Helper.add("AjaxHelper", AjaxHelper);

/** XML的一些使用 */
var XMLHelper = {
	/**
	 * 根据XPath表达式获取节点
	 * 
	 * @param node 要计算xpath的节点
	 * @param xpath XPath表达式
	 * @returns
	 */
	selectNodes : function(node, xpath) {
		if (!node) return [];
		var xml = node.ownerDocument || node;
		if (window['XPathResult']) {
			// 其他浏览器
			// xmlDoc.evaluate(xpath, xmlDoc, null, XPathResult.ANY_TYPE,null);
			return xml.evaluate(xpath, node, null, XPathResult.ANY_TYPE, null);
		}
		// IE,注意在IE10下无法支持XPATH
		try {
			if (xml) xml.setProperty("SelectionLanguage", "XPath"); // 是XPath索引从1开始
		} catch(e) {
			// TODO
		}
		return node.selectNodes(xpath);
	},

	/**
	 * 得到node的文本内容,node应当是一个只有文本内容的节点
	 * 
	 * @param node
	 */
	getNodeText : function(node) {
		if (!node) return '';
		return node.text || node.textContent || '';
		// return String.trim(node.firstChild.nodeValue);
	},
	/**
	 * 设置node的文本内容,node应当是一个只有文本内容的节点
	 * 
	 * @param node
	 */
	setNodeText : function(node, text) {
		if (!node) return '';
		while(node.childNodes.length > 0) node.removeChild(node.childNodes[0]);	// 清除子节点
		var xml = node.ownerDocument || node;
		var textNode = xml.createTextNode(text);
		return node.appendChild(textNode);
		// return String.trim(node.firstChild.nodeValue);
	},

	/**
	 * 将XML文档或节点转换为XML文本
	 * 
	 * @param xmlDom
	 * @returns
	 */
	toString : function(xmlDom) {
		if (null == xmlDom) return '';
		var xmlString;
		if (window['XMLSerializer']) {
			xmlString = (new XMLSerializer()).serializeToString(xmlDom);
		} else {
			xmlString = xmlDom.xml;
		}
		return xmlString;
	},

	/**
	 * 加载txt字符串为XML对象
	 * 
	 * @param txt XML格式的字符串
	 */
	load : function(txt) {
		var xmlDoc;
		if (window.DOMParser) {
			var parser = new DOMParser();
			xmlDoc = parser.parseFromString(txt, "text/xml");
		} else {// Internet Explorer
			xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async = "false";
			xmlDoc.loadXML(txt);
		}
		return xmlDoc;
	}
};
Helper.add("XMLHelper", XMLHelper);