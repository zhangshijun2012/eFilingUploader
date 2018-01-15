/**
 * 系统首页main.html的类
 */
var Main = {
	FIRST_LOGIN: false,
	module: '', // 要加载的菜单,传入的应该是moduleId,如果有此参数,则是外部进行调用,此时仅加载一个菜单,并隐藏头部和菜单
	maximum: false,	// 如果初始module不为空,则根据此参数判断是否最大化窗口,如果为true则隐藏菜单栏和头部窗口
	dept: '', // dept默认的登录部门 

	/** 将正文区域最大化 */
	maximize: function() {
		// 隐藏头部和菜单栏
		Element.hide($('header'));
		Element.hide($('nav'));

		var e = $('bodyContainer');
		if (e) e.style.top = 0;
		
		Menu.hidden = false;
		Menu.toggleContainer();
		this.maximum = true;
	},
	/** 最大化后恢复正常 */
	normalize: function() {
		// 隐藏头部和菜单栏
		var header = $('header');
		var nav = $('nav');
		Element.show(header);
		Element.show(nav);

		var e = $('bodyContainer');
		if (e) e.style.top = (header.offsetHeight + nav.offsetHeight) + 'px';
		
		Menu.hidden = false;
		Menu.toggleContainer();
		this.maximum = false;
	},

	 
	ready: function() {
		if (this.module && this.maximum) {
			this.maximize();
		}
		
		var ready = (function() {
			// alert(this.FIRST_LOGIN);
			if (this.FIRST_LOGIN) {
				// 第一次访问需要选择登录机构
				this.selectDeparment();
			} else {
				if (this.module) {
					if (Ajax.AJAX_COUNT > 0) {
						// 有异步加载的数据尚未完成,等待
						return window.setTimeout(ready, 200);
					} else {
						this.load(this.module);
					}
				} else {
					// 加载菜单
					this.loadMenu();
				}
				// Module.load('eFiling.file.scan');
			}
		}).bind(this);
		
		Language.ready(ready);
	},
	/*---------以下为加载菜单的方法---------------------------------------------------------------------*/
	/** 加载根节点菜单 */
	loadMenu: function() { 
		Menu.load();
	},
	user: null,	// 当前登录用户
	loginForm: 'loginForm',
	logout: function() {
		window.location = 'logout.do';
	},
	/**
	 * 切换登录部门
	 * @param value
	 */
	changeDepartment: function(value) {
		var form = $('currentDepartmentId').form;
		form.action = 'index.do';
		form.method = 'post';
		form.target = '_self';
		form.submit();
	},
	/** 
	 * 选择登录机构,如果user参数不存在，则弹出登录框供选择
	 * @param user 如果存在user，则是登录完成之后的回调信息
	 */
	selectDeparment: function(user) {
		if (user) {
			// 设定用户的机构信息等
			this.user = user;
			var departmentId = user.department.id;
			$('currentDepartmentId').value = departmentId;
			return;
		}
		user = this.user;
		var queryString = window.location.search || '';
		if (queryString) queryString = queryString.replaceAll('firstLogin=first', 'firstLogin=false');
		var html = '<div class="login-dialog"><form name="' + this.loginForm 
			// + '" action="index.do?module=' + this.module + '&dept=' + this.dept + '" method="post" target="_self">'
			+ '" action="index.do' + queryString + '" method="post" target="_self">'
			+ '<div class="login-dialog-header"></div>'
			+ '<div class="login-dialog-nav"></div>'
			+ '<div class="login-dialog-body"><div class="login-dialog-body-body">'
			+ '<p><img src="' + Base.template + 'images/login/username.gif" />'
			+ '<input name="userName" value="' + user.name + '" class="text" readonly /></p>'
			+ '<p><img src="' + Base.template + 'images/login/department.gif" />'
			+ '<select name="currentDepartmentId">';
		user.departments.each(function(department) {
			html = html + '<option value="' + department.id + '"'
				+ (department.id == user.department.id ? ' selected' : '') + '>'
				+ department.id + ' - ' + department.name 
				+ '</option>';
		});
		html = html 
			+ '</select>'
			+ '</p>'
			+ '<p><img src="' + Base.template + 'images/login/language.gif" />'
			+ '<select name="request_locale">'
			+ '<option value="zh_CN">简体中文</option>'
			+ '<option value="en_US"' + (user.locale.startsWith('en') ? ' selected' : '') + '>English</option>'
			+ '</select>'
			+ '</div></div>'
			
			+ '<div class="login-dialog-footer"><table class="form" height="100%"><tr><td height="100%" class="txtCenter">' 
			+ '<input class="button" type="submit" value="' + Language.get('global.confirm') + '" />' 
			+ '</td></tr></table></div>'
			
			+ '</form></div>';
		var dialog = new Dialog('login-dialog', {
			className: {
				'element': 'dialog dialog-login',
				'dialog': 'dialog-login-dialog',
				'header': 'dialog-login-hidden',
				'footer': 'dialog-login-hidden'
			},
			temp: true
		});
		dialog.show(html, null, {
			width: 481,
			/* top: 100, */
			on: function() {
				if (!Main.module) return;
				var form = document.forms[Main.loginForm];
				var field = form.elements['currentDepartmentId'];
				if (field.options.length > 1 && !!Main.dept) {
					field.value = Main.dept;
				}
				
				if (field.value) form.submit();
			}
		});
	},
	
	/**
	 * 登录后点确定或者切换了部门后触发的事件
	 * @param options
	 */
//	login: function(options) {
//		options = options || { };
//		var parameters = options['parameters'];
//		if (!parameters) parameters = options['parameters'] = Form.serialize($(this.loginForm));
//		var onSuccess = options['onSuccess'];
//		options['onSuccess'] = (function(response) {
//			document.location.reload();
//			/*this.loadMenu();	// 重新加载菜单
//			var text = response.responseText;
//			var user = StringHelper.evalJSON(text);
//			this.selectDeparment(user);
//			if (onSuccess) onSuccess.apply(this, $A(arguments));
//			*/
//		}).bind(this);
//	},
	
	/*---------以下为加载模块的方法---------------------------------------------------------------------*/
	services: { },
	/**
	 * 加载模块module.请参见module.js
	 * 默认的情况下,加载之后会调用其对应类的main方法.
	 * 
	 * @param module
	 */
	load: function(module) {
		Module.load(module, {
			onSuccess: function() {
				Main.previousModule = Main.module;
				Main.previousService = Main.service;
				Main.module = Module['module'];
				Main.service = Module['service'];
				Main.services[Main.module['id']] = Module['service'];
			}
		});
	},
	
	/** 刷新,重新加载当前模块 */
	refresh: function() {
		Module.load(Module.module);
	},
	
	/** 打印 */
	print: function(content, onload) {
		window['printContent'] = content;
		window['printOnload'] = onload;
		
		//this.printWindow = window.open("print.html", "printWindow", 
		//		"width=900, height=700, top=0, left=" + (window.screen.width - 900) / 2 + " toolbar=no, menubar=yes, scrollbars=yes, resizable=yes, location=no, status=no");
		//this.printWindow.focus();

		this.printWindow = this.openWindow('print.html', 'printWindow');
		
	},
	
	/** 打开窗口 */
	openWindow: function(url, options) {
		if (Object.isString(options)) options = { name: options };
		else options = options || { };
		var width = options['width'] || 800;
		var height = options['height'] || 600;
		var top = options['top'];
		var left = options['left'];
		var maxWidth = screen.availWidth;
		var maxHeight = screen.availHeight;
		width = Math.min(width, maxWidth);
		height = Math.min(height, maxHeight);
		
		// 默认居中显示
		if (Object.isUndefined(left)) {
			left = (maxWidth - width) / 2;
		}
		if (Object.isUndefined(top)) {
			top = (maxHeight - height) / 2;
		}
		
		var parameters = options['parameters'] || 'toolbar=no, menubar=yes, scrollbars=yes, resizable=yes, location=no, status=no';
		parameters = 'width=' + width + ', height=' + height + ', left=' + left + ', top=' + top + ', ' + parameters;
		return window.open("print.html", options['name'] || '', parameters);
	}
};

Event.observe(window, "unload", function() { if (Main.printWindow) Main.printWindow.close(); });
