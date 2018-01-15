var Dialog = Class.create({
	visible : false, // 当前是否可见
	containers : null,
	/** 对话框最外层的元素 */
	element: null,
	initialized: false,	// 是否初始化
	moveable: true,	// 是否可拖动
	maximum: false,	// 是否已最大化
	temp: false,	// 是否为临时窗口，如果为临时窗口，则在隐藏时自动销毁
	/** 初始化 */
	initialize : function(id, options) {
		this.id = id;
		// 默认不显示最大化按钮
		this.options = Object.extend({ hideable: true, toggleable: false, header: true }, options || {});
		if (!Object.isUndefined(this.options['moveable'])) this.moveable = !!this.options['moveable'];
		this.temp = !!this.options['temp'];
		this.resize = this.resize.bind(this);
		this.containers = {};
		this.create();


		// 已经初始化
		this.initialized = true;
		var html;
		if (Object.isString(this.options['html'])) {
			html = this.options['value'] = this.options['html'];
			this.options['html'] = null;
		} else {
			html = this.options['value'] || '';
		}
		if (html) {
			// 自动显示,此时options中应该有html,title两个属性
			this.show(hmtl);
		}
		
	},
	
	/** 销毁此对象 */
	destroy: function() {
		document.body.removeChild(this.element);
		this.options = null;
		this.element = null;
		this.overlayer = null;
		this.dialog = null;
		this.header = null;
		this['header.title'] = null;
		this['header.button.toggle'] = null;
		this['header.button.hide'] = null;
		this['header.button'] = null;
		this.body = null;
		this.container = null;
		this.footer = null;
		this.buttons = null;
		this.containers = null;
		
		this.visible = false;
		this.initialized = false;
		
		this.maximum = false;
		this.moveable = true;
		
		this.removeListener();
		this.removeValidator();
	},

	/** 创建对象 */
	create : function() {
		var className = Object.clone(Dialog.DEFAULT_CLASS_NAME);
		if (this.options['className']) Object.extend(className, this.options['className']);
		this.options['className'] = className;
		var div;

		this.element = this.containers['element'] = div = ElementHelper.createElement("div", null, document.body);
		Element.addClassName(div, className['element']);
		Element.hide(this.element);
		
		this.overlayer = this.containers['overlayer'] = div = ElementHelper.createElement("div", null, this.element);
		Element.addClassName(div, className['overlayer']);

		this.dialog = this.containers['dialog'] = div = ElementHelper.createElement("div", null, this.element);
		Element.addClassName(div, className['dialog']);

		this.header = this.containers['header'] = div = ElementHelper.createElement("div", null, this.dialog);
		Element.addClassName(div, className['header']);
		if (this.moveable) {
			// 拖动
			Event.observe(this['header'], "mousedown", (function() {
				//设置捕获范围
				if (this['header'].setCapture) {
					this['header'].setCapture();
				} else if (window.captureEvents) {
					window.captureEvents(Event.MOUSEMOVE | Event.MOUSEUP);
				}
				var onMouseMove = (function() {
					var lastPointer = this.pointer;
					var evt = EventHelper.get();
					this.pointer = evt.pointer();
					var x = this.pointer.x - lastPointer.x;
					var y = this.pointer.y - lastPointer.y;
					if (x == 0 && y == 0) return;
					this.dialog.style.top = this.dialog.offsetTop + y;
					this.dialog.style.left = this.dialog.offsetLeft + x;
					
				}).bind(this);
				Event.observe(this['header'], "mousemove", onMouseMove);
			
				var onMouseUp = (function() {
					this['header'].style.cursor = "auto";
	
					if (this['header'].releaseCapture) {
						this['header'].releaseCapture();
					} else if (window.captureEvents) {
						window.captureEvents(Event.MOUSEMOVE | Event.MOUSEUP);
					}
	
					Event.stopObserving(this['header'], "mousemove", onMouseMove);
					Event.stopObserving(this['header'], "mouseup", onMouseUp);
				}).bind(this);
				Event.observe(this['header'], "mouseup", onMouseUp);
				this['header'].style.cursor = "move";
				var evt = EventHelper.get();
				this.pointer = evt.pointer();
			}).bind(this));
		}


		// 显示图标
		// this.icon = this.containers['header.icon'] = div = ElementHelper.createElement("div", null, div);
		// Element.addClassName(div, className['header.icon']);

		// 显示标题的元素
		this['header.title'] = this.containers['header.title'] = div = ElementHelper.createElement("div", null, this.header);
		Element.addClassName(div, className['header.title']);
		
		// 关闭/最大化按钮等
		this['header.button'] = this.containers['header.button'] = div = ElementHelper.createElement("div", null, this.header);
		Element.addClassName(div, className['header.button']);

		div = this['header.button.hide'] = ElementHelper.createElement("div", { className : "closeButton" }, this['header.button']);
		var closeButton = ElementHelper.createElement("a", {
			href : "javascript: void(0);",
			title : Language.getText('global.dialog.close', "关闭窗口")
		}, div);
		Event.observe(closeButton, "click", (function() { this.hide(); }).bind(this));
		
		div = this['header.button.toggle'] = ElementHelper.createElement("div", { className : "maximize" }, this['header.button']);
		var toggleButton = ElementHelper.createElement("a", {
			href : "javascript: void(0);",
			title : Language.getText('global.dialog.toggle', "切换:最大化/还原")
		}, div);
		Event.observe(toggleButton, "click", (function() { this.toggle(); }).bind(this));


		// 显示滚动条
		this.body = this.containers['body'] = div = ElementHelper.createElement("div", null, this.dialog);
		Element.addClassName(div, className['body']);

		// 如果有滚动条，则需要调整context的宽度、高度等
		this.context = this.containers['context'] = div = ElementHelper.createElement("div", null, this.body);
		Element.addClassName(div, className['context']);
		
		// 显示正文
		this.container = this.containers['container'] = div = ElementHelper.createElement("span", null, this.context);
		Element.addClassName(div, className['container']);

		// 底部按钮
		this.footer = this.containers['footer'] = div = ElementHelper.createElement("div", null, this.dialog);
		Element.addClassName(div, className['footer']);
	},
	
	/** 创建操作按钮	 */
	createButtons: function(buttons) {
		var noButtons = !buttons;
		if (noButtons) buttons = { };
		if (Object.isFunction(buttons)) {
			var handler = (function(scope, handler) { 
				return function() {	handler.apply(scope, $A(arguments)); };
			})(this, buttons);
			buttons = {
				confirm: {
					text: Language.getText('global.confirm', '确 定'),
					handler: handler
				}
			};
		} else if (Object.isString(buttons)) {
			buttons = {
				confirm: {
					text: Language.getText(buttons,  Language.getText('global.confirm')),
					handler: (function() { this.hide(); }).bind(this)
				}
			};
		}
		this.removeButtons();
		this.buttons = { };
		this.buttonsCount = 0;
		this.options['buttons'] = buttons;
		var element, handler;
		for (var button in buttons) {
			button = this.buttons[button] = buttons[button];
			element = ElementHelper.createElement("input", button['attributes']);
			element.type = button['type'] || 'button';
			if (button.id) element.id = button.id;
			this.footer.appendChild(element);
			
			button['element'] = element;
			element.value = button.value || button.text;
			element.disabled = !!button.disabled;
			Element.addClassName(element, this.options['className']['button']);
			if (button.className) Element.addClassName(element, button['className']);
			
			handler = button.handler;
			if (Object.isFunction(handler)) {
				Event.observe(element, "click", handler.bind(this));	// 绑定到this中去
			} else if (handler) {
				for (var evt in handler) {
					Event.observe(element, evt, handler[evt].bind(this));
				}
			}
			this.buttonsCount ++;
		}
		noButtons = this.buttonsCount < 1;
		
		if (noButtons) {
			Element.hide(this.footer);
		} else {
			Element.show(this.footer);
		}
	},
	
	/** 删除按钮 */
	removeButtons: function(buttons) {
		if (!buttons) {
			buttons = this.buttons;
			for (var button in buttons) {
				if (!(button = this.buttons[button])) continue;
				if (!button.element) return;
				this.footer.removeChild(button.element);
			}
			this.buttons = null;
		} else {
			// 删除指定的按钮
			if (Object.isString(buttons)) buttons = buttons.split(",");
			else if (!Object.isArray(buttons)) buttons = $A(buttons);
			var me = this;
			buttons.each(function(button) {
				var btn = button;
				if (!(button = me.buttons[button])) return;
				if (!button.element) return;
				me.footer.removeChild(button.element);
				me.buttons[btn] = null;
			});
		}
	},
	/**
	 * 显示
	 * 
	 * @param html 显示内容
	 * @param title 标题
	 * @param options 一些参数
	 */
	show : function(html, title, options) {
		if (!this.initialized) {
			this.initialize(this.id, this.options);
		}
		this.visible = true;
		Event.observe(window, "resize", this.resize);		
		if (Object.isUndefined(html)) {
			// 直接显示
			Element.show(this.element);
			return this;
		}
		if (this.maximum) {
			this.maximum = false;
			Element.setClassName(this['header.button.toggle'], "maximize");
		}
		if (!options && html && !Object.isString(html)) {
			options = html;
			if (Object.isString(options['html'])) {
				html = options['value'] = options['html'];
				options['html'] = null;
			} else {
				html = options['value'] || this.options['value'] || '';
			}
		} else if (!options && title && !Object.isString(title)) {
			options = title;
			title = null; // options['title'] || this.options['title'] || '';
		}
		var opt = Object.clone(this.options);
		options = Object.extend(opt, options || { });
		if (this['header.button.hide']) {
			if (options['hideable']) {
				this['header.button.hide'].style.visibility = '';
			} else {
				this['header.button.hide'].style.visibility = 'hidden';
			}
		}
		if (this['header.button.toggle']) {
			if (options['toggleable']) {
				this['header.button.toggle'].style.visibility = '';
			} else {
				this['header.button.toggle'].style.visibility = 'hidden';
			}
		}
		this.options['showOptions'] = options;	// 当先显示操作的参数
		
		this.container.innerHTML = html;

		if (window['CalendarHelper']) CalendarHelper.observe(this.container);
		
		this['header.title'].innerHTML = title || options['title'] || '';
		html = options['html'] || {};
		for ( var e in this.containers) {
			if (Object.isUndefined(html[e])) continue;
			if (Object.isString(html[e])) this.containers[e].innerHTML = html[e];
			Element[html[e] ? 'show' : 'hide'](this.containers[e]);
		}
		
		var buttons = options['buttons'] || this.options['buttons'];
		if (!buttons && !this.footer.innerHTML) buttons = 'global.confirm';	// 仅显示一个确定按钮
		if (buttons) { // 操作按钮
			this.createButtons(buttons);
		}

		Element.show(this.element);
		
		if (!options['header']) {
			// 隐藏标题
			Element.hide(this.header);
		}
		
		// 进行定位
		this.repaint();
		
		// 添加一次性事件
		var listener = options['listener'] || options['after'] || options['on'];
		if (listener) this.addListener('show', listener, 'once');

		listener = options['show'];	// onshow事件一直有效
		if (listener) { this.addListener('show', listener); }
		
		listener = options['onShow'];	// onshow事件一直有效
		if (listener) this.addListener('show', listener);
		
		listener = options['hide'];	// hide事件一直有效
		if (listener) this.addListener('hide', listener);
		
		listener = options['onHide'];	// onHide事件一直有效
		if (listener) this.addListener('hide', listener);

		// hideOnce事件
		if (options['hideOnce']) {
			this.hideOnce(options['hideOnce']);
		}
		
		// 添加可能会有的其他事件
		this.addListener(options['beforeListeners'], 'once');
		this.addListener(options['listeners'], 'once');
	},
	/** 隐藏对话框 */
	hide : function() {
		if (!this.visible) {
			// 如果当前是隐藏的，则不调用after事件
			throw $break;
		}
		Element.hide(this.element);
		this.visible = false;
		Event.stopObserving(window, "resize", this.resize);
		this.hideOnce();
		if (this.temp) {
			// 自动销毁
			this.addListener('hide', this.destroy.bind(this));
		}
	},
	close: function() {
		this.hide();
	},
	/**
	 * 在窗口隐藏时调用，仅调用一次
	 * 
	 * @param listener
	 *            若存在，则添加一个hideOnce事件，否则则视为调用hideOnce事件
	 */
	hideOnce : function(listener) {
		if (listener) {
			this.addListener('hideOnce', listener);
			throw $break; // 不调用after事件
		} else {
			this.addListener('hideOnce', function() {
				// 删除所有hideOnce监听
				// alert('this.removeListener(\'hideOnce\')' + this.listeners['hideOnce']);
				this.removeListener('hideOnce');
				// alert('this.removeListener(\'hideOnce\')后:' + this.listeners['hideOnce']);
			});
		}
	},

	/**
	 * 根据内容显示窗口
	 */
	paint: function(options) {
		if (!this.visible) return;
		var maxWidth = bodyWidth = document.body.offsetWidth;
		var maxHeight = bodyHeight = document.body.offsetHeight;
		
		this.element.style.width = maxWidth;
		this.element.style.height = maxHeight;		
		this.overlayer.style.width = maxWidth;
		this.overlayer.style.height = maxHeight;

		maxWidth = bodyWidth = this.element.offsetWidth;
		maxHeight = bodyHeight = this.element.offsetHeight;
		
		options = options || this.options['showOptions'] || { };
		var width, height;

		var minWidth = options['minWidth'] || this.options['minWidth'] || 0;
		var minHeight = options['minHeight'] || this.options['minHeight'] || 0;

		var autoHeight = true;	// 自适应高度
		if (height = options['height'] || this.options['height']) {
			this.dialog.style.height = Math.max(minHeight, height) + 'px';
			autoHeight = false;
		} else if (this.dialog.offsetHeight < minHeight) {	// 规定了最小高度
			this.dialog.style.height = minHeight + 'px';
			autoHeight = false;
		}

		var autoWidth = true;	// 自适应宽度
		if (width = options['width'] || this.options['width']) {
			width = Math.max(minWidth, width);
			this.dialog.style.width = width + 'px';
			autoWidth = false;
		} else {
			// 根据内容指定宽度
			// alert('of' + this.container.offsetWidth);
			minWidth = Math.max(minWidth, this['header.title'].offsetWidth  + 50);	// 50是右上关闭按钮等
			width = Math.max(minWidth, this.container.offsetWidth);
			this.context.style.width = width + 'px';
			// this.body.style.width = width + 'px';
			// this.dialog.style.width = width + 'px';
			this.dialog.style.width = this.body.offsetWidth + 'px';
		}

		width = options['maxWidth'] || this.options['maxWidth'] || 0;
		if (width) maxWidth = Math.min(maxWidth, width);

		height = options['maxHeight'] || this.options['maxHeight'] || 0;
		if (height) maxWidth = Math.min(maxHeight, height);
		
		width = this.dialog.offsetWidth;
		height = this.dialog.offsetHeight;
		if (width > maxWidth) {
			// 超出了宽度
			this.dialog.style.width = (width = maxWidth) + 'px';
			autoWidth = false;
		}
		
		// 给body的width赋值
		this.body.style.width = (width) + 'px';
		var dialogWidth = this.body.offsetWidth;
		// var newWidth = width;
		while (dialogWidth > width) {
			this.body.style.width = (dialogWidth - (dialogWidth - width)) + 'px';
			dialogWidth = this.body.offsetWidth;
		}
		
		if (height > maxHeight) {
			// 超出了高度
			this.dialog.style.height = (height = maxHeight) + 'px';
			autoHeight = false;
		}
		// var layout = Element.getLayout(this.dialog);
		height = this.dialog.offsetHeight; // layout.get('height');

		// alert(height);
		
		// 判断footer是否被隐藏
		if (height != this.footer.offsetTop + this.footer.offsetHeight) {
			// footer被隐藏或者未在底部
			height = (height - this.footer.offsetHeight - this.header.offsetHeight);
			this.body.style.height = height + 'px';
		}
		if (height < this.body.offsetHeight) this.body.style.height = (height - (this.body.offsetHeight - height)) + 'px';
		
		//alert('bodyHeight' + this.body.offsetHeight);
		/*else if (height > this.footer.offsetTop + this.footer.offsetHeight) {
			// footer需要置底
			var offsetHeight = layout.get('border-box-height') - layout.get('height');
			this.body.style.height = (height - offsetHeight - this.footer.offsetHeight - this.header.offsetHeight) + 'px';
		}*/

		Layout.resizeScroll(this.body);	// 在IE7下处理滚动条

		//alert('bodyHeight2' + this.body.offsetHeight);
		// 出现了横向滚动条，并且是自适应宽度
		var offsetScroll = (this.body.scrollWidth || 0) - this.body.clientWidth;
		var overflowX = (this.body.scrollWidth || 0) - this.body.clientWidth;
		if (overflowX > 0 && autoWidth) {
			// alert('X');
			// 出现了横向滚动条
			this.body.style.width = Math.min(maxWidth, (this.context.offsetWidth + overflowX)) + 'px';
			this.dialog.style.width = this.body.offsetWidth + 'px';
		}
		//alert('bodyHeight3' + this.body.offsetHeight);

		var overflowY = (this.body.offsetHeight || 0) - this.body.clientHeight;
		// alert('autoHeight = ' + autoHeight + ' ' + overflowY + ' = ' + this.body.offsetHeight + ' ' + this.body.clientHeight);
		if (overflowY > 0 && overflowY < 22 && autoHeight && this.dialog.offsetHeight < maxHeight) {
			// alert('xxxxxxx');
			// 有竖向滚动条,此情况一般是因为在IE7中，出现横向滚动条而造成可视区域的高度不足，因此需要加高高度
			height = this.dialog.offsetHeight;
			// alert('Y' + height);
			var newHeight = Math.min(maxHeight, height + overflowY);
			// alert('Y' + newHeight);
			overflowY = newHeight - height;
			// alert('Y' + overflowY);
			if (overflowY > 0) {
				this.dialog.style.height = newHeight + 'px';
				this.body.style.height = (this.dialog.offsetHeight + overflowY) + 'px';
			}
		}

		//alert('bodyHeight4' + this.body.offsetHeight);
		// alert(this.body.offsetWidth);
		this.header.style.width = this.body.offsetWidth + 'px';
		this.dialog.style.width = this.body.offsetWidth + 'px';
		width = this.dialog.offsetWidth;
		height = this.dialog.offsetHeight;
		
		
		// 左上角所在位置，参数为一个比例
		var top = options['top'];
		var left = options['left'];
		if (Object.isUndefined(top)) top = 0.3;	// 默认
		else if (Object.isString(top) && top.toLowerCase() == 'center') top = 0.5;	// 居中
		if (Object.isUndefined(left) || (Object.isString(left) && left.toLowerCase() == 'center')) left = 0.5;	// 默认居中
		if (Object.isNumber(top) && top <= 1) top = (top * (bodyHeight - height)) + 'px';
		if (Object.isNumber(left) && left <= 1) left = (left * (bodyWidth - width)) + 'px';
		
		this.dialog.style.top = top + (Object.isNumber(top) ? 'px' : '');
		this.dialog.style.left = left + (Object.isNumber(left) ? 'px' : '');
	},
	
	/** 重画数据 */
	repaint: function(options) {
		if (!this.visible) return;
		this.dialog.style.width = 'auto';
		this.dialog.style.height = 'auto';
		this.body.style.width = 'auto';
		this.body.style.height = 'auto';
		this.context.style.width = 'auto';
		this.context.style.height = 'auto';
		this.paint(options);
	},
	
	maximum: false,	// 是否已最大化
	/**
	 * 窗口最大化/还原
	 */
	toggle: function() {
		if (!this.visible) return;
		this.maximum = !this.maximum;
		Element.setClassName(this['header.button.toggle'], this.maximum ? "normalization" : "maximize");
		
		if(this.maximum) {
			// 最大化
			this.location = {
				top: this.dialog.offsetTop,
				left: this.dialog.offsetLeft,
				width: this.dialog.offsetWidth,
				height: this.dialog.offsetHeight
			};
			this.repaint({
				top: 0,
				left: 0,
				width: document.body.offsetWidth,
				height: document.body.offsetHeight
			});
		} else {
			// 还原
			this.repaint(this.location);
		}
	},
	
	/** 变化窗口大小 */
	resize: function() {
		if (!this.visible) return;
		if(this.maximum) {
			// 最大化
			this.paint({
				top: 0,
				left: 0,
				width: document.body.offsetWidth,
				height: document.body.offsetHeight
			});
		} else {
			// 还原
			this.repaint();
		}
	},
	
	/**
	 * 读取url的内容进行显示
	 * @param url
	 * @param title
	 * @param options
	 */
	open: function(url, title, options) {
		options = options || { };
		var _options = options['request'] || { };
		var _onSuccess = _options['onSuccess'] || options['onSuccess'];
		var onSuccess = (function(response, json) {
			this.show(response.responseText, title, options);
			if (_onSuccess) _onSuccess(response, json);
		}).bind(this);
		_options.onSuccess = onSuccess;
		_options.parameters = _options['parameters'] || options['parameters'];
		var transport = options['transport'] || this.options['transport'] || AjaxHelper;
		transport.send(url, _options);
	}
});

Interceptable.intercept(Dialog);
(function() {
	var dialog;
	Language.ready(function() {
		var id = 'STATIC_FINAL_DIALOG_' + Math.random();
		dialog = new Dialog(id);	// 定义一个全局的静态对话框
	});
	Object.extend(Dialog, {
		DEFAULT_CLASS_NAME : {
			'element': 'dialog',
			'overlayer' : "dialog-overlayer",
			'overlayer.frame' : "dialog-overlay-frame",
			'dialog' : 'dialog-dialog',
			'header' : 'dialog-header',
			'header.icon' : 'dialog-header-icon',
			'header.title' : 'dialog-header-title',
			'header.button' : 'dialog-header-button shortcutBar',
			'body' : 'dialog-body',
			'context' : 'dialog-context',
			'container' : 'dialog-container',
			'footer' : 'dialog-footer',
			'button' : 'dialog-footer-button button'
		},
		
		// 定义一些静态操作
		DEFAULT_DIALOG: dialog,
		show: function() {
			dialog.show.apply(dialog, $A(arguments));
			return dialog;
		},
		open: function(url, title, options) {
			dialog.open.apply(dialog, $A(arguments));
			return dialog;
		},
		hide: function() {
			dialog.hide();
		}
	});
})();

/** 临时的弹出窗口管理器 */
var TempDialogManager = TempDialog = {
	index: 0,
	dialogs: [],			// 正在使用的窗口
	createDialog: function() {
		var dialog = new Dialog('tempDialog' + (this.index++), {
			temp: true
		});
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