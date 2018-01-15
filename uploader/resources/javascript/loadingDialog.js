var LoadingDialog = Class.create(Dialog, {
	times : 0,
	
	/** 创建对象 */
	create : function() {
		var className = Object.extend({ }, LoadingDialog.DEFAULT_CLASS_NAME);
		if (this.options['className']) Object.extend(className, this.options['className']);
		this.options['className'] = className;
		var div;
		this.element = this.containers['element'] = div = ElementHelper.createElement("div", null, document.body);
		div.id = this.id;
		Element.addClassName(div, className['element']);
		Element.hide(this.element);
		
		this.overlayer = this.containers['overlayer'] = { };

		this.dialog = this.containers['dialog'] = div = ElementHelper.createElement("div", null, this.element);
		Element.addClassName(div, className['dialog']);

		this.header = this.containers['header'] = { };

		// 显示标题的元素
		this['header.title'] = this.containers['header.title'] = { };
		
		// 关闭/最大化按钮等
		this['header.button'] = this.containers['header.button'] = { };

		this.body = this.containers['body'] = div = ElementHelper.createElement("div", null, this.dialog);
		Element.addClassName(div, className['body']);
		
		this.context = this.containers['context'] = { };
		
		// 显示正文
		this.container = this.containers['container'] = div = ElementHelper.createElement("span", null, this.body);
		Element.addClassName(div, className['container']);

		// 底部按钮
		this.footer = this.containers['footer'] = { };
	},

	repaint: function(options) {
		if (!this.visible) return;
		this.dialog.style.width = 'auto';
		this.dialog.style.height = 'auto';
		this.body.style.width = 'auto';
		this.body.style.height = 'auto';
		this.paint(options);
	},
	paint: function(options) {
		if (!this.visible) return;
		options = options || this.options['show'] || { };
		
		var maxWidth = bodyWidth = document.body.offsetWidth;
		var maxHeight = bodyHeight = document.body.offsetHeight;
		
		var width = this.container.offsetWidth;
		this.body.style.width = Math.max(100, width) + 'px';
		this.dialog.style.width = this.body.offsetWidth + 'px';
		/*
		var height = this.container.offsetHeight;
		this.body.style.height = height + 'px';
		this.dialog.style.height = this.body.offsetHeight + 'px';
		*/

		// this.dialog.style.height = this.body.offsetHeight + 'px';
		width = this.dialog.offsetWidth;
		height = this.dialog.offsetHeight;

		this.element.style.width = width + 'px';
		this.element.style.height = height + 'px';
		
		// 左上角所在位置，参数为一个比例
		var top = options['top'];
		if (Object.isUndefined(top)) top = 54 + 'px';	// 默认顶部50px
		else if (Object.isNumber(top) && top <= 1) top = (top * (bodyHeight - height)) + 'px';
		this.element.style.top = top + (Object.isNumber(top) ? 'px' : '');

		var left = options['left'];
		var right = options['right'];
		if (Object.isUndefined(left) && Object.isUndefined(right)) right = 1;	// 默认居右1px
		if (!Object.isUndefined(right)) {
			left = right;
			if (Object.isNumber(right)) {
				if (right >= 1) {
					left = (bodyWidth - width - right) + 'px';
				} else {
					left = ((bodyWidth - width) * (1 - right)) + 'px';
				}
			}
		} else if (!Object.isUndefined(left) && Object.isNumber(left)) {
			if (left <= 1) {
				left = ((bodyWidth - width) * left) + 'px';
			}
		}
		
		this.element.style.left = left + (Object.isNumber(left) ? 'px' : '');;
	},
	/**
	 * 显示Loading框
	 * @param $super
	 * @param message 显示的文字
	 * @param ico 图标
	 * @param options
	 */
	show : function($super, message, ico, options) {
		this.times++;
		if (!this.visible) {		
			ico = ico === false ? '' : (ico || LoadingDialog.DEFAULT_ICO);
			message = message || LoadingDialog.DEFAULT_MESSAGE || Language.getText('global.loading', '正在加载，请稍候...');
			var html = ico + ' ' + message;
			
			$super(html, null, options);
		}
		this.disable();
	},

	hide : function($super) {
		if (this.times > 0)	this.times--;
		if (!this.times) {
			$super();
			this.enable();
		}
	},

	createButtons : Prototype.emptyFunction,
	
	/* 被自动disabled的按钮 */
	disabledElements: [],
	/* 将所按钮disabled */
	disable: function() {
		var inputs = $A(document.getElementsByTagName("input"));
		var disabledElements = this.disabledElements;
		inputs.each(function(input){
			if (input.disabled) return;
			if (!['submit', 'reset', 'button', 'image'].include(input.type)) return;
			input.disabled = true;
			disabledElements.push(input);
		});
	},
	
	/* 释放所有按钮 */
	enable: function() {
		var input;
		while (input = this.disabledElements.shift()) {
			if (StringHelper.parseBoolean(input.getAttribute('outerDisabled')))  {
				// 被外部禁用
				// input.setAttribute('outerDisabled', 'false'); 
			} else {
				input.disabled = false;
			}
		};
	}
});

Object.extend(LoadingDialog, {
	DEFAULT_ICO: '<img src="' + Base.template + 'images/loading_16x16.gif" align="absmiddle" />',
	DEFAULT_MESSAGE: Language.getText('global.loading', '正在加载，请稍候...'),
	DEFAULT_CLASS_NAME : {
		'element': 'dialog dialog-loading',
		'overlayer' : "dialog-overlayer",
		'overlayer.frame' : "dialog-overlay-frame",
		'dialog' : 'dialog-dialog',
		'header' : 'dialog-header',
		'header.icon' : 'dialog-header-icon',
		'header.title' : 'dialog-header-title',
		'header.button' : 'dialog-header-button shortcutBar',
		'body' : 'dialog-body',
		'container' : 'dialog-container',
		'footer' : 'dialog-footer',
		'button' : 'dialog-footer-button button'
	}
});
Interceptable.intercept(LoadingDialog, '*', ['show', 'hide']);
var Loading;
Language.ready(function() {
	LoadingDialog.DEFAULT_MESSAGE = Language.getText('global.loading', '正在加载，请稍候...');
	Loading = new LoadingDialog('loading-dialog');
});