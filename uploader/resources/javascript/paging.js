/** 分页信息处理类 */
var Paging = {
	service: null,
	containers: Layout.containers,
	/** 多选框处理 */
	selector: function(selector, cell, value, service, values) {
		var service = service || this.service;
		Element.addClassName(cell, 'txtCenter');
		cell.style.width = '20px';
		if (Object.isFunction(selector)) {
			selector.apply(service, [cell, value, values]);
		} else {
			if (selector === true) {
				if (!value) cell.innerHTML = '<input type="checkbox" onclick="Main.service[\'checkAll\'](this.checked)" id="checkAllSelector" />';
				else cell.innerHTML = '<input type="checkbox" onclick="Main.service[\'check\'](this)" name="ids" value="' + value + '" />';
			} else if (Object.isString(selector)) {
				cell.innerHTML = service.getText(selector, value || '');
			} else {
				ElementHelper.setAttributes(cell, selector['attributes']);
				if (html = selector['value'] || selector['html'] || selector['innerHTML']) 
					cell.innerHTML = service.getText(html, value || ''); 
			}
		}
		
	},
	/**
	 * 列出数据列表
	 * @param data 对应后台的PagingEntity类
	 */
	list: function(data, service) {
		this.previousService = this.service;
		this.service = service = service || Main.service;
		service.selectorData = { };	// 用于选择的数据
		service.selectorCount = 0;
		service.selected = [];
		if (Object.isFunction(service['list'])) {
			return service['list'](data);
		}
		service.data = data = data || { };
		// 显示分页信息
		if (Object.isFunction(service['pagingRenderer'])) {
			service['pagingRenderer'](data);
		} else {
			this['pagingRenderer'](data);
		}
		
		var containers = service['containers'] || { };
		var dataTable = $(containers['dataTable'] || this.containers['dataTable']);
		var dataHeader = $(containers['dataHeader'] || this.containers['dataHeader']);
		var dataBody = $(containers['dataBody'] || this.containers['dataBody']);

		if (!dataTable) {
			if (service['createDataTable']) {
				dataTable = service['createDataTable']();
				dataHeader = $(dataTable.tHead);
				dataBody = $(dataTable.tBodies[0]);
			} else {
				var container = $(containers['container'] || this.containers['container']);
				container.empty();
				dataTable = ElementHelper.createElement("table", null, container);			
				dataTable.id = containers['dataTable'] || this.containers['dataTable'];
				Element.addClassName(dataTable, 'list');
	
				dataHeader = ElementHelper.createElement("thead", null, dataTable);			
				dataHeader.id = containers['dataHeader'] || this.containers['dataHeader'];
				
				dataBody = ElementHelper.createElement("tbody", null, dataTable);			
				dataBody.id = containers['dataBody'] || this.containers['dataBody'];
				Element.addClassName(dataBody, 'nowrap');
				/*
				dataHeader = $(dataHeader);
				dataBody = $(dataBody);
				*/
			}
		}


		var selector = service['selector'];
		
		if (this.previousService != this.service || dataHeader.rows.length < 1) {
			// 第一次调用需要创建表头
			while (dataHeader.rows.length > 1) dataHeader.deleteRow(0);

			var headers = service['dataHeader'];
			
			if (Object.isFunction(headers)) {
				headers(data);
			} else {
				var row = dataHeader.insertRow();
				/*
				var dataLineNumber = service['dataLineNumber'];	// 是否显示行号
				var columns = dataLineNumber ? 1 : 0;
				var dataSelector = service['dataSelector'];		// 是否显示多选框
				columns += (dataLineNumber ? 1 : 0);
				*/
				var columns = 0;
				// 显示行号
				
				// 显示多选框
				if (selector) {
					var cell = ElementHelper.createElement("td", null, row);
					this.selector(selector, cell, null, service);
					columns++;
				}
				headers.each(function(header) {
					columns++;
					// var cell = row.insertCell();
					var cell = ElementHelper.createElement("td", null, row);
					if (Object.isString(header)) {
						cell.innerHTML = service.getText(header);
					} else {
						ElementHelper.setAttributes(cell, header['attributes']);
						if (html = header['value'] || header['html'] || header['innerHTML']) 
							cell.innerHTML = service.getText(html); 
					}
				});
				
				service['dataColumnSize'] = service['dataColumnSize'] || columns;
			}
		}
		
		// 创建数据
		while (dataBody.rows.length > 0) dataBody.deleteRow(0);
		var dataHandler = service['dataHandler'];
		if (Object.isFunction(dataHandler)) {
			return dataHandler(data);
		}
		var columns = service['dataColumnSize'] || dataHeader.rows[0].cells.length;
		if (!data['list']) {
			// 没有数据
			var row = dataBody.insertRow();
			var cell = row.insertCell();
			cell.innerHTML = Object.isUndefined(data['pageIndex']) ? '' : service.getText('global.nothing');
			cell.colSpan = columns;
			Element.addClassName(cell, 'nothing');
			return;
		}

		if (selector) columns--;
		
		var me = this;
		data.list.each(function(values, index) {
			var row = dataBody.insertRow();
			Element.addClassName(row, index % 2 == 1 ? 'even' : 'odd');
			// 默认情况下,values第一列数据为id
			var id;// = values.shift();
			if (service.dataFilter) values = service.dataFilter(values);	// 处理数据
			if (!Object.isUndefined(service.idIndex) && service.idIndex !== null) id = values[service.idIndex];	// 指定第indexId列为id,此时不删除id列
			else id = values.shift();

			if (selector) {
				var cell = row.insertCell();
				me.selector(selector, cell, id, service, values);
			}
			
			var cols = 0;
			values.each(function(value, index){
				cols++;
				if (index >= columns) throw $break;
				var cell = row.insertCell();
				var handler = dataHandler[index];
				var renderer, attributes;
				if (handler && Object.isFunction(handler)) renderer = handler;
				else if (handler) {
					renderer = handler['renderer'];
					attributes = handler['attributes'];
					handler = handler['handler'];
				}
				ElementHelper.setAttributes(cell, attributes);
				if (renderer) { // renderer属性只能是函数
					renderer.apply(service, [cell, value, index, values, data, id]);
				} else {
					if (index == 0 && !handler) {
						if (Object.isUndefined(service.idHandler)) {
							handler = (function(id) {
								this.view(id);
							}).bind(service, id);
						} else {
							handler = service.idHandler;
						}
					}
					if (handler) { // handler属性只能是函数,为该列函数定义的事件
						var a = ElementHelper.createElement("a", { href: "#", innerHTML: service.getText(value) }, cell);
						Event.observe(a, "click", (function(handler, id, cell, value, index, values, element) {
							handler.apply(this, [id, cell, value, index, values, element]); 
							return false; 
						}).bind(service, handler, id, cell, value, index, values, a));
					} else {
						if (Object.isNumber(value)) {
							// 数字
							value = NumberHelper.format(value);
							$(cell).addClassName('number');
						} else if (Object.isDate(value)) {
							// 日期
							value = Date.format(value);
							$(cell).addClassName('date');
						}
						cell.innerHTML = !value ? '&nbsp;' : (handler ? ('<a href="#">' + service.getText(value) + '</a>') : value);
						// cell.innerHTML = value;
						// cell.innerHTML = service.getText(value) || '';
					}
				}
			});
			
			// 还原id
			values.insert(0, id);
			service.selectorData[id] = values;
			service.selectorCount = service.selectorCount + 1;
			
			for (; cols < columns; cols++) {
				var cell = row.insertCell();
				var handler = dataHandler[cols];
				var renderer, attributes;
				if (handler && Object.isFunction(handler)) renderer = handler;
				else if (handler) {
					renderer = handler['renderer'];
					attributes = handler['attributes'];
					handler = handler['handler'];
				}
				ElementHelper.setAttributes(cell, attributes);
				if (renderer) { // renderer属性只能是函数
					renderer.apply(service, [cell, id, cols, values, data, id]);
				}
			}
		});
		data = null;

		Layout.resizeScroll();
	},
	
	pagingContainers: {
		'total': 'pagingEntity.total',
		'size': 'pagingEntity.size',
		'maxResults': 'pagingEntity.maxResults',
		'pageCount': 'pagingEntity.pageCount',
		'pageIndex': 'pagingEntity.pageIndex',
		'previous': 'pagingEntity.previous',
		'next': 'pagingEntity.next',
		'first': 'pagingEntity.first',
		'last': 'pagingEntity.last'
	},
	pagingEntity: {
		pageCount: 0,
		pageIndex: 1,
		maxResults: 20,
		size: 0,
		total: 0
	},
	/* 显示分页信息 */
	pagingRenderer: function(pagingEntity) {
		var value;
		for (var container in this.pagingContainers) {
			value = pagingEntity[container];
			if (Object.isUndefined(value)) continue;
			var ele = $(this.pagingContainers[container]);
			if (ElementHelper.isTagName(ele, 'input')) {
				ele.value = value || '';
			} else {
				ele.innerHTML = value || '';
			}
			this.pagingEntity[container] = value;
		}
		Element[pagingEntity['pageIndex'] <= 1 ? 'addClassName' : 'removeClassName'](this.pagingContainers['previous'], 'disabled');
		Element[pagingEntity['pageIndex'] <= 1 ? 'addClassName' : 'removeClassName'](this.pagingContainers['first'], 'disabled');

		Element[pagingEntity['pageIndex'] >= pagingEntity['pageCount'] ? 'addClassName' : 'removeClassName'](this.pagingContainers['next'], 'disabled');
		Element[pagingEntity['pageIndex'] >= pagingEntity['pageCount'] ? 'addClassName' : 'removeClassName'](this.pagingContainers['last'], 'disabled');
	},
	/** 跳转到第一页 */
	first: function() {
		if (this.pagingEntity['pageIndex'] <= 1) return false;
		this.go(1, this.pagingEntity['maxResults']);
	},
	/** 跳转到最后一页 */
	last: function() {
		if (this.pagingEntity['pageIndex'] >= this.pagingEntity['pageCount']) return false;
		this.go(this.pagingEntity['pageCount'], this.pagingEntity['maxResults']);
	},
	/** 跳转到上一页 */
	previous: function() {
		if (this.pagingEntity['pageIndex'] <= 1) return false;
		this.go(this.pagingEntity['pageIndex'] - 1, this.pagingEntity['maxResults']);
	},
	/** 跳转到下一页 */
	next: function() {
		if (this.pagingEntity['pageIndex'] >= this.pagingEntity['pageCount']) return false;
		this.go(this.pagingEntity['pageIndex'] + 1, this.pagingEntity['maxResults']);
	},
	/**
	 * 转到第pageIndex页，每页maxResults条数据
	 * @param pageIndex
	 * @param maxResults
	 */
	go: function(pageIndex, maxResults) {
		var service = this.service;
		if (Object.isFunction(service['go'])) {
			service.go(pageIndex, maxResults);
		} else {
			if (pageIndex) $(this.pagingContainers['pageIndex']).value = Math.max(1, Math.min(this.pagingEntity['pageCount'], pageIndex));
			if (maxResults) $(this.pagingContainers['maxResults']).value = Math.max(1, maxResults);
			pageIndex = NumberHelper.intValue($(this.pagingContainers['pageIndex']).value);
			maxResults = NumberHelper.intValue($(this.pagingContainers['maxResults']).value);
			if (service.queryJSONParameters) {
				// 分页参数
				service.queryJSONParameters['pageIndex'] = pageIndex;
				service.queryJSONParameters['maxResults'] = maxResults;
			}
			service.query({ 'paging': true });
		}
	}
};