/**
 * org.anywnyu.util.Grid
 * 依赖base.js
 * author: LuoGang
 */
/*------ GridHelper = Helper.GridHelper -----------------------------------------------*/
/**
 * 表格处理工具。
 * 主要是使表格可点击表头进行排序，固定表头.
 * 要处理的表格的形式最好是:
 * 	<table>
 *		<thead><tr><td></td></tr></thead>	此为表头,点击里面的表格将已被点击的列进行排序.必须
 *		<tbody><tr><td></td></tr></tbody>	数据表格:点击之后将按被点击的列进行排序.必须
 *					如果有多个表格则每个表格分别排序.
 *					如果有多个表格,并且需要对表格进行排序,则两个表格之间的将使用第1行的值进行比较
 *		<tfoot><tr><td></td></tr></tfoot>	tfoot不参与排序.非必须
 *	</table> 
 */
var GridHelper = {
	tagNames: {
		table: "table",
		thead: "thead",
		tbody: "tbody",
		tfoot: "tfoot",
		th: "th",
		tr: "tr",
		td: "td"
	},
	
	sortTypes: {
		ASC: "asc",
		DESC: "desc",
		isAsc: function(sortType) {
			if (Object.isUndefined(sortType) || StringHelper.isEmpty(sortType) || StringHelper.isTrue(sortType)) {
				return true;
			}
			return StringHelper.trim(sortType).toLowerCase() == this.ASC;
		},
		
		isDesc: function(sortType) {
			return !this.isAsc(sortType);
			/*
			if (Object.isUndefined(sortType)) {
				return false;
			}
			return StringHelper.isFalse(sortType) || (!StringHelper.isEmpty(sortType) 
				&& StringHelper.trim(sortType).toLowerCase() == this.DESC);
			*/
		}
	},
	
	/* 排序的图标 */
	sortIcon: {
		ASC: Base.template + "images/grid/sort_asc.gif",	// 升序图标
		DESC: Base.template + "images/grid/sort_desc.gif",	// 降序图标
		icon: ElementHelper.createElement("img"),
		get: function(sortType) {
			var src = GridHelper.sortTypes.isAsc(sortType) ? this.ASC : this.DESC;
			this.icon.src = src;
			return this.icon;
		}
	},
	
	arrows: {	// 箭头，使用css样式控制，只在IE中有效
		asc: "5",
		
		desc: "6",
		
		element: ElementHelper.createElement("b", {
			innerHTML: "5"
		}, null, function(e) {
			e.style.cssText = "padding: 0 0 2px 0; margin: 0 0 2px 0; width: 10px; height: 12px; "
				+ "overflow: hidden; font-family: webdings; font-size: 11px;";
		}),
		
		get: function(sortType) {
			this.element.innerHTML = GridHelper.sortTypes.isAsc(sortType) ? this.asc : this.desc;
			return this.element;
		}
	},
	
	isTable: function (e) {
		return ElementHelper.isTagName(e, this.tagNames.table);
	},
	isTHead: function (e) {
		return ElementHelper.isTagName(e, this.tagNames.thead);
	},
	isTBody: function (e) {
		return ElementHelper.isTagName(e, this.tagNames.tbody);
	},
	isTFoot: function (e) {
		return ElementHelper.isTagName(e, this.tagNames.tfoot);
	},
	isTh: function (e) {
		return ElementHelper.isTagName(e, this.tagNames.th);
	},
	isTr: function (e) {
		return ElementHelper.isTagName(e, this.tagNames.tr);
	},
	isTd: function (e) {
		return ElementHelper.isTagName(e, this.tagNames.td);
	},
	
	/**
	 * 保存表格上次排序的状态:
	 * 	sortCaches[grid] = {
	 *		sortIndex: -1,						// 排序的列
	 * 		sortClass:	null,					// 排序列的数据类型,暂未使用
	 *		sortType: GridHelper.sortTypes.ASC,	// 排序类型
	 *		caches: []							// 记录哪些列已经排序
	 * 	}
	 */
	sortCaches: [],
	
	/** 
	 * 对表格grid按第columnIndex列进行排序
	 * grid: 要排序的表格，只能为table, thead, tbody, tfoot之一
	 * options: {
	 * 	sortIndex:	以sortIndex列的数据进行排序
	 * 	sortClass:	排序列的数据类型
	 * 	sortType:	排序方式.true为升序,false为降序.若该参数为空,且该列已经排序,则按照该列的排序进行相反的排序.否则默认为升序.
	 * }
	 */
	sort: function(grid, options) {
		grid = $(grid);
		grid = this.isTable(grid) ? grid.tBodies[0] : grid;
		if (!(this.isTHead(grid) || this.isTBody(grid) || this.isTFoot(grid))) {
			// 只能对表格中的thead, tbody, tfoot进行排序
			return false;
		}
		
		options = NumberHelper.isNumber(options) ? { sortIndex: NumberHelper.intValue(options) } : (options || {});
		options.sortIndex = NumberHelper.intValue(options.sortIndex, -1);
		if (options.sortIndex < 0) {
			// 未指定排序的列
			return;
		}
		
		//		sortIndex, sortClass, sortType
		
		if (!this.sortCaches[grid]) {
			this.sortCaches[grid] = {
				sortIndex: -1,
				sortClass:	null,
				sortType: GridHelper.sortTypes.ASC,
				caches: []	//暂未使用
			};
		}
		
		var asc = true;
		if (StringHelper.isEmpty(options.sortType)) {
			if (this.sortCaches[grid].sortIndex == options.sortIndex) {
				// 用来排序的列与上一次排序的列一致
				asc = !this.sortTypes.isAsc(this.sortCaches[grid].sortType);
			}
		} else {
			asc = this.sortTypes.isAsc(options.sortType);
		}
		options.sortType = asc ? this.sortTypes.ASC : this.sortTypes.DESC;
		
		var e = EventHelper.getElement(); // 触发事件的对象
		if (!this.sortCaches[grid].caches[options.sortIndex]) {
			// 该列尚未排过序,则将其innerHTML重新设置过,主要是为了去掉前后的空白
			if (e.innerHTML != e.innerHTML.trim()) {
				e.innerHTML = e.innerHTML.trim();
			}
			
			this.sortCaches[grid].caches[options.sortIndex] = true;
		}
		
		this.sortCaches[grid].sortIndex = options.sortIndex;
		this.sortCaches[grid].sortType = options.sortType;		// 排序方式
		this.sortCaches[grid].sortClass = options.sortClass;	// 排序的数据类型
		
		var rows = grid.rows;	// 所有需要排序的行
		var length = rows.length;
		var list = new this.util.RowList(options);
		for (var i = 0; i < length; i++) {
			list.append(rows[i]);
		}
		list.sort();	// 排序
		
		rows = list.getRows();
		for (var i = 0; i < length; i++) {
			grid.appendChild(rows[i]);
		}
		try {
			e.appendChild(this.sortIcon.get(options.sortType));
		} catch (error) {
		}
	},
	
	/**
	 * 初始化一个表格,使之可进行排序
	 * thead: 触发事件的行(可以为thead,tbody,tbale对象)
	 * tbody: 点击之需要排序的表格(可以为thead,tbody,tbale对象)
	 * options: {
	 * 	noSortIndex: [] //点击后不需要排序的列
	 * }
	 */
	initialize: function(thead, tbody, options) {
		thead = $(thead);
		if (Object.isUndefined(tbody) || !tbody) {
			if (!this.isTable(thead)) {
				return;
			}
			tbody = thead.tBodies[0];
			thead = thead.tHead;
		} else {
			// tbody = $(tbody);
		}
		
		// alert(thead + " " + tbody);
		
		if (!(this.isTHead(thead) || this.isTBody(thead) || this.isTFoot(thead)) ||
			!(this.isTHead(tbody) || this.isTBody(tbody) || this.isTFoot(tbody))) {
			return;
		}
		if (this.sortCaches[$(tbody)]) {
			this.sortCaches[$(tbody)] = null;
		}
		
		var rows = thead.rows;
		var celss;
		options = options ? options : {};
		if (!options.noSortIndex) {
			// 不需要排序的列
			// options.noSortInndex = [];
		}
		
		for (var row = 0, length = rows.length; row < length; row++) {
			cells = rows[row].cells;
			for (var i = 0, l = cells.length; i < l; i++) {
				if (StringHelper.indexInArray(i, options.noSortIndex) >= 0) {
					continue;
				}
				Element.addClassName(cells[i], "hand");
				// cells[i].setAttribute("columnIndex", i);	//主要是用于自动添加事件时进行传值
				// 使用闭包防止数据无法传递
				
				// Event.stopObserving(this.buttons[p], e, buttons[p].clearHandle[e]);
					
				Event.observe(cells[i], "click", (function (tbody, p) {
					return (function() { if (tbody && Object.isElement(tbody) && Object.isElement(tbody.parentNode)) { GridHelper.sort(tbody, p); } })
				})(tbody, i));
			}
		}
	},
	
	/**
	 * 一些实用类
	 */
	util: {
		/**
		 * 可比较的行
		 * row: 行,
		 * sortIndex: 用于比较大小的列,
		 * sortClass: 比较的类型.0:字符串;1:数字;2:日期.
		 */
		ComparableRow: function(row, options) {
			row = GridHelper.isTr(row) ? row : null;	// 该行元素
			options = options ? options : {};
			// this.row = GridHelper.isTr(row) ? row : null;	// 该行元素
			options.sortIndex = NumberHelper.isNumber(options.sortIndex) ? 
				Math.max(0, NumberHelper.intValue(options.sortIndex)) : 0;	// 索引
			options.sortClass = NumberHelper.intValue(options.sortClass, -1);	// -1根据单元格的className来
			
			
			this.getRow = function() {
				return row;
			}
			this.getOptions = function() {
				return options;
			}
			
			var o = this;
			this.getSortValue = function() {
				if (row == null) {
					return null;
				}
				var cell = row.cells[options.sortIndex];
				if (cell.getAttribute('sortValue') != null) {
					return cell.getAttribute('sortValue');
				}
				var sortClass = options.sortClass;
				if (sortClass == -1) {
					if (Element.hasClassName(cell, "number")) {
						// 数字
						sortClass = 1;
					} else if (Element.hasClassName(cell, "date")) {
						// 日期
						sortClass = 2;
					}
				}
				
				var cell = row.cells[options.sortIndex];
				var text = ElementHelper.getTextContent(cell);
				switch (sortClass) {
					case 1:
						// 数字
						return NumberHelper.doubleValue(text);
						// break;
					case 2:
						var date = DateHelper.parse(text);
						return date ? date : text;
						// break;
					case 3:
						break;
				}
				
				return text;
			}
			
			this.compareTo = function(anotherRow) {
				if (this === anotherRow) {
					return 0;
				}
				if (!anotherRow.getSortValue) {
					return 1;
				}
				var value = o.getSortValue();
				var anotherValue = anotherRow.getSortValue();
				return value < anotherValue ? -1 : value > anotherValue ? 1 : 0;
			}
		},
		
		RowList: function(options) {
			options = options ? options : {};
			// this.row = GridHelper.isTr(row) ? row : null;	// 该行元素
			options.sortIndex = NumberHelper.isNumber(options.sortIndex) ? 
				Math.max(0, NumberHelper.intValue(options.sortIndex)) : 0;	// 索引
			options.sortClass = NumberHelper.intValue(options.sortClass, -1);	// -1根据单元格的className来
			
			this.getOptions = function() {
				return options;
			}
			
			var o = this;
			var rows = new Array();
			
			this.getRows = function () {
				return rows;
			}
			/**
			 * 数量
			 */
			this.size = function() {
				return rows.length;
			}
			
			/**
			 * 增加一行元素
			 */
			this.append = function(row) {
				if (GridHelper.isTr(row)) {
					return rows.push(row);
				}
			}
			
			/**
			 * 删除元素
			 */
			this.remove = function(row, howmany) {
				var rmoved = [];
				if (GridHelper.isTr(row)) {
					// row为一个元素
					for (var i = 0, l = o.rows.length; i < l; i++) {
						if (rows[i] == row) {
							rmoved.push(rows.splice(i, 1));
							i--;
							l--;
						}
					}
				} else {
					row = NumberHelper.intValue(row);
					howmany = NumberHelper.intValue(howmany, 1);
					rmoved = rows.splice(row, howmany);
				}
				
				return removed;
				
			}
			
			this.sort = function() {
				rows.sort(function(value, anotherValue) {
					//try {
						return new GridHelper.util.ComparableRow(value, options).compareTo(
									new GridHelper.util.ComparableRow(anotherValue, options));
					//} catch (error) {
						// alert(error.message);
					//}
					
					return value < anotherValue ? -1 : value > anotherValue ? 1 : 0;
				});
				
				if (GridHelper.sortTypes.isDesc(options.sortType)) {
					// 降序
					rows.reverse();
				}
			}
		}
	}
};

Helper.add("GridHelper", GridHelper);