Loading = {
	//当前的对象
	loading: false,
	busy: false,
	autoDisabled: 1,	/* 1表示既自动disabled按钮也自动释放，-1表示只自动disabled，0表示不自动操作, -2表示只自动释放 */

	//为了防止程序卡住,连续发现reSetBusyMax次busy则重新设置busy标记为false
	reSetBusy: 0,
	reSetBusyMax: 5,
	
	/* 被自动disabled的按钮 */
	disabledElements: null,
	/* 将所按钮disabled */
	disabled: function() {
		this.disabledElements = [];
		//操作所有按钮,e未定义或者e为true或false
		var buttons = document.body.getElementsByTagName("input");
		var n = 0;
		for (var i = 0, l = buttons.length; i < l; i++) {
			if ((buttons[i].type == "submit" || buttons[i].type == "reset" || 
				buttons[i].type == "button" || buttons[i].type == "image") && !buttons[i].disabled) {
				this.disabledElements[n] = buttons[i];
				// n += Form.disabled(buttons[i]);
			}
		}
		return n;
	},
	
	/* 释放所有按钮 */
	available: function() {
		if (!this.disabledElements) {
			return;
		}
		
		for (var i = 0, l = this.disabledElements.length; i < l; i++) {
			// Form.disabled(this.disabledElements[i], false);
		}
		
		this.disabledElements = null;
	},

	//当先是否有正在处理的请求
	isBusy: function () {
		if ((this.reSetBusy >= this.reSetBusyMax) && this.busy) {
			this.busy = false;
		} else if (this.busy) {
			//alert("还有未完成的操作,请稍候重试...");
			this.reSetBusy = this.reSetBusy + 1;
			return true;
		}
		if (this.reSetBusy != 0) {
			this.reSetBusy = 0;
		}
		return this.busy;
	},

	DEFAULT_ICO: "<img src=\"" + Base.template + "images/loading_16x16.gif\" align=\"absmiddle\" /> ",
	DEFAULT_MESSAGE: "正在加载,请稍候...",
	//显示隐藏的loading对象
	show: function (message, ico, fn) {
		if (this.isBusy()) {
			return false;
		}
		
		//标记为忙状态
		this.busy = true;
	
		if (this.autoDisabled === 1 || this.autoDisabled === -1) { 
			//Form.disabled();
			this.disabled();
		}
		
		//显示隐藏层
		if (!this.loading) {
			this.create();
		}
		
		message = (ico === false ? "" : (ico ? ico : this.DEFAULT_ICO)) + (message ? message : this.DEFAULT_MESSAGE);
		this.loading.innerHTML = message;
		Element.show(this.loading);
		this.setLocation({right: '0px'});
		if (fn) {
			fn();
		}
	},
	
	/* 定时器对象 */
	autoHideTimer: null,
	/**
	 * 显示后自动隐藏
	 * times: 显示的时间 
	 */
	showAutoHide: function (message, times, ico, fn) {
		this.busy = false;
		window.clearTimeout(this.autoHideTimer);	// 清除自动隐藏的定时器
		this.show(message, ico, fn);
		times = Math.max(1, NumberHelper.intValue(times, 1000));
		this.autoHideTimer = window.setTimeout("Loading.hide()", times);
	},
	
	hide: function (fn) {//隐藏loading对象
		if (!this.loading) {
			return false;
		}
		
		//隐藏
		Element.hide(this.loading);
		
		if (this.autoDisabled === 1 || this.autoDisabled === -2) { 
			//Form.disabled(false);
			this.available();
		}
		
		//状态标记为空闲
		this.busy = false;
		if (fn) {
			fn();
		}
	},
	
	create: function () {//创建隐藏对象
		this.loading = document.createElement("div");
		this.loading.id = "loading";
		this.loading.innerHTML = "正在加载,请稍候...";
		Element.hide(this.loading);
		document.body.appendChild(this.loading);
	},
	
	DEFAULT_LEFT_RATE: 1 - 0.618,	// 默认的左边比例
	
	/* 
	 * 定位进度框位置。
	 * 默认的将进度框左侧放在DEFAULT_LEFT的位置
	 * options: {
	 *     left: 距离左侧的位置.此参数如果为[-1,1]则表示比例.要居中则使用0.5.如果要指定距离左侧1px,则使用1px.
	 *	   right: 距离右侧的位置.left与right如果同时出现,以left为准.
	 *     top: 距离顶端的位置
	 * }
	 */
	setLocation: function(options) {
		var _options = {
			// left: Loading.DEFAULT_LEFT_RATE		// 距离左侧的位置.此参数如果为小数则表示比例.要居中则使用0.5
			// top:								// 距离顶端的位置
		};
		this.loading.style.width = '';
		if (NumberHelper.isNumber(options) || Object.isString(options)) {
			_options.left = options;
		} else {
			Object.extend(_options, options || { });
		}
		var left = null;
		 if (Object.isUndefined(options.left) && !Object.isUndefined(_options.right)) {
			// 未定义left,且定义了right
			left = NumberHelper.doubleValue(_options.right);
			if (left < 1 && left > -1) {
				left = Math.abs(left);
				left = (document.body.clientWidth - this.loading.offsetWidth) * (1 - left);
			} else {
				left = document.body.clientWidth - this.loading.offsetWidth - parseInt(StringHelper.trim(_options.right).replaceAll(",", ""));
			}
		} else if (!Object.isUndefined(options.left)) {
			// 定义了left
			left = NumberHelper.doubleValue(_options.left);
			if (left <= 1 && left >= -1 && left != 0) {
				left = Math.abs(left);
				left = (document.body.clientWidth - this.loading.offsetWidth) * left;
			} else {
				left = _options.left;
			}
		}
		this.loading.style.left = left;
	}
};
