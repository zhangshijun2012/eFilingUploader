/**
 * 消息显示窗口，用于显示覆盖在body以上的内容，
 * 必须有css的支持，css的名称存放在Message.CLASS_NAME中
 */
var MessagePane = Class.create({
	DEFAULT_OVERLAY_LAYER_ID: "overlayLayer",
	DEFAULT_OVERLAY_FRAME_ID: "overlayFrame",
	DEFAULT_MESSAGE_LAYER_ID: "messageLayer",
	className: {
		overlayLayer: "overlayLayer",
		overlayFrame: "overlayFrame",
		messageLayer: "messageLayer",
		messageLayerHeader: "messageLayerHeader",
		messageLayerHeaderContainer: "messageLayerHeaderContainer",	// 标题栏
		messageLayerHeaderTitle: "messageLayerHeaderTitle",
		messageLayerHeaderClose: "messageLayerHeaderClose shortcutBar",
		messageLayerHeaderTop: "hidden",
		messageLayerBody: "messageLayerBody",
		messageLayerContent: "messageLayerContent",
		messageLayerFooter: "messageLayerFooter",
		messageLayerBottom: "hidden",
		messageLayerButtons: "messageLayerButtons",
		button: "button"
	},
	overlayLayer: false,	/* 覆盖层 */
	overlayFrame: false,	/* 覆盖层中的iframe，用以避免ie6中select无法覆盖的BUG */
	messageLayer: false,	/* 消息显示层 */
	messageLayerHeader: false,	/* 消息头部显示层 */
	messageLayerHeaderContainer: null,
	messageLayerHeaderTitle: false,	/* 消息头部标题显示层 */
	messageLayerHeaderClose: false,	/* 消息头部关闭按钮层 */
	messageLayerHeaderCloseHidden: false,	/* 消息头部关闭按钮层 */
	messageLayerBody: false,	/* 消息文字滚动条层 */
	messageLayerContent: false,	/* 消息文字显示层 */
	messageLayerFooter: false,	/* 底部操作按钮显示层 */
	messageLayerBottom: false,	/* 底部显示层 */
	messageLayerButtons: false,	/* 底部操作按钮显示层 */
	buttons: null,
	destroyAuto: false,			// 隐藏时是否自动销毁
	removeAuto: true,			// 隐藏时是否自动从document中remove
	zIndex: 9000,
	//{					/* 底部操作按钮 */
	//	confirm: false,			/* 确认按钮 */
	//	cancel: false,			/* 取消 */
	//	close: false			/* 关闭 */
	//},
	onShow: function() {	/* 窗口加载后的调用函数 */		
		if (this.options && Object.isFunction(this.options.onShow)) {
			this.options.onShow.apply(this);
		}
	},
	onHide: false,	/* 关闭时的调用函数 */
	STATUS: {
		ERROR: -1,	/* 异常 */
		UNINITIALIZE: null,	/* 尚未初始化 */
		HIDDEN: 0,	/* 已经初始化 */
		VISIBLE: 1	/* 执行成功 */
	},
	disable: function(disable) {
		if (!this.buttons) return;
		for (var button in this.buttons) {
			this.buttons[button].disabled = Object.isUndefined(disable) || !!disable;
		}
	},
	enable: function() {
		this.disable(false);
	},
	
	status: null,
	
	isInitialized: function() {
		return this.status !== this.STATUS.UNINITIALIZE;
	},
	
	isShow: function() {
		return this.isInitialized && this.status == this.STATUS.VISIBLE;
	},
	/* 初始化 */
	initialize: function(messageLayer, overlayLayer, overlayFrame, buttons) {
		this.lastLocation = {
			width: 0, 
			height: 0,
			offsetTop: -1,
			offsetLeft: -1,
			center: false
		};
		
		this.overlayLayer = overlayLayer ? $(overlayLayer) : this.overlayLayer;
		if (!this.overlayLayer) {
			this.createOverlayLayer(overlayLayer, overlayFrame);
		}
		
		this.messageLayer = messageLayer ? $(messageLayer) : this.messageLayer;
		if (!this.messageLayer) {
			this.createMessageLayer(messageLayer, {});
		}
		
		this.status = this.STATUS.HIDDEN;
		var pane = this;
		Event.observe(window, "resize", function() {
			if (pane.isShow()) {
				pane.repaint();
			}
		});
	},
	
	setTitle: function(title) {
		ElementHelper.innerHTML(this.messageLayerHeaderTitle, title);
	},
	
	innerHTML: function(html) {
		ElementHelper.innerHTML(this.messageLayerContent, html);
	},
		
	showOverlayLayer: function() {	/* 显示覆盖层 */
		//var width = document.body.clientWidth;
		//var height = document.body.clientHeight;
		this.overlayLayer.style.top = 0;	// document.body.scrollTop;
		this.overlayLayer.style.left = 0;	// document.body.scrollLeft;
		// 未使用w3c时，需要使用document.documentElement.scrollWidth
		// document.body.clientWidth;
		this.overlayLayer.style.width = Math.max(document.body.scrollWidth, document.documentElement.scrollWidth);	
		this.overlayLayer.style.height = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);
		this.overlayLayer.style.zIndex = this.zIndex;
		Element.show(this.overlayFrame);
		Element.show(this.overlayLayer);
	},
	
	/* 保存最后一次显示位置的参数 */
	lastLocation: null,
	resetMessageLayer: function() {	/* 重新改变大小 */
		this.messageLayer.style.top = 0;	// document.body.scrollTop;
		this.messageLayer.style.left = 0;	// document.body.scrollLeft;
		this.messageLayer.style.width = "75%";
		this.messageLayerBody.style.height = "100%";
		this.messageLayerBody.style.height = "auto";
	},
	
	showMessageLayer: function(width, height, offsetTop, offsetLeft, center) {	/* 显示消息层 */
		// this.messageLayerBody.style.width = "100%";
		// this.messageLayerContent.style.height = "auto";
		
		if (width && !Object.isString(width) && !Object.isNumber(width)) {
			this.lastLocation = width;
			width = this.lastLocation["width"];
			height = this.lastLocation["height"];
			offsetTop = this.lastLocation["offsetTop"];
			offsetLeft = this.lastLocation["offsetLeft"];
			center = this.lastLocation["center"];
		} else {
			this.lastLocation["width"] = width;
			this.lastLocation["height"] = height;
			this.lastLocation["offsetTop"] = offsetTop;
			this.lastLocation["offsetLeft"] = offsetLeft;
			this.lastLocation["center"] = center;
		}
		if (width) {
			this.messageLayer.style.width = width;
		}
		
		this.messageLayer.style.zIndex = this.zIndex;
		Element.show(this.messageLayer);
		var h = height;
		if (height) {
			this.messageLayerBody.style.height = h - this.messageLayerHeader.offsetHeight - this.messageLayerFooter.offsetHeight;
			height = this.messageLayer.offsetHeight;
			while (height > h) {
				this.messageLayerBody.style.height = this.messageLayerBody.offsetHeight - 1;
				height = this.messageLayer.offsetHeight;
			}
		}
		height = this.messageLayer.offsetHeight;
		h = Math.min(document.body.offsetHeight, Math.max(0, this.overlayLayer.offsetHeight));
		//if (h <= 0) {
		//	h = document.body.offsetHeight;
		//}
		var maxHeight = h;
		var maxWidth = Math.min(document.body.offsetWidth, Math.max(0, this.overlayLayer.offsetWidth));
		
		// alert(this.messageLayerHeader.offsetHeight + " " + this.messageLayerFooter.offsetHeight);
		if (height > maxHeight) {
			this.messageLayerBody.style.height = maxHeight - this.messageLayerHeader.offsetHeight - this.messageLayerFooter.offsetHeight;
			height = this.messageLayer.offsetHeight;
			while (height > maxHeight) {
				this.messageLayerBody.style.height = this.messageLayerBody.offsetHeight - 1;
				height = this.messageLayer.offsetHeight;
			}
		}
		
		height = this.messageLayer.offsetHeight;
		
		this.status = this.STATUS.VISIBLE;
				
		var proportion = center ? 0.5 : (1 - 0.618);	//0.618为黄金比例的前3位
		
		//var o = this.overlayLayer ? this.overlayLayer : document.body;
		var offsets = ElementHelper.getOffset(this.overlayLayer);
		var top = offsets.top;
		var left = offsets.left;
		
		offsetTop = ((!Object.isNumber(offsetTop) && new String(offsetTop).parseInt() <= 0) || 
						(Object.isNumber(offsetTop) && offsetTop < 0 )) ? 
						Math.max(0, (maxHeight - height) * proportion) : 
						new String(offsetTop).parseInt();
		offsetTop = top + offsetTop;
		this.messageLayer.style.top = offsetTop;
		width = this.messageLayer.offsetWidth;
		offsetLeft = ((!Object.isNumber(offsetLeft) && new String(offsetLeft).parseInt() <= 0) || 
						(Object.isNumber(offsetLeft) && offsetLeft < 0)) ? 
						Math.max(0, (this.overlayLayer.clientWidth - width) * proportion) : 
						new String(offsetLeft).parseInt();
		offsetLeft = left + offsetLeft;
		this.messageLayer.style.left = offsetLeft;
		
		var dom = this.messageLayerBody;
		if (dom.scrollHeight > dom.clientHeight && height < maxHeight) {
			//alert(dom.scrollHeight + " " + dom.clientHeight + " " + dom.offsetHeight);
			//alert(dom.scrollWidth + " " + dom.clientWidth + " " + dom.offsetWidth);
			// 出现了滚动条
			this.lastLocation["height"] = height + dom.scrollHeight - dom.clientHeight;
			this.repaint();
		} else if (dom.scrollWidth > dom.clientWidth && this.messageLayer.offsetWidth < maxWidth) {
			this.lastLocation["width"] = Math.min(maxWidth, this.messageLayer.offsetWidth + dom.scrollWidth - dom.clientWidth);
			this.repaint();
		}
	},
	
	/**
	 * message: 消息内容
	 * title: 标题
	 * width, height, offsetTop, offsetLeft: 分别为宽、高、上边距、左边距
	 * buttons: 按钮
	 * center: 是否居中,offsetTop和offsetLeft<0时有效
	 * showHTML: 是否显示html标签，如为false则仅显示文字
	 */
	show: function(message, title, options) {	/* 显示消息框 */
		this.options = this.options || { };
		Object.extend(this.options, options || { });
		
		if (!this.isInitialized()) {
			this.initialize();
		} else if (this.removed && this.messageLayer) {
			document.body.appendChild(this.overlayLayer);
			document.body.appendChild(this.messageLayer);
			this.removed = false;
		}
		
		this.zIndex = options ? NumberHelper.intValue(options.zIndex, 9000) : 9000;
		if (Object.isUndefined(message)) {
			Element.show(this.messageLayer);
			Element.show(this.overlayLayer);
			Element.show(this.overlayFrame);
			this.status = this.STATUS.VISIBLE;
			if (Object.isFunction(this.onShow)) {
				try {
					this.onShow();
				} catch (error) {
				}
			}
			return;
		}
		
		
		//this.showOverlayLayer();
		//this.showMessageLayer();	// 防止IE中的BUG,将层显示出来之后再操作.对显示层进行两次显示操作
		//this.resetMessageLayer();
		
		// width, height, offsetTop, offsetLeft, buttons, center, showHTML
		
		if (title || title === "") {
			this.messageLayerHeaderTitle.innerHTML = title;
		}
		
		options = options || { };
		this.destroyAuto = Object.isUndefined(options.destroyAuto) ? this.destroyAuto : options.destroyAuto;
		this.removeAuto = Object.isUndefined(options.removeAuto) ? this.removeAuto : options.removeAuto;
		
		if (message || message === "") {
			if (Object.isUndefined(options.showHTML) || options.showHTML) {
				// this.messageLayerContent.innerHTML = message;
				ElementHelper.innerHTML(this.messageLayerContent, message);
			} else {
				ElementHelper.setInnerText(this.messageLayerContent, message);
			}
		}
		
		
		this.showOverlayLayer();
		if (options.buttons !== false) {
			this.createButtons(options.buttons);
		}
		// 重新对显示层定位
		this.showMessageLayer(options.width, options.height, options.offsetTop, options.offsetLeft, options.center);	
		
		
		if (this.maximum) {
			this.maximum = false;
			var e = Selector.findChildElements(this.messageLayerHeaderClose, ".normalization");
			e = e ? e[0] : null;
			if (e) Element.setClassName(e, "maximize");
		}
		if (Object.isFunction(this.onShow)) {
			try {
				this.onShow();
			} catch (error) {
			}
		}
	},
	
	hide: function() {
		Element.hide(this.messageLayer);
		//Element.hide(this.messageLayerHeader);
		//Element.hide(this.messageLayerHeaderTitle);
		//Element.hide(this.messageLayerBody);
		//Element.hide(this.messageLayerContent);
		//Element.hide(this.messageLayerFooter);
	
		Element.hide(this.overlayLayer);
		// Element.hide(this.overlayFrame);
		this.status = this.STATUS.HIDDEN;

		if (Object.isFunction(this['onHideOnce'])) {
			try {
				this.onHideOnce();
			} catch (error) {
			}
			this.onHideOnce = null;
		}
		if (Object.isFunction(this.onHide)) {
			try {
				this.onHide();
			} catch (error) {
			}
		}
		
		if (this.destroyAuto) {
			this.destroy();
		} else if (this.removeAuto) {
			document.body.removeChild(this.overlayLayer);
			document.body.removeChild(this.messageLayer);
			this.removed = true;
		}
	},
	
	/* 销毁对象 */
	destroy: function() {
		document.body.removeChild(this.overlayLayer);
		document.body.removeChild(this.messageLayer);
		this.overlayLayer = null;
		this.overlayFrame = null;
		this.messageLayer = null;
		this.messageLayerHeader = null;
		this.messageLayerHeaderTitle = null;
		this.messageLayerHeaderClose = null;
		this.messageLayerHeaderCloseHidden = null;
		this.messageLayerBody = null;
		this.messageLayerContent = null;
		this.messageLayerFooter = null;
		this.messageLayerBottom = null;
		this.messageLayerButtons = null;
		this.buttons = null;
		this.status = this.STATUS.UNINITIALIZE;
		if (Object.isFunction(this.onDestory)) {
			try {
				this.onDestroy();
			} catch (error) {
			}
		}
	},
	
	setMessageLayerHeaderCloseHidden: function(hidden) {
		this.messageLayerHeaderClose = hidden;
		if (this.messageLayerHeaderCloseHidden) {
			Element.hide(this.messageLayerHeaderClose);
		}
	},
	createMessageLayer: function(messageLayerId, buttons) {
		messageLayerId = messageLayerId ? messageLayerId : this.DEFAULT_MESSAGE_LAYER_ID + Math.random();
		if (this.messageLayer) {
			/* 已经创建 */
			return 0;
		} else if ($(messageLayerId)) {
			/* id号重复 */
			return -1;
		}
		
		this.messageLayer = document.createElement("div");
		this.messageLayer.id = messageLayerId;
		Element.setClassName(this.messageLayer, this.className.messageLayer);
		Element.hide(this.messageLayer);
		document.body.appendChild(this.messageLayer);
		
		
		this.messageLayerHeader = document.createElement("div");
		Element.setClassName(this.messageLayerHeader, this.className.messageLayerHeader);
		this.messageLayer.appendChild(this.messageLayerHeader);
		

		// 拖动
		Event.observe(this.messageLayerHeader, "mousedown", (function() {
			if (Object.isUndefined(this.moveable)) {
				Event.observe(document, "mousemove", (function() {
					if (!this.moveable) return;
					var lastPointer = this.pointer;
					var evt = EventHelper.get();
					this.pointer = evt.pointer();
					//$("header").innerHTML = "{" + this.pointer.x + "," + this.pointer.y + "}";
					var x = this.pointer.x - lastPointer.x;
					var y = this.pointer.y - lastPointer.y;
					if (x == 0 && y == 0) return;
					this.messageLayer.style.top = this.messageLayer.offsetTop + y;
					this.messageLayer.style.left = this.messageLayer.offsetLeft + x;
					
				}).bind(this));
			
				Event.observe(document, "mouseup", (function() {
					if (!this.moveable) return;
					//var evt = Event.get();
					// this.pointer = evt.pointer();
					this.moveable = false;
					this.messageLayerHeader.style.cursor = "auto";
				}).bind(this));
			}
			if (this.moveable) return;
			this.moveable = true;
			this.messageLayerHeader.style.cursor = "move";
			var evt = EventHelper.get();
			this.pointer = evt.pointer();
		}).bind(this));
		
		
		this.messageLayerHeaderContainer = ElementHelper.createElement("div", { 
			className: this.className.messageLayerHeaderContainer }, this.messageLayerHeader);
		
		
		this.messageLayerHeaderTitle = ElementHelper.createElement("span", { 
			className: this.className.messageLayerHeaderTitle, innerHTML: "标题" }, this.messageLayerHeaderContainer);
			
		this.messageLayerHeaderClose = ElementHelper.createElement("span", {
			className: this.className.messageLayerHeaderClose }, this.messageLayerHeaderContainer);
			
		
		var ul = ElementHelper.createElement("ul", null, this.messageLayerHeaderClose);
		
		/*
		var li = ElementHelper.createElement("li", { className: "printButton" }, ul);
		var printButton = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "打印" }, li);
		Event.observe(printButton, "click", (function() { Main.print(true, this); }).bind(this));
		*/
		
		var li = ElementHelper.createElement("li", { className: "maximize" }, ul);
		var toggleButton = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "切换:最大化/还原" }, li);
		Event.observe(toggleButton, "click", (function() { this.toggle(); }).bind(this));
		
		li = ElementHelper.createElement("li", { className: "closeButton" }, ul);
		var closeButton = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "关闭窗口" }, li);
		Event.observe(closeButton, "click", (function() { this.hide(); }).bind(this));
			
		this.setMessageLayerHeaderCloseHidden(this.messageLayerHeaderClose);
		
		this.messageLayerHeaderTop = ElementHelper.createElement("div", { className: this.className.messageLayerHeaderTop }, this.messageLayerHeader);
		Element.hide(this.messageLayerHeaderTop);	
		
		this.messageLayerBody = document.createElement("div");
		Element.setClassName(this.messageLayerBody, this.className.messageLayerBody);
		this.messageLayer.appendChild(this.messageLayerBody);
		
		this.messageLayerContent = document.createElement("div");
		Element.setClassName(this.messageLayerContent, this.className.messageLayerContent);
		this.messageLayerBody.appendChild(this.messageLayerContent);
		
		
		this.messageLayerFooter = document.createElement("div");
		Element.setClassName(this.messageLayerFooter, this.className.messageLayerFooter);		
		
		this.messageLayerBottom = ElementHelper.createElement("div", { className: this.className.messageLayerBottom }, this.messageLayerFooter);
		Element.hide(this.messageLayerBottom);
		
		this.messageLayerButtons = document.createElement("div");
		Element.setClassName(this.messageLayerButtons, this.className.messageLayerButtons);	
		this.messageLayerFooter.appendChild(this.messageLayerButtons);
		
		this.createButtons(buttons);
		this.messageLayer.appendChild(this.messageLayerFooter);
		
		return 1;
	},
	
	maximum: false,			// 当前是否最大化
	cacheLocation: null,	// 用于最大化/最小化的切换是保存原来的位置
	/**
	 * 窗口最大化/最小化
	 */
	toggle: function() {
		if(!this.maximum) {
			// 当前未最大化
			this.cacheLocation = Object.clone(this.lastLocation);
			//var clientWidth = document.body.clientWidth || document.documentElement.clientWidth;
			//var clientHeight = document.body.clientHeight || document.documentElement.clientHeight;
			this.lastLocation = {
				offsetTop: 0,
				offsetLeft: 0,
				width: document.body.clientWidth || document.documentElement.clientWidth,
				height: document.body.clientHeight || document.documentElement.clientHeight
				//center: true
			}
		} else {
			this.lastLocation = this.cacheLocation;
		}
		
		var e = Selector.findChildElements(this.messageLayerHeaderClose, this.maximum ? ".normalization" : ".maximize");
		e = e ? e[0] : EventHelper.getElement();
		this.maximum = !this.maximum;
		if (e) Element.setClassName(e, this.maximum ? "normalization" : "maximize");
		this.repaint();
	},
	
	
	/* 删除按钮，按钮可以是一个json对象，或者字符串，或者一个按钮对象。缺省则隐藏所有按钮 */
	deleteButtons: function(buttons) {
		if (!this.messageLayerButtons || !this.buttons) {
			return;
		}
		for (var p in this.buttons) {
			if (this.buttons[p]) {
				try {
					this.messageLayerButtons.removeChild(this.buttons[p]);
					this.buttons[p] = null;
				} catch (error) {
					// alert(error.message);
				}
			}
		}
	},
	
	
	/* 隐藏按钮，按钮可以是一个json对象，或者字符串，或者一个按钮对象。缺省则隐藏所有按钮 */
	hiddenButtons: function(buttons) {
		if (!this.buttons) {
			return;
		}
		for (var p in this.buttons) {
			if (this.buttons[p] && (Object.isUndefined(buttons) || (Object.isString(buttons) && p == buttons) 
				|| (buttons == this.buttons[p]) || buttons[p])) {
				Element.hide(this.buttons[p]);
			}
		}
	},
	
	DEFAULT_BUTTONS: function(buttons) {
		return {
			confirm: {
				text: Language.get('global.confirm'),
				show: Object.isString(buttons) ? buttons.charAt(0) == '1' : true,
				clearHandler: true,
				handler: function() {
					this.hide();
				}
			},
			cancel: {
				text: Language.get('global.cancle'),
				show: Object.isString(buttons) ? buttons.charAt(1) == '1' : true,
				clearHandler: true,
				handler: function() {
					this.hide();
				}
			},
			close: {
				text: Language.get('global.close'),
				show: Object.isString(buttons) ? buttons.charAt(2) == '1' : true,
				clearHandler: true,
				handler: function() {
					this.hide();
				}
			}
		};
	},
	/* 创建按钮 */
	createButtons: function(buttons) {
		if (!this.messageLayerButtons) {
			return;
		}
		buttons = Object.isUndefined(buttons) || !buttons ? "100" : buttons;
		buttons = Object.isString(buttons) || Object.isNumber(buttons) ? new String(buttons).trim() + "000" : buttons;
		buttons = buttons && !Object.isString(buttons) ? buttons : this.DEFAULT_BUTTONS(buttons);
		if (!this.buttons) {
			this.buttons = { };
		}
		var showFooter = false;
		var buttonText = "";	//(buttons && buttons.confirm.text) ? new String(buttons.confirm.text).trim() : "确定";
		this.deleteButtons();
		
		for (var p in buttons) {
			buttonText = (buttons && buttons[p].text) ? new String(buttons[p].text).trim() : p;
			buttonHidden = !(buttons && (Object.isUndefined(buttons[p].show) || buttons[p].show));
			buttonDisabled = !!buttons[p].disabled;
			
			if (!this.buttons[p]) {
				this.buttons[p] = document.createElement("input");
				this.buttons[p].type = buttons[p].type ? type : "button";
				this.messageLayerButtons.appendChild(this.buttons[p]);
			}
			
			if (buttons[p].title) this.buttons[p].title = buttons[p].title;
			
			this.buttons[p].disabled = buttonDisabled;
			this.buttons[p].value = buttonText;
			Element.setClassName(this.buttons[p], this.className.button);
			if (buttonHidden) {
				Element.hide(this.buttons[p]);
			} else {
				showFooter = true;
			}
			
			if (buttons[p].className) {
				Element.addClassName(this.buttons[p], buttons[p].className);
			}
			
			
			if (Object.isUndefined(buttons[p].clearHandler) 
				|| buttons[p].clearHandler === true 
				|| buttons[p].clearHandler === 1) {	//默认或者true删除所有事件
				Event.stopObserving(this.buttons[p]);
			} else if (buttons[p].clearHandler) {
				for (var e in buttons[p].clearHandler) {
					/**
					 * clearHandle里面可以自定要删除的事件，如
					 * {
					 *		click: function() {},
					 *		blur: function() {},
					 * }
					 */
					Event.stopObserving(this.buttons[p], e, buttons[p].clearHandler[e]);
				}
			}
			if (Object.isFunction(buttons[p].handler)) {
				Event.observe(this.buttons[p], "click", buttons[p].handler.bind(this));	// 绑定到this中去
			} else if (buttons[p].handler) {
				/**
				 * handle里面可以自定其他事件，如
				 * {
				 *		click: function() {},
				 *		blur: function() {},
				 * }
				 */
				for (var h in buttons[p].handler) {
					Event.observe(this.buttons[p], h, buttons[p].handler[h].bind(this));
				}
			}
		}
		
		if (showFooter) {
			Element.show(this.messageLayerButtons);
		} else {
			Element.hide(this.messageLayerButtons);
		}
	},
	
	createOverlayLayer: function(overlayLayerId, overlayFrameId) {
		overlayLayerId = overlayLayerId ? overlayLayerId : this.DEFAULT_OVERLAY_LAYER_ID + Math.random();
		overlayFrameId = overlayFrameId ? overlayFrameId : this.DEFAULT_OVERLAY_FRAME_ID + Math.random();
		if (this.overlayLayer) {
			/* 已经创建 */
			return 0;
		} else if ($(overlayLayerId)) {
			/* id号重复 */
			return -1;
		} else if ($(overlayFrameId)) {
			return -2;
		}
		this.overlayLayer = document.createElement("div");
		this.overlayLayer.id = overlayLayerId;
		Element.setClassName(this.overlayLayer, this.className.overlayLayer);
		Element.hide(this.overlayLayer);
		document.body.appendChild(this.overlayLayer);
		
		/*
		this.overlayFrame = document.createElement("iframe");
		this.overlayFrame.id = overlayFrameId;
		this.overlayFrame.name = overlayFrameId;
		Element.setClassName(this.overlayFrame, this.className.overlayFrame);
		//this.overlayFrame.style.frameborder = 0;
		this.overlayFrame.setAttribute("frameborder","0"); 
		this.overlayFrame.style.width = "100%";
		this.overlayFrame.style.height = "100%";
		this.overlayLayer.appendChild(this.overlayFrame);
		*/
		return 1;
	},
	
	/**
	 * 得到url的内容并显示
	 * url: 地址
	 * options: 请参见AjaxHelper.send
	 * 其余参数请参见this.show
	 */
	open: function(url, options, title, messageOptions) {
		var _options = { };
		Object.extend(_options, options = (options || { }));
		var onSuccess = options.onSuccess;
		var pane = this;
		_options.onSuccess = function(response, json) {
			pane.show(response.responseText, title, messageOptions);
			if (Object.isFunction(onSuccess)) {
				onSuccess(response, json);
			}
		};
		
		AjaxHelper.send(url, _options);
	},
	
	/* 更改当前的显示层大小、位置 */
	setLocation: function(width, height, offsetTop, offsetLeft) {
		if (width && !Object.isString(width) && !Object.isNumber(width)) {
			this.lastLocation = width;
		} else {
			this.lastLocation["width"] = width;
			this.lastLocation["height"] = height;
			this.lastLocation["offsetTop"] = offsetTop;
			this.lastLocation["offsetLeft"] = offsetLeft;
		}
		
		this.repaint(this.lastLocation);
	},
	
	repaint: function(callOnShow) {
		if (this.isInitialized()) {
			this.showOverlayLayer();
			this.resetMessageLayer();
			this.showMessageLayer(this.lastLocation);
			
			if (callOnShow && Object.isFunction(this.onShow)) {
				try {
					this.onShow();
				} catch (error) {
				}
			}
		}
	},
	
	paint: function() {
		if (this.isShow()) this.repaint();
	}
});

/* 主要用于查询分页显示的面板 */
var PagingPane = Class.create(MessagePane, {
	options: null,
	initialize: function($super, url, options, messageLayer, overlayLayer, overlayFrame, buttons) {
		this.className.messageLayerHeaderTop = "messageLayerHeaderTop";
		this.className.messageLayerBottom = "paging";
		this.url = url;
		this.options = options ? Helper.clone(options) : { },	// 里面最好定义表单要提交的action
		
		//this.showResultsOptions = Helper.clone(this.showResultsOptions);
		this.showResultsOptions = {
		/* 显示数据的参数 */
		//listContainer: listContainer,	// 显示列表的容器
			attributes: {
				table: {
					id: "listGrid",
					className: "jtable"
				},
				thead: {
					attributes: {
					},
					tr: {
					},
					td: {
					},
					numberTd: {
						style: {
							width: "3%"
						}
					},
					selectorTd: {
						style: {
							width: "3%"
						},
						className: "selector"
					}
				},
				tbody: { },
				tr: { },
				td: { },
				numberTd: {
					className: "number"
				},
				selectorTd: {
					className: "txtCenter selector"
				},
				nothing: {
					style: {
						height: "30"
					},
					className: "txtCenter red",
					innerHTML: "没有符合条件的数据,请更改查询条件之后再试!"
				}
			},
			number: false,					// 是否显示行号
			selector: false,				// 是否显示复选框
			events: {
				/*"0": function(id, e) {		// 第0列的事件.第一个参数为id,第二个为触发事件的元素
					Main.service.view(id);
				}*/
			},
			renderer: {
				// 可指定渲染每列的方法
			}
		};
		
		$super(messageLayer, overlayLayer, overlayFrame, buttons);
		
		var html = "<form><div id=\"pagingOperation\">"
				+ "	<a href=\"#\">全选</a>"
				+ "	<a href=\"#\">不选</a>"
				+ "	<a href=\"#\">反选</a>"
				+ "</div>"
				+ "<!-- 显示分页 -->"
				+ "<div id=\"pagingLefter\">"
				+ "	<!-- 左侧页码显示区域 -->"
				+ "	总记录:"
				+ "	<span id=\"size\" class=\"b\">0</span>&nbsp; 本页:"
				+ "	<span id=\"currentPageSize\" class=\"b\">0</span>&nbsp; 每页:"
				+ "	<input name=\"pageSize\" id=\"pageSize\" value=\"\" />"
				+ "&nbsp; 共&nbsp;<span id=\"pages\" class=\"b\">0</span>&nbsp;页&nbsp;"
				+ "</div>"
				+ "<div id=\"pagingRighter\">"
				+ "	<!-- 右侧显示区域 -->"
				+ "	<span id=\"pagingPages\"></span>&nbsp; 转到:"
				+ "	<input name=\"page\" id=\"page\" value=\"\" />"
				+ "	<input type=\"submit\" id=\"pagingGo\" value=\"GO\" onclick=\"return false;\" />"
				+ "</div><form>";
		this.messageLayerBottom.innerHTML = html;
		
		if (this.options.title) this.setTitle(this.options.title);
		if (this.options.condition) this.setCondition(this.options.condition);
		
		var e = Selector.findChildElements(this.messageLayerBottom, "#pagingGo")[0];
		var me = this;
		Event.observe(e, "click", function() { me.go(); return false; });
		var onShow = this.onShow;
		
		//if (Object.isFunction(this.options.onShow)) {
		//	onShow = this.options.onShow;
		//}
		var me = this;
		this.onShow = (function() {	if (Object.isFunction(onShow)) { onShow.apply(me); }; this.query(); this.onShow = onShow; }).bind(this);
	},
	
	setCondition: function(html) {
		ElementHelper.innerHTML(this.messageLayerHeaderTop, html);
		if (this.isShow()) this.repaint();
	},
	
	onShow: function() {
		if (StringHelper.isEmpty(this.messageLayerHeaderTop.innerHTML)) {
			Element.hide(this.messageLayerHeaderTop);
		} else {
			Element.show(this.messageLayerHeaderTop);
		}
		
		Element.show(this.messageLayerBottom);
		
		if (this.options && Object.isFunction(this.options.onShow)) {
			this.options.onShow.apply(this);
		}
	},
	
	validate: function() {
		var e = Selector.findChildElements(this.messageLayerBottom, "#page")[0];
		var page = e.value;
		if (page && !NumberHelper.isNumber(page)) {
			alert("请输入正确的 页数!");
			e.focus();
			return false;
		}
		e.value = NumberHelper.intValue(page);
		
		e = Selector.findChildElements(this.messageLayerBottom, "#pageSize")[0];
		var pageSize = e.value;
		if (pageSize && !NumberHelper.isNumber(pageSize)) {
			alert("请输入正确的 每页数量!");
			e.focus();
			return false;
		}
		e.value = NumberHelper.intValue(pageSize);
		
		return true;
	},
	onQuery: function(response, headerJSON) {
		var text = StringHelper.trim(response.responseText);
		var json = StringHelper.evalJSON(text);
		//var container = this.messageLayer;
		if (!json) {
			// 可能是系统出错
			ElementHelper.innerHTML(this.messageLayerContent, "<center class=\"red marginTop\">" + text + "<center>");
			return;
		}
		this.showPaging(json.page);		// 显示分页信息
		this.showResults(json.results);	// 显示结果数据
		
		// this.fireEvent("afterQuery", response, target, options, headerJSON);
	},
	
	query: function(page, pageSize) {
		if (page || pageSize) {
			return this.go(page, pageSize);
		}
		if (!this.validate()) return;
		var form = Selector.findChildElements(this.messageLayerHeaderTop, "form");
		form = form ? form[0] : null;
		
		var url = form ? form[0].action : "";
		url = StringHelper.isEmpty(url) ? (this.options.url || this.url) : url;
		if (!url) return;
		if (Object.isFunction(this.options.onBeforeQuery)) {
			this.options.onBeforeQuery.apply(this, [form, url]);
		}
		
		var me = this;
		var _options = {
			parameters:  (form ? Form.serialize(form) + "&" : "") // 查询条件
				+ Form.serialize(Selector.findChildElements(this.messageLayerBottom, "form")[0]), // 分页信息
			onSuccess: function(response, headerJSON) {
				/* 可以在子类中实现此方法 */
				if (Object.isFunction(me.onQuery)) {
					me.onQuery(response, headerJSON);
				}
			}
		};
		
		Object.extend(_options, this.options.query || { });
		
		//var form = Selector.findChildElements(this.messageLayer, "form");
		AjaxHelper.send(url, _options);
		
	},
	
	go: function(page, pageSize) {
		if (!Object.isUndefined(page)) {
			var e = Selector.findChildElements(this.messageLayerBottom, "#page")[0];
			e.value = page;
		}
		
		if (!Object.isUndefined(pageSize)) {
			var e = Selector.findChildElements(this.messageLayerBottom, "#pageSize")[0];
			e.value = pageSize;
		}
		
		this.query();
	},
	
	/* 显示分页信息 */
	showPaging: function(page) {
		if (!page) return -1;
		var container = this.messageLayerBottom;
		if (!container) {
			return -2;
		}
		
		if (page.size <= 0) {
			// 没有任何信息
			// ElementHelper.innerHTML(this.listContainer, "<center class='red margintop'>没有查询到符合条件的数据,请更改查询条件后重试!</center>");
			return 0;
		}
		var paging = this.messageLayerBottom;
		ElementHelper.innerHTML(Selector.findChildElements(paging, "#size")[0], page.size);
		ElementHelper.innerHTML(Selector.findChildElements(paging, "#currentPageSize")[0], page.currentPageSize);
		ElementHelper.innerHTML(Selector.findChildElements(paging, "#pages")[0], page.pages);
		var e;
		if (NumberHelper.intValue((e =Selector.findChildElements(paging, "#pageSize")[0]).value) <= 0) {
			e.value = page.pageSize;
		}
		
		if (NumberHelper.intValue((e = Selector.findChildElements(paging, "#page")[0]).value) <= 0) {
			e.value = page.page;
		}
		var e = Selector.findChildElements(paging, "#pagingPages")[0];
		e.innerHTML = "";
		var a;
		
		if (page.currentPage <= 1) {
			ElementHelper.createTextNode("首页", e);
			ElementHelper.createTextNode(" ", e);
			ElementHelper.createTextNode("上一页", e);
		} else {
			a = ElementHelper.createElement("a", { innerHTML: "首页", href: "javascript: void(0)" }, e);
			Event.observe(a,"click", (function(){ this.go(1, page.pageSize); }).bind(this));
			ElementHelper.createTextNode(" ", e);
			a = ElementHelper.createElement("a", { innerHTML: "上一页", href: "javascript: void(0)" }, e);
			Event.observe(a,"click", (function(){ this.go(page.page - 1, page.pageSize); }).bind(this));
		}
		
		ElementHelper.createTextNode(" ", e);
		for (var i = Math.max(1, page.currentPage - 5), l = Math.min(page.pages, i + 9); i <= l; i++) {
			if (i == page.currentPage) {
				a = ElementHelper.createElement("span", { id: "currentPage", innerHTML: i }, e);
			} else {
				a = ElementHelper.createElement("a", { innerHTML: i, href: "javascript: void(0)" }, e);
				
				Event.observe(a,"click", (function(p){ this.go(p, page.pageSize); }).bind(this, i));
			
			}
			ElementHelper.createTextNode(" ", e);
		}
		
		if (page.currentPage >= page.pages) {
			ElementHelper.createTextNode("下一页", e);
			ElementHelper.createTextNode(" ", e);
			ElementHelper.createTextNode("尾页", e);
		} else {
			a = ElementHelper.createElement("a", { innerHTML: "下一页", href: "javascript: void(0)" }, e);
			Event.observe(a,"click", (function(){ this.go(page.page + 1, page.pageSize); }).bind(this));
			ElementHelper.createTextNode(" ", e);
			a = ElementHelper.createElement("a", { innerHTML: "尾页", href: "javascript: void(0)" }, e);
			Event.observe(a,"click", (function(){ this.go(page.pages, page.pageSize); }).bind(this));
		}
		
		return true;
	},
	
	
	/* 表头 */
	showResultsHead: null,
	
	/* 显示数据的参数 */
	showResultsOptions: null,
	
	/* 显示数据 */
	showResults: function(results) {
		var _options = this.showResultsOptions || { };
		//alert(this.showResultsOptions.toSource());
		Helper.extend(_options, this.options.showResultsOptions || { });
		//alert(_options.toSource());
		
		var container = this.messageLayerContent;
		var table;
		while ((table = container.firstChild) && (!table.tagName || table.tagName.toLowerCase() != "table")) {
			// 用于除去前面的文本节点.主要在当后台系统出错的情况
			container.removeChild(table);
		}
		// var table = e;//Selector.findChildElements(container, ["table"]);
		// table = table ? table[0] : null;
		// var thead = table ? table.tHead : null;
		var thead, tr, td;
		if (!table) {
			table = ElementHelper.createElement("table", _options.attributes.table, container);
			thead = table.tHead;
			if (!thead) {
				var head = this.options.showResultsHead || this.showResultsHead;
				if (head) {	// 创建表头
					var thead = ElementHelper.createElement("thead", _options.attributes.thead.attributes, table);
					tr = ElementHelper.createElement("tr", _options.attributes.thead.tr, thead);
					if (_options.number) {	// 显示行号
						ElementHelper.createElement("th", _options.attributes.thead.numberTd, tr);
					}
					if (_options.selector) {	// 显示复选框
						td = ElementHelper.createElement("th", _options.attributes.thead.selectorTd, tr);
						td.innerHTML = "<input name=\"checkAllOrNone\" id=\"checkAllOrNone\" type=\"checkbox\" "
								+ "class=\"checkbox\" onclick=\"Main.service.setChecked(this.checked)\" />";
					}
					
					for (var j = 0, l = head.length; j < l; j++) {
						td = ElementHelper.createElement("th", head[j].attributes || _options.attributes.thead.td[j], tr);
						
						if (head[j].value) {
							td.innerHTML = head[j].value;
						}
						if (head[j].width) {
							td.style.width = head[j].width;
						}
					}
				}
			}
		} else {
			thead = table.tHead;
		}
		
		var tBodies = $A(table.tBodies);
		for (var i = 0, l = tBodies.length; i < l; i++) {
			table.removeChild(tBodies[i]);
		}
				
		var tbody = ElementHelper.createElement("tbody", _options.attributes.tbody);
		if (thead && thead.nextSibling) table.insertBefore(tbody, thead.nextSibling);
		else table.appendChild(tbody);
		
		var length = results ? results.length : 0;
		if (length <= 0) {
			// 没有任何数据
			tr = ElementHelper.createElement("tr", _options.attributes.tr, tbody);
			td = ElementHelper.createElement("td", _options.attributes.nothing, tr);
			if (thead && thead.rows[0]) {
				td.colSpan = thead.rows[0].cells.length;
			}
			return 0;
		}
		var result, data;
		var events = _options.events || { };
						
		var cols = thead && thead.rows[0] ? thead.rows[0].cells.length : 0;
		if (_options.number) cols--;	// 显示行号
		if (_options.selector) cols--;	// 显示行号
		var me = this;
		for (var i = 0; i < length; i++) {	// 创建表头
			result = results[i];
			tr = ElementHelper.createElement("tr", _options.attributes.tr, tbody);
			if (_options.number) {	// 显示行号
				td = ElementHelper.createElement("td", _options.attributes.numberTd, tr);
				td.innerHTML = (i + 1);
			}
			if (_options.selector) {	// 显示复选框
				td = ElementHelper.createElement("td", _options.attributes.selectorTd, tr);
				td.innerHTML = "<input name=\"ids\" id=\"ids\" type=\"checkbox\" value=\"" + result.id + "\" class=\"checkbox\" />";
			}
			data = result.data;
			for (var j = 0, l = cols > 0 ? cols : data.length; j < l; j++) {
				td = ElementHelper.createElement("td", _options.attributes.td[j], tr);
				if (events[j] && Object.isFunction(events[j])) {
					var a = ElementHelper.createElement("a", { href: "#", innerHTML: data[j] }, td);
					// 使用闭包防止数据无法传递
					//Event.observe(a, "click", (function(p, id, e) { return (function() { events[p](id, e); }) })(j, result.id, a));
					Event.observe(a, "click", (function(evt, id, e) { evt.apply(this, [id, e]); return false; }).bind(this, events[j], result.id, a));
				} else {
					td.innerHTML = data[j];
				}
			}
		}
		
		var noSortIndex = [];
		if (_options.selector) {
			this.initializeCheckBox();
			noSortIndex = [_options.number ? 1 : 0];
			Element.show(Selector.findChildElements(this.messageLayerBottom, "#pagingOperation")[0]);
		} else {
			Element.hide(Selector.findChildElements(this.messageLayerBottom, "#pagingOperation")[0]);
		}
		
		if (thead) {
			GridHelper.initialize(thead, tbody, { noSortIndex: noSortIndex });
		}
		
		this.repaint();
	},
	
	
	
	/**
	 * 复选框改变时的事件
	 */
	onCheckBoxChange: function(forms, elements, operationCheckBox) {
		forms = forms ? forms : Selector.findChildElements(this.messageLayer, "form");
		if (!forms) return;
		elements = elements ? elements : "ids";
		operationCheckBox = operationCheckBox ? operationCheckBox : Selector.findChildElements(this.messageLayer, "checkAllOrNone");
		operationCheckBox = operationCheckBox ? ((Object.isString(operationCheckBox) || Object.isElement(operationCheckBox)) ? [operationCheckBox] : $A(operationCheckBox)) : [];
		var i = operationCheckBox.length;
		var count = CheckBoxHelper.getCheckedCount(forms, elements);
		while(i-- > 0) {
			if ($(operationCheckBox[i])) {
				$(operationCheckBox[i]).checked = (count > 0 && CheckBoxHelper.isCheckedAll(forms, elements));	// 全选框
			}
		}
		// $("toolbarLefter").innerHTML = (count <= 0 ? "&nbsp;" : ("当前已选中&nbsp;<b>" + count + "</b>&nbsp;条记录"));
		// Form.Element[count > 0 ? "enable" : "disable"](this.operations["remove"]);
	},
	
	/**
	 * 初始化复选框的事件.会触发onCheckBoxChange
	 */
	initializeCheckBox: function(forms, elements, operationCheckBox) {
		forms = forms ? forms : Selector.findChildElements(this.messageLayer, "form");
		if (!forms) return;
		elements = elements ? elements : "ids";
		//operationCheckBox = operationCheckBox ? operationCheckBox : "checkAllOrNone";
		//operationCheckBox = (Object.isString(operationCheckBox) || Object.isElement(operationCheckBox)) ?
		// [operationCheckBox] : $A(operationCheckBox);
		this.onCheckBoxChange(forms, elements, operationCheckBox);
		var service = this;
		CheckBoxHelper.observe(forms, elements, function() { service.onCheckBoxChange(forms, elements, operationCheckBox); });
	},
	
	/**
	 * 设定选中状态,会触发onCheckBoxChange
	 */
	setChecked: function(check, forms, elements, onSuccess) {
		forms = forms ? forms : Selector.findChildElements(this.messageLayer, "form");
		if (!forms) return;
		elements = elements ? elements : "ids";
		var service = this;
		var _onSuccess = function() {
			service.onCheckBoxChange(forms, elements);
			if (Object.isFunction(onSuccess)) {
				onSuccess();
			}
		}
		
		CheckBoxHelper.setChecked(check, forms, elements, _onSuccess);
	},
	
	/**
	 * 全选
	 */
	check: function(forms, elements, onSuccess) {
		this.setChecked(true, forms, elements, onSuccess);
	},
	/**
	 * 全不选
	 */
	clear: function(forms, elements, onSuccess) {
		// elements = elements ? elements : ["ids", "checkAllOrNone"];
		this.setChecked(false, forms, elements, onSuccess);
	},
	/**
	 * 反选
	 */
	turn: function(forms, elements, onSuccess, operationCheckBox) {
		this.setChecked(-1, forms, elements, onSuccess);
	}
});

var Message, Dialog;
/**
 * 临时的弹出窗口管理器
 */
var TempDialogManager = {
	index: 0,
	dialogs: [],			// 正在使用的窗口
	destroiedDialogs: [],	// 已回销的窗口
	createDialog: function() {
		if (this.destroiedDialogs.length > 0) return this.destroiedDialogs.shift();
		var dialog = new MessagePane('tempDialog' + (this.index++));
		dialog.destroyAuto = true;
		this.dialogs.push(dialog);
		dialog.onHide = (function(dialog) {
			return function() {
				var index = this.dialogs.index(dialog);
				this.dialogs.splice(index, 1);
				this.destroiedDialogs.push(dialog);
			};
		})(dialog).bind(this);
		return dialog;
	},
	show: function() {
		var dialog = this.createDialog();
		dialog.show.apply(dialog, $A(arguments));
		return dialog;
	},
	open: function() {
		var dialog = this.createDialog();
		dialog.open.apply(dialog, $A(arguments));
		return dialog;
	}
};
Event.observe(window, "load", function() {
	Dialog = Message = new MessagePane("staticMessagePane");
	Message.removeAuto = true;
	Message.hide();
});