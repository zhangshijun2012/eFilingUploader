/** 布局管理 */
var Layout = {
	containers: {
		header: 'header',
		nav: 'nav',
		body: 'bodyContainer',
		'body.menu': 'menu',
		'body.separator': 'separator',
		'body.container': 'mainContainer',
		'body.container.header': 'mainHeader',
		'body.container.nav': 'mainNav',
		'body.container.body': 'mainBody',
		'body.container.body.context': 'mainContext',
		'body.container.statusBar': 'mainStatusBar',
		'body.container.footer': 'mainFooter',
		footer: 'footer',
		container: 'container',	// 正文显示区域
		'dataTable': 'dataTable',
		'dataHeader': 'dataHeader',
		'dataBody': 'dataBody'
	},
	/** 滚动条宽度，出现了滚动条，并且滚动条宽度在此值内，则重新计算其子节点宽度，以便重新显示滚动条 */
	SCROLL_WIDTH: 20,
	/**
	 * 如果element出现了横向滚动条，则有可能是因为其内部元素宽度100%对element重新计算滚动条.
	 * 主要针对IE7，element的html至少应该是<div><div></div></div>
	 * @param element
	 */
	resizeScroll: function(element) {
		// if (!Prototype.Browser.IE7) return;
		if (!element) element = $(this.containers["body.container.body"]);
		else if (!(element = $(element))) return;
		var body = element.firstDescendant();	// 内部的第一个子节点
		if (!body) return;
		body.style.width = 'auto';
		body.style.height = 'auto';
		var overflowX = (element.scrollWidth || 0) - element.clientWidth;
		var overflowY = (element.scrollHeight || 0) - element.clientHeight;
		if (overflowY > 0 && overflowX > 0 && overflowX <= this.SCROLL_WIDTH) {
			// 出现了竖向和横向滚动条
			body.style.width = (body.offsetWidth - overflowX) + 'px';
			var overflow = body.style.overflow;
			body.style.overflow = 'auto';
			overflowX = (body.scrollWidth || 0) - body.clientWidth;
			if (overflowX > 0) body.style.width = 'auto';	// 还是有滚动条		
			body.style.overflow = overflow;
		}
	},
	resizeBody: function() {
		var body = $(this.containers["body"]);
		var container = $(this.containers["body.container"]);
		var width = body.offsetWidth - $(this.containers["body.menu"]).offsetWidth;
		container.style.width = width + 'px';

		var body = $(this.containers["body.container.body"]);
		body.style.width = width + 'px';
		// var context = $(this.containers['body.container.body.context']);
		
		var height = container.offsetHeight;
		var statusBar = $(this.containers["body.container.statusBar"]);
		var footer = $(this.containers["body.container.footer"]);
		height = (height - body.offsetTop - statusBar.offsetHeight - footer.offsetHeight);
		// alert(height);
		if (height > 0) body.style.height = height + 'px';
		
		this.resizeScroll();
	},
	resize: function() {
		/*
		var height = document.body.offsetHeight;
		var clientHeight = document.documentElement.clientHeight;
		if (clientHeight && clientHeight != height) {
			// document.body.style.height = clientHeight + 'px';
		}
		
		var clientWidth = document.documentElement.clientWidth || document.body.clientWidth;
		var clientHeight = document.documentElement.clientHeight || document.body.clientHeight;
		*/
		
		var clientHeight = document.body.offsetHeight;
		var height = Math.max(300, clientHeight);	// 最小为300
		// css样式中position的bottom有BUG，在IE8及其以前未起作用，因此需要指定body的高度
		var body = $(this.containers["body"]);
		var footer = $(this.containers["footer"]);
		height = (height - body.offsetTop - footer.offsetHeight);
		body.style.height = height + 'px';
		
		this.resizeBody();
	}
};