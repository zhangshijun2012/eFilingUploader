/**
 * 用于显示日历控件
 */
var CalendarHelper = {
	container: null,		// 显示日历控件的元素
	mainContainer: null,	// 显示日历控件的元素
	yearContainer: null,	// 显示年的元素
	monthContainer: null,	// 显示月的元素
	dayContainer: null,		// 显示日的元素
	dayTable: null,			// 显示日的元素
	
	timeContainer: null,	// 显示时间的元素
	hourContainer: null,	// 显示时间的元素
	minuteContainer: null,	// 显示时间的元素
	secondContainer: null,	// 显示时间的元素
	millisecondContainer: null,	// 显示时间的元素
	
	hourElement: null,		// 显示时间的元素
	minuteElement: null,	// 显示时间的元素
	secondElement: null,	// 显示时间的元素
	millisecondElement: null,	// 显示时间的元素
	
	element: null, 	// 点击此元素之后显示日历控件.当选中某个日期之后将改变此元素的值
	date: null,		// 当前日期
	today: null,	// 今天
	
	DAYS: ["日", "一", "二", "三", "四", "五", "六"],
	COLUMNS: 7,		// 列数
	ROWS: 6,		// 行数
	MONTHS: ["\u4e00\u6708", "\u4e8c\u6708", "\u4e09\u6708", "\u56db\u6708", "\u4e94\u6708", "\u516d\u6708", "\u4e03\u6708", "\u516b\u6708", "\u4e5d\u6708", "\u5341\u6708", "\u5341\u4e00\u6708", "\u5341\u4e8c\u6708"],
	//DAYS: ["日", "一", "二", "三", "四", "五", "六"],
	classNames: ["sunday", "", "", "", "", "", "saturday"],
	visible: false,
	
	// format: DateHelper.defaultFormat,	// 默认显示的日期格式
	options: null,
	
	hideOnClickDocument: true,		// 是否在点击body时隐藏日历
	/**
	 * 当点击日历自身或相应的element时触发此事件
	 * 防止事件冒泡传递给了body从而导致日历被隐藏
	 */
	clickSelf: function(e) {
		this.hideOnClickDocument = false;
	},
	/**
	 * 更改年份
	 */
	clickYear: function() {
		if (this.yearInputField) return;
		this.yearContainer.innerHTML = '';
		var yearField = ElementHelper.createElement('input', { 
			value: this.date.getFullYear(), style: { width: '32px', height: '16px' } }, this.yearContainer);
		yearField.focus();
		this.yearInputField = yearField;
		var onblur = (function(field) {
			return function() {
				var year = NumberHelper.isNumber(field.value);
				if (!year) {
					alert('请输入正确的年份');
					field.focus();
					return;
				}
				year = NumberHelper.intValue(field.value);
				this.yearContainer.innerHTML = year;
				this.yearInputField = null;

				if (year != this.date.getFullYear()) {
					var date = this.date.clone();
					date.setFullYear(year);
					this.set(date);
				}
			};
		})(yearField);
		onblur = onblur.bind(this);
		Event.observe(yearField, "blur", onblur);
		Event.observe(yearField, "keydown", (function() {
			var keyCode = EventHelper.keyCode();
			switch(keyCode) {
			case Event.KEY_RETURN:
				onblur();
				break;
			case Event.KEY_TAB:
				onblur();
				this.clickMonth();
				Event.stop(EventHelper.get());
				break;
			}
		}).bind(this));
	},

	/**
	 * 更改月份
	 */
	clickMonth: function() {
		if (this.monthInputField) return;
		this.monthContainer.innerHTML = '';
		var monthInputField = ElementHelper.createElement('input', { 
			value: this.date.getMonth() + 1, style: { width: '16px', height: '16px' } }, this.monthContainer);
		monthInputField.focus();
		this.monthInputField = monthInputField;
		var onblur = (function(field) {
			return function() {
				month = NumberHelper.intValue(field.value);
				if (!month || month < 1 || month > 12) {
					alert('请输入正确的年份');
					field.focus();
					return;
				}				
				this.monthContainer.innerHTML = StringHelper.leftPad(month, 2, '0');
				this.monthInputField = null;

				month--;
				if (month != this.date.getFullYear()) {
					var date = this.date.clone();
					date.setMonth(month);
					this.set(date);
				}
			};
		})(monthInputField);
		
		onblur = onblur.bind(this);
		Event.observe(monthInputField, "blur", onblur);
		Event.observe(monthInputField, "keypress", function() {
			if (EventHelper.isEnter()) {
				onblur();
			}
		});
	},
	initialize: function(options) {
		this.options = {
			format: DateHelper.defaultFormat,	// 要显示的日期格式
			formats: null,						// 指定可能的所有日期格式.将用此格式解析日期字符串转为日期
			hideOnInput: true,					// 选定日期之后是否自动隐藏
			hideOnClear: false,					// 清除日期之后是否自动隐藏
			inputOnReturnToday: true,			// 是否在点击今天的按钮时向输入框输入时间
			hideOnReturnToday: true,			// 点击今天之后是否自动隐藏.inputOnReturnToday为true才有效
			time: false							// 是否需要显示时间
		};
		Object.extend(this.options, options || { });
		
		
		if (this.element) {
			if (Object.isUndefined(this.element.value)) this.currentDate = this.element.innerHTML;
			else this.currentDate = this.element.value;
			this.currentDate = this.currentDate ? this.parse(this.currentDate) : null;
		}
		
		this.today = new Date();
		this.date = this.currentDate ? this.currentDate.clone() : (this.date ? this.date : this.today);
		
		
		if (!this.container) {
			this.container = ElementHelper.createElement("div", { className: "calendar-dialog" }, document.body);
			
			this.mainContainer = ElementHelper.createElement("div", { className: "container" }, this.container);
			ElementHelper.createElement("iframe", { frameborder: 0, width: "200%", height: "200%" }, this.container);
			var header = ElementHelper.createElement("div", { className: "header" }, this.mainContainer);
			
			var e = ElementHelper.createElement("span", { className: "floatLeft" }, header);
			var a = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "上一年", 
				innerHTML: "<img src=\"" + Base.template + "images/page/page-first.gif\" width=11 height=11 align=absmiddle border=0 />" }, e);
			Event.observe(a, "click", (function() { this.add(Date.YEAR, -1); }).bind(this));
			
			a = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "上一月", 
				innerHTML: "<img src=\"" + Base.template + "images/page/page-prev.gif\" width=11 height=11 align=absmiddle border=0 />" }, e);
			Event.observe(a, "click", (function() { this.add(Date.MONTH, -1); }).bind(this));
			
			this.yearContainer = ElementHelper.createElement("span", { className: "txtRight", href: "javascript: void(0);", 
				style: { width: "20px", textDecoration: "underline"  }, innerHTML: this.today.getFullYear() }, e);

//			a = ElementHelper.createElement("a", { className: "txtRight", href: "javascript: void(0);", 
//				style: { width: "20px", textDecoration: "underline" }, innerHTML: this.today.getFullYear() }, this.yearContainer);
			
			Event.observe(this.yearContainer, "click", (function() { 
				this.clickYear();
			}).bind(this));
			
			ElementHelper.createElement("span", { innerHTML: "年", className: "separator" }, e);
			
			this.monthContainer = ElementHelper.createElement("span", { className: "txtRight", href: "javascript: void(0);", 
				style: { width: "10px", textDecoration: "underline"  }, innerHTML: this.today.getMonth() + 1 }, e);

			Event.observe(this.monthContainer, "click", (function() { 
				this.clickMonth();
			}).bind(this));
			
			ElementHelper.createElement("span", { innerHTML: "月", className: "separator" }, e);
			
			
			a = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "下一月", 
				innerHTML: "<img src=\"" + Base.template + "images/page/page-next.gif\" width=11 height=11 align=absmiddle border=0 />" }, e);
			Event.observe(a, "click", (function() { this.add(Date.MONTH, 1); }).bind(this));
			
			a = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "下一年", 
				innerHTML: "<img src=\"" + Base.template + "images/page/page-last.gif\" width=11 height=11 align=absmiddle border=0 />" }, e);
			Event.observe(a, "click", (function() { this.add(Date.YEAR, 1); }).bind(this));
			
			// 生成关闭按钮
			e = ElementHelper.createElement("span", { className: "floatRight shortcutBar" }, header);
			var ul = ElementHelper.createElement("ul", null, e);
			
			var li = ElementHelper.createElement("li", { className: "minusButton" }, ul);
			button = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "清空"}, li);
			Event.observe(button, "click", (function() { this.clear(); }).bind(this));
			
			var li = ElementHelper.createElement("li", { className: "refreshButton" }, ul);
			button = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "今天"}, li);
			Event.observe(button, "click", (function() { this.returnToday(); }).bind(this));
			
			li = ElementHelper.createElement("li", { className: "closeButton" }, ul);
			var button = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "关闭" }, li);
			Event.observe(button, "click", (function() { this.hide(); }).bind(this));
			
			
			this.dayContainer = ElementHelper.createElement("div", null, this.mainContainer);
			this.dayTable = ElementHelper.createElement("table", { className: "jtable" }, this.dayContainer);
			var thead = ElementHelper.createElement("thead", null, this.dayTable);
			var tr = ElementHelper.createElement("tr", null, thead);
			var td;
			for (var i = 0; i < this.COLUMNS; i++) {
				ElementHelper.createElement("td", { innerHTML: this.DAYS[i], className: this.classNames[i] } , tr);
			}
			
			
			this.timeContainer = ElementHelper.createElement("div", { className: "footer" }, this.mainContainer);
			
			ElementHelper.createElement("span", { innerHTML: "时间:", style: { margin: "0 2px 0 0" } }, this.timeContainer);
			this.hourContainer = ElementHelper.createElement("span", null, this.timeContainer);
			this.hourElement = ElementHelper.createElement("input", { maxlength: 2,
				value: StringHelper.leftPad(this.date.getHours(), 2, '0') }, this.hourContainer);
			ElementHelper.createElement("span", { innerHTML: ":" }, this.hourContainer);
			
			this.minuteContainer = ElementHelper.createElement("span", null, this.timeContainer);
			this.minuteElement = ElementHelper.createElement("input", { maxlength: 2,
				value: StringHelper.leftPad(this.date.getMinutes(), 2, '0') }, this.minuteContainer);
			ElementHelper.createElement("span", { innerHTML: ":" }, this.minuteContainer);
			
			this.secondContainer = ElementHelper.createElement("span", null, this.timeContainer);
			this.secondElement = ElementHelper.createElement("input", { maxlength: 2,
				value: StringHelper.leftPad(this.date.getSeconds(), 2, '0') }, this.secondContainer);
			ElementHelper.createElement("span", { innerHTML: "." }, this.secondContainer);
			
			this.millisecondContainer = ElementHelper.createElement("span", null, this.timeContainer);
			this.millisecondElement = ElementHelper.createElement("input", { maxlength: 3, style: { width: 25 },
				value: StringHelper.leftPad(this.date.getMilliseconds(), 3, '0') }, this.millisecondContainer);
			
			var validateTime = function (field) {
				var v = field.value;
				if (v && !NumberHelper.isNumber(v)) {
					alert("输入的时间必须为数字");
					field.focus();
					return false;
				}
				field.value = StringHelper.leftPad(v, NumberHelper.intValue(field.getAttribute("maxlength")), '0');
			}
			
			Event.observe(this.hourElement, "blur", function() { validateTime(this); });
			Event.observe(this.minuteElement, "blur", function() { validateTime(this); });
			Event.observe(this.secondElement, "blur", function() { validateTime(this); });
			Event.observe(this.millisecondElement, "blur", function() { validateTime(this); });
			
				
			a = ElementHelper.createElement("a", { href: "javascript: void(0);", title: "将时间清零", innerHTML: "清空", 
				style: { margin: "0 2px 0 5px", textDecoration: "underline" } }, this.timeContainer);
			Event.observe(a, "click", (function() { this.clearTime(); }).bind(this));
			
			var o = this;
			// 点击时隐藏日历
			Event.observe(document, "click", function() {
				var hideOnClickDocument = o.hideOnClickDocument;
				o.hideOnClickDocument = true;
				if (!hideOnClickDocument || !o.visible) return; o.hide(); 
			});
			
			// 点击this.container则需要阻止事件传递给body
			Event.observe(this.container, "click", function() { o.clickSelf(); });
			
			// 仅触发一次创建时的事件
			this.fire("create", options);
		}
		
		Element[this.options.time ? "show" : "hide"](this.timeContainer);
	},
	
	/**
	 * 画出本月的日期
	 */
	paintDate: function() {
		var tbody = this.dayTable.tBodies[0];
		if (tbody) this.dayTable.removeChild(tbody)
		tbody = ElementHelper.createElement("tbody", null, this.dayTable);
		var firstDay = this.date.getFirstDayOfMonth();
		var days = this.date.getDaysInMonth();
		//var rows = Math.ceil((firstDay + days) / this.COLUMNS);
		var previousMonth = this.date.add(Date.MONTH, -1);	// 前一个月
		var nextMonth = this.date.add(Date.MONTH, 1);		// 后一个月
		var tr, td, day, className, anotherMonth, daysInPreviousMonth = previousMonth.getDaysInMonth();
		var date;
		if (firstDay == 0) firstDay = 7;	// 保证必然显示上一个月的几天数据
		for (var i = 0; i < this.ROWS; i++) {
			tr = ElementHelper.createElement("tr", null, tbody);
			for (var j = 0; j < this.COLUMNS; j++) {
				day = i * this.COLUMNS + j + 1 - firstDay;
				anotherMonth = (day <= 0 || day > days); // 不是当前显示的月份
				if (day <= 0) {
					day += daysInPreviousMonth
					date = previousMonth;
				} else if (day > days) {
					day -= days;
					date = nextMonth;
				} else {
					date = this.date.clone();
				}
				
				date.setDate(day);
				
				// className = anotherMonth ? "anotherMonth" : (this.classNames[j] + (same && day == this.today.getDate() ? " today" : ""));
				
				td = ElementHelper.createElement("td", { innerHTML: day }, tr);
				
				if (anotherMonth) {
					td.setAttribute("date", date.format(this.options.format));
					Element.addClassName(td, "anotherMonth");
				} else {
					Element.addClassName(td, this.classNames[j]);
				}
				if (DateHelper.isSameDay(date, this.today)) {
					// 今天
					Element.addClassName(td, "today");
					td.title = "今天";
				} else if (this.currentDate && DateHelper.isSameDay(date, this.currentDate)) {
					// 当前显示的日期
					Element.addClassName(td, "current");
					this.currentElement = td;
				}
				
				Event.observe(td, "click", function() { CalendarHelper.click(); });
				Event.observe(td, "mouseover", function() { Element.addClassName(this, "mouserover"); });
				Event.observe(td, "mouseout", function() { Element.removeClassName(this, "mouserover"); });
			}
		}
	},
	
	/**
	 * 画出日历
	 */
	paint: function() {
		this.yearContainer.innerHTML = this.date.getFullYear();
		this.monthContainer.innerHTML = StringHelper.leftPad(this.date.getMonth() + 1, 2, "0");
		this.paintDate();
	},
	
	/**
	 * 改变了年月后重画
	 */
	repaint: function() {
		this.paint();
	},
	
	/**
	 * 增加value.可以为负数
	 */
	add: function(interval, value) {
		value = NumberHelper.intValue(value, 1);
		if (value == 0) return;
		
		var date = this.date.add(interval, value);
		// this.fire("add", interval, value);
		// this.repaint();
		this.set(date);
	},
	
	/**
	 * 重新设定日期
	 */
	set: function(date, formats) {
		date = this.parse(date, formats);
		// date不能转为日期或者未改变年月
		if (!date || DateHelper.isSameDay(date, this.date)) return;
		this.date = date;
		this.fire("set", date, formats);
		this.repaint();
	},
	
	/**
	 * 将日历跳转到今天
	 */
	returnToday: function() {
		this.set(this.today);
		this.fire("returnToday");
	},
	/**
	 * 得到element的日期
	 */
	get: function(element) {
		if (!(element = Object.isElement(element) ? element : EventHelper.getElement())) return;
		var date = element.getAttribute("date");
		if (!date) {
			var day = element.innerHTML;
			if (!day) return;
			date = this.date.clone();
			date.setDate(day)
			//date = date.format(this.format);
		} else {
			date = this.parse(date);
		}
		
		if (this.options.time) {
			// 需要显示时间
			var y = date.getFullYear();
			var m = date.getMonth();
			var d = date.getDate();
			var h = NumberHelper.intValue(this.hourElement.value);
			var mi = NumberHelper.intValue(this.minuteElement.value);
			var s = NumberHelper.intValue(this.secondElement.value);
			var ms = NumberHelper.intValue(this.millisecondElement.value);
			if (!DateHelper.isValid(y, m + 1, d, h, mi, s, ms)) {
				alert("请输入正确的时间!");
				return null;
			}
			date.setHours(h);
			date.setMinutes(mi);
			date.setSeconds(s);
			date.setMilliseconds(ms);
		}
		this.fire("get", date, element);
		return date;
		//if (!this.element) return;
	},
	/**
	 * 将date字符串转为日期
	 */
	parse: function(date, formats) {
		return DateHelper.parse(date, formats || this.options.formats);
	},
	/**
	 * 格式化日期为字符串
	 */
	format: function(date) {
		date = Object.isUndefined(date) ? this.date : date;
		return DateHelper.format(date, this.options.format);
		// return this.date.format(this.options.format);
	},
	
	/**
	 * 显示日历
	 * element: 要显示日期的元素.定义为false/0则不需要此元素
	 * options: 一些基本操作
	 */
	show: function(element, options) {
		var event = EventHelper.get(true);
		if (event && event.type == "click") {
			// 如果是依靠click事件触发的本方法,则调用此方法.防止在FF中因为事件的捕获机制与IE不一致造成日历显示的错误.
			// 在FF中,如果是click事件触发的show方法.会在show执行完毕之后才执行document的click,就会触发hide事件.这样会造成日历无法显示.
			// 因此在click时调用此方法,是hideOnClickDocument变量为false,防止document的click事件隐藏日历
			this.clickSelf();
		}
		
		if (element) element = $(element);
		//if (!element) element = document.body;
		if (!element || !Object.isElement(element)) {
			element = Object.isUndefined(element) || element === null ? EventHelper.getElement() : null;
		}
		this.element = element;
		this.currentDate = null;
		this.currentElement = null;
		
		this.initialize(options);
		this.paint();
			
		//显示的位置,在input的相对位置
		var top = 0, left = 0;
		if (this.element) {
			// Event.stop(EventHelper.get());	// 显示时阻止click事件的传递
			Event.observe(this.element, "click", (function() { this.clickSelf(); }).bind(this));
    		var offsets = ElementHelper.getOffset(this.element);
    		top = offsets.bottom;
    		left = offsets.left;
		} else if (event) {
			top = event.pageX;
			left = event.pageY;
		}
		
		this.container.style.top = top;
		this.container.style.left = left;
		
		Element.show(this.container);
		this.visible = true;
		this.fire("show", element, options);
		return;
	},
	
	/**
	 * 隐藏日历控件
	 */
	hide: function() {
		Element.hide(this.container);
		this.visible = false;
		this.fire("hide");
	},
	
	/**
	 * 当前的日期,点击一个文本框时文本框中的日期
	 */
	currentDate: null,
	
	/**
	 * 当前的日期对象
	 */
	currentElement: null,
	
	/**
	 * 点击日期的事件
	 */
	click: function(element) {
		if (!(element = Object.isElement(element) ? element : EventHelper.getElement())) return;
		if (this.currentElement != element) {
			if (this.currentElement) Element.removeClassName(this.currentElement, "current");
			this.currentElement = element;
			Element.addClassName(this.currentElement, "current");
		}
		this.fire("click", element);
	},
	/**
	 * 清空日期
	 */
	clear: function() {
		var hideOnInput = this.options.hideOnInput;
		this.options.hideOnInput = false;			// 在input时不自动隐藏
		this.input("");
		this.options.hideOnInput = hideOnInput;		// 还原this.options.hideOnInput
		this.fire("clear");
	},
	
	/**
	 * 清空时间
	 */
	clearTime: function() {
		this.hourElement.value = '00';
		this.minuteElement.value = '00';
		this.secondElement.value = '00';
		this.millisecondElement.value = '000';
	},
	
	/**
	 * 填入日期
	 */
	input: function(date) {
		if (this.element) {
			date = StringHelper.trim(this.format(date));
			if (Object.isUndefined(this.element.value)) this.element.innerHTML = date;
			else this.element.value = date;
			this.fire("input", date);
		}
	},
	
	/**
	 * 监听的事件
	 */
	listeners: {
		click: function(element) {
			// this.hide();
			this.input(this.get(element));
		},
		
		clear: function() {
			if (this.options.hideOnClear) this.hide();
		},
		
		input: function() {
			if (this.options.hideOnInput) this.hide();
		},
		
		returnToday: function() {
			if (this.options.inputOnReturnToday) {
				var hideOnInput = this.options.hideOnInput;
				this.options.hideOnInput = false;			// 在input时不自动隐藏
				this.input(this.today);
				this.options.hideOnInput = hideOnInput;		// 还原this.options.hideOnInput
				if (this.options.hideOnReturnToday) this.hide();
			}
		}
	},
	/**
	 * 监听event事件
	 */
	addListener: function(event, listener) {
		if (!event) return;
		this.listeners[event] = listener;
	},
	/**
	 * 触发event事件
	 */
	fire: function(event) {
		if (!event) return;
		var listener;
		if (!this.options.listeners || !Object.isFunction(listener = this.options.listeners[event])) {
			listener = this.listeners[event];
		}
		// var listener = this.listeners[event];
		if (listener && Object.isFunction(listener)) {
			var args = Array.prototype.slice.call(arguments, 1);
			listener.apply(this, args);
		}
	},
	
	/**
	 * 将parentNode下的所有包含calendar样式的子节点绑定日历事件
	 * @param parentNode
	 */
	observe: function(parentNode) {
		parentNode = $(parentNode || document.body);
		var elements = parentNode.select('.calendar[__calendar_initialize__!="true"]');
		if (!elements) return;
		var me = this;
		elements.each(function(element){
			element = $(element);
			if (element.getBoolean('__calendar_initialize__')) return;	// 已经初始化
			element.setAttribute('__calendar_initialize__', 'true');
			element.readOnly = true;
			Element.addClassName(element, 'pointer');
			Event.observe(element, "focus", (function(element) {
				var options = element.getAttribute('options');
				this.show(element, options);
			}).bind(me, element));
		});
	}
};

var Calendar = CalendarHelper;
Helper.add("CalendarHelper", CalendarHelper);
Event.observe(window, "load", function() {
	CalendarHelper.observe();
});