/** 菜单操作类 */
var Menu = {
	/** 加载菜单的地址 */
	URL: 'loadMenus.do',
	rootContainer: 'menu',	// 显示菜单的根节点
	loading: false,
	menus: { },
	/**
	 * 加载菜单
	 * @param parentId 上级菜单
	 * @param container 显示子菜单的DOM节点
	 */
	load: function(parentId) {
		this.loading = true;
		var url = this.URL + "?parentId=" + (parentId || "");
		var parentMenu = parentId ? this.menus[parentId] : null;
		var onSuccess = (function (parent) { 
			return function(response) {
				this.loading = false;
				var text = response.responseText.trim();
				var menus;
				var html = '';
				if (!text) {
					html = '没有任何可操作的菜单,可能是你没有权限操作本系统.';
				} else if (!(menus = StringHelper.evalJSON(text))) {
					html = '菜单加载失败:' + text;
				} else {
					var menu;
					var level = parent ? parent['level'] + 1 : 0;
					var parentNav = parent ? parent['nav'] + '<span class="navSeparator">&gt;&gt;</span>' : '';
					for (var menuId in menus) {
						menu = menus[menuId];
						if (!menu) continue;
						menu['name'] = Language.getText(menu['menuEName'], menu['name']);
						this.menus[menuId] = menu;
						menu['parent'] = parent;	// 菜单等级
						menu['level'] = level;	// 菜单等级
						menu['nav'] = parentNav + menu['name'];	// 菜单等级
						
						menu['container'] = 'menu_' + menuId;
						menu['headerContainer'] = 'menuHeader_' + menuId;
						var empty = menu['empty'] = menu['flag'] == '1';
	
	
						html += '<div class="menuContainer collapsed" id="' + menu['container'] + '">\n\t';
						html += '<div id="' + menu['headerContainer'] + '" class="menuHeader"';
						html += ' title="' + menu['name'] + '" onclick="Menu.click(\'' + menuId + '\')">';
						html += '<a href="javascript: void(0)">' + menu['name'] + '</a>';
						html += '</div>\n\t';
						
						if (!empty) {
							menu['childrenContainer'] = 'menuBody_' + menuId;
							html += '<div id="' + menu['childrenContainer'] + '" class="menuBody">&nbsp; &nbsp; ' + Language.getText('global.loading', 'Loading...') + '</div>\n';
						}
						html += '</div>\n';
					}
				}
				$(parent ? parent['childrenContainer'] : this.rootContainer).innerHTML = html;
			};
		})(parentMenu);
		onSuccess = onSuccess.bind(this);
		AjaxHelper.send(url, {
			onSuccess: onSuccess
		});
		// AjaxHelper.update(container || "menuBody", );
	},
	/**
	 * 点击菜单
	 * @param menuId
	 * @param options
	 */
	click: function(menuId, options) {
		if (!menuId) return;
		var menu = this.menus[menuId];
		var container = menu['container'] = $(menu['container']);
		if (menu['empty']) {
			// 叶子节点,执行加载程序
			this.action(menuId, options);
		} else {
			// 展开/折叠节点
			this.toggle(menuId, options);
		}
		
	},
	/**
	 * 根据当前菜单状态切换菜单显示状态
	 * @param menuId 菜单id
	 * @param options
	 */
	toggle: function(menuId, options) {
		var menu = this.menus[menuId];
		var expand = !menu['expanded'];
		this[expand ? 'expand' : 'collapse'](menuId, options);
	},
	
	/** 操作菜单触发的事件,可在其他地方覆盖此方法 */
	actionPerformed: function(options) {
		Main.load(options);
	},
	
	/** 导航信息显示DOM节点 */
	navContainer: 'currentNav',
	/** 当前菜单 */
	currentElement: null,
	/** 触发菜单的事件 */
	action: function(menuId, options) {
		var menu = this.menus[menuId];
		if (this.currentElement) {
			Element.removeClassName(this.currentElement, "current");
		}

		var container = menu['container'];
		this.currentElement = container;
		Element.addClassName(this.currentElement, "current");
		
		var menuAction = (function(options) {
			return function() {
				this.actionPerformed(options); 
			};
		})(options || menu['task']['id']);
		
		menuAction = menuAction.bind(this);
		
		var navContainer = this.navContainer = $(this.navContainer);
		if (navContainer) navContainer.innerHTML = menu['nav'];	// 显示导航文字
		
		menuAction(); 
	},
	
	/**
	 * 展开菜单menuId的子菜单
	 * @param menuId 菜单id
	 * @returns
	 */
	expand: function(menuId, options) {
		var menu = this.menus[menuId];
		if (!menu['loaded']) {
			// 从后台加载菜单
			menu['loaded'] = true;
			this.load(menuId);
		}
		var container = menu['container'];
		Element.removeClassName(container, true ? "collapsed" : "expanded");
		Element.addClassName(container, true ? "expanded" : "collapsed");
		menu['expanded'] = true;
	},
	/**
	 * 折叠菜单menuId,隐藏其子菜单
	 * @param menuId
	 */
	collapse: function(menuId, options) {
		var menu = this.menus[menuId];
		var container = menu['container'];
		Element.removeClassName(container, "expanded");
		Element.addClassName(container, "collapsed");
		menu['expanded'] = false;
	},
	
	hidden: false,
	/** 关闭/打开菜单 */
	toggleContainer: function() {
		var rootContainer = $(this.rootContainer);
		rootContainer[this.hidden ? 'show' : 'hide']();
		this.hidden = !this.hidden;
		var e = $('mainContainer');
		if (e) e.style.left = (this.hidden ? 0 : rootContainer.offsetWidth + 1) + 'px';
	}
};