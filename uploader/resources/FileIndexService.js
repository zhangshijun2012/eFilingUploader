/** File的一些JS处理函数 */
var FileIndexService = {
	namespace : window['UPLOADER_ROOT'] || window['SERVER_ROOT'] || '',
	/**
	 * 打开一个新窗口
	 * 
	 * @param url
	 * @param options
	 */
	open : function(url, options) {
		options = Object.extend({
			'name': '',
			'arguments': '' // 'width=800, height=600, top=100, left=100'
		}, options || {});
		return window.open(url, options['name'], options['arguments']);
	},
	/**
	 * 查看文件
	 * 
	 * @param id
	 * @param options
	 */
	view : function(id, options) {
		var url = this.URL_VIEW + '?id=' + id;
		return this.open(url, options);
	},
	/** 是否禁用下载功能 */
	downloadDisabled: false,
	/**
	 * 下载文件
	 * 
	 * @param id
	 * @param options
	 */
	download : function(id, options) {
		if (this.downloadDisabled) {
			// 不允许下载则至view
			return this.view(id, options); 
		}
		var url = this.URL_DOWNLOAD + '?id=' + id;
		return this.open(url, options);
	},
	/**
	 * 将参数转换为查询字符串
	 * 
	 * @param parameters 字符串或json对象
	 * @returns {String}
	 */
	toQueryString : function(parameters) {
		if (Object.isString(parameters)) return parameters;
		var queryString = '';
		for ( var p in parameters) {
			var key = encodeURIComponent(p);
			var values = parameters[p];
			if (Object.isString(values)) values = [ values ];
			for ( var i = 0, l = values.length; i < l; i++) {
				var value = encodeURIComponent(values[i]);
				if (value) queryString += ('&' + key + '=' + value);
			}
		}
		return queryString ? queryString.substring(1) : '';
	},
	/**
	 * 显示图片文件列表
	 * 
	 * @param parameters 查询参数
	 * @param element 显示元素
	 */
	show : function(parameters, element) {
		this.parameters = parameters;
		element = $(element);
		if (!element) {
			// alert(this.URL_SHOW + '?' + this.toQueryString(parameters));
			// open窗口进行显示
			// alert(this.toQueryString(parameters));
			return this.open(this.URL_SHOW + '?' + this.toQueryString(parameters));
		} else if (element == window) {
			// 在本窗口打开图片查看
			window.location = this.URL_SHOW + '?' + this.toQueryString(parameters);
		} else {
			// 在element元素上显示结果
			var $this = this;
			// alert($this.URL_QUERY + ',' + AjaxHelper.request);
			AjaxHelper.request($this.URL_QUERY, {
				parameters : parameters,
				onSuccess : function(xhq, options) {
					// alert(xhq.responseText);
					var json = String.toJSON(xhq.responseText);
					$this.data = json;
					$this.pageIndex = json['pageIndex'];
					$this.maxResults = json['maxResults'];
					$this.list(element);
				}
			});
		}
	},

	/**
	 * 得到list方法中data的HTML代码
	 * 
	 * @param data
	 * @returns {String}
	 */
	htmlData : function(data) {
		var html = this.LIST_HTML;
		var rexp;
		var value;
		for ( var p in data) {
			rexp = new RegExp('\\${\\s*' + p + '\\s*}', "gi");
			value = data[p];
			if (p == 'fileSize') value = Number.formatFileSize(value);
			html = html.replace(rexp, value);
		}
		rexp = new RegExp('\\${\\s*\\w+\\s*}', "gi");
		html = html.replace(rexp, '');
		return html;
	},
	DEFAULT_MAX_RESULTS: 20,	// 默认显示的图片数量
	MORE_CONTAINER: 'more',
	pageIndex: 0,
	maxResults: 0,
	/**
	 * 显示图片列表
	 * 
	 * @param element 显示的元素
	 * @param list 图片数据,如果省略则调用this.data
	 * @param options 可选配置
	 * 	insert 是否将图片在列在现有数据的前面
	 */
	list : function(element, list, options) {
		options = options || { };
		if (!list) {
			// 调用query方法进行执行,需要判断是否有分页
			list = this.data['list'];
			var more = $(options['moreContainer'] || this.MORE_CONTAINER);
			if (more) {
				if (this.data['pageCount'] > this.data['pageIndex']) {
					// 还有要显示的图片
					more.style.display = '';
				} else {
					more.style.display = 'none';
				}
			}
		}
		element = $(element);
		if (!list || !element) return;
		this.container = element;
		var insert = options['insert'];
		if (insert) {
			// 在现有图片之前插入list中的数据
			insert = element.firstChild;
		}
		for ( var i = 0, length = list.length; i < length; i++) {
			var data = list[i];
			var div = document.createElement("div");
			div.id = 'preview' + data['id'];
			if (insert) {
				element.insertBefore(div, insert);
			} else {
				element.appendChild(div);
			}
			var html = this.htmlData(data);
			if (data['preview']) {
				html = html.replace('<img src="' + this.URL_PREVIEW + '?id=' + data['id'] + '" />', '<img src="' + data['preview'] + '" />');
			}
			div.innerHTML = html;
		}
	},
	listX : function(element, list, options) {
		if (!list) list = this.data;
		element = $(element);
		if (!list || !element) return;
		this.container = element;
		var data;
		var first = 0;
		var max = list.length;
		options = options || { };
		var pageIndex = options['pageIndex'];
		if (pageIndex) {	// 有页数
			var maxResults = options['maxResults'] || this.maxResults || this.DEFAULT_MAX_RESULTS || 20;
			this.pageIndex = pageIndex;
			this.maxResults = maxResults;
			first = (pageIndex - 1)* maxResults;
			if (max > first + maxResults) {
				// 还有要显示的图片
				max = first + maxResults;
				$(options['moreContainer'] || this.MORE_CONTAINER).style.display = '';
			} else {
				$(options['moreContainer'] || this.MORE_CONTAINER).style.display = 'none';
			}
		}
		var insert = options['insert'];
		if (insert) {
			// 在现有图片之前插入list中的数据
			insert = element.firstChild;
		}
		for ( var i = first; i < max; i++) {
			data = list[i];
			var div = document.createElement("div");
			div.id = 'preview' + data['id'];
			if (insert) {
				element.insertBefore(div, insert);
			} else {
				element.appendChild(div);
			}
			var html = this.htmlData(data);
			if (data['preview']) {
				html = html.replace('<img src="' + this.URL_PREVIEW + '?id=' + data['id'] + '" />', '<img src="' + data['preview'] + '" />');
			}
			div.innerHTML = this.htmlData(data);
		}
	},
	/** 显示下一页的图片预览 */
	next: function(element) {
		var parameters = this.parameters || { };
		parameters['pageIndex'] = this.pageIndex + 1;
		parameters['maxResults'] = this.maxResults;
		this.show(parameters, element || this.container);
		/*
		this.list(element || this.container, this.data, {
			pageIndex: this.pageIndex + 1,
			maxResults: this.maxResults
		});
		*/
	},
	/** 初始化一些静态参数 */
	init : function() {
		this.URL_UPLOAD = this.namespace + 'upload.do';
		this.URL_SAVE = this.namespace + 'save.do';
		this.URL_SHOW = this.namespace + 'show.do';
		this.URL_VIEW = this.namespace + 'view.do';
		this.URL_READ = this.namespace + 'read.do';
		this.URL_DOWNLOAD = this.namespace + 'download.do';
		this.URL_PREVIEW = this.namespace + 'preview.do';
		this.URL_QUERY = this.namespace + 'query.do';

		/* 显示预览时的html */
		this.LIST_HTML = '' // '<div id="preview${ id }">'
				+ '<p id="img${ id }"><a href="javascript: void(0)" onclick="FileIndexService.view(\'${ id }\')">'
				+ '<img src="' + this.URL_PREVIEW + '?id=${ id }" /></a></p>'
				+ '<h3 id="title${ id }"><a href="javascript: void(0)" onclick="FileIndexService.download(\'${ id }\')">${ fileTitle }</a></h3>'
				+ '<p id="name${ id }" class="gray">${ fileName } (${ fileSize })</p>'
				+ '<span class="gray" id="keyWords${ id }">${ keywords }</span>'
				+ '<span class="gray" id="text${ id }">${ text }</span>';
		// + '</div>';
	}
};
FileIndexService.init();