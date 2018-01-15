/**
 * 表单处理工具类
 */
var FormHelper = {
	/* object是否为表单对象 */
	isForm: function(object) {
		return !!(object && object.tagName && object.tagName.toLowerCase() == "form");
	},
	
	/* object是否为表单的输入对象 */
	isField: function(object) {
		return !!(Object.isElement(object) && (object.tagName.toLowerCase() == "input" 
			|| object.tagName.toLowerCase() == "textarea" || object.tagName.toLowerCase() == "select"  
			|| object.form === null || Object.isForm(object.form)));
	},
	
	/* object是否为表单的输入对象 */
	isOptionOfSelect: function(object) {
		return !!(Object.isElement(object) && object.tagName.toLowerCase() == "option");
	},
	
	
	/* object是否为表单的select对象 */
	isFieldOfSelect: function(object) {
		return this.isField(object) && object.tagName.toLowerCase() == "select";
	},
	
	/* object是否为多选列表框 */
	isFieldOfSelectMultiple: function(object) {
		return this.isField(object) && object.type == "select-multiple";
	},
	
	/* object是否为多选框 */
	isFieldOfCheckbox: function(object) {
		return this.isField(object) && (object.type == "checkbox");
	},
	/* object是否为单选框 */
	isFieldOfRadio: function(object) {
		return this.isField(object) && (object.type == "radio");
	},
	
	/**
	 * disabled: true不可提交;false:可提交
	 * form: 要操作的表单
	 * typeName: 要操作的表单域类型
	 * name: 要操作的表单域名称
	 */
	setDisabled: function(disabled, form, typeName, name) {
		disabled = Object.isUndefined(disabled) ? true : !StringHelper.isFalse(disabled);
		if (form) {
			Form[disabled ? "disable" : "enable"](form, typeName, name);
		} else {
			var forms = document.getElementsByTagName("form");
			for (var i = 0, length = forms.length; i < length; i++) {
				Form[disabled ? "disable" : "enable"](forms[i], typeName, name);
			}
		}
	}
	
};

FormHelper.CheckBoxHelper = {
	checkBoxNames: ["checkbox"],					/* 选择框名称 */
	operationCheckBoxNames: ["checkAll_Or_None"],	/* 操作框名称 */
	
	onCheck: null,	/* 选择一个时的执行函数 */
	onUnCheck: null,	/* 取消选择某个选择框时的执行函数 */
	
	onCheckAll: null,	/* 全选时的执行函数 */
	onUnCheckAll: null,	/* 全不选时的执行函数 */
	
	onCheckOther: null,	/* 反选时的执行函数 */
	/**
	 * 选择e
	 * e: 选择框
	 * checked: 是否选中
	 * click: 是否点击e，当click为false时checked才有效
	 * callback: 是否调用回调函数.若省略且click为true，则为true
	 */
	check: function(e, checked, click, callback) {
		if (Form.isFieldOfCheckbox(e) && 
			(StringHelper.indexInArray(e.name, this.checkBoxNames) > -1 || StringHelper.indexInArray(e.name, this.operationCheckBoxNames) > -1)) {
			checked = (checked || Object.isUndefined(checked)) ? true : false;
			if (!click) {
				e.checked = checked;
			}
			callback = Object.isUndefined(callback) && click ? true : callback;
			
			if (callback) {
				if (e.checked) {
					if(Object.isFunction(this.onCheck)) {
						this.onCheck(e);
					}
				} else {
					if(Object.isFunction(this.onUnCheck)) {
						this.onUnCheck(e);
					}
				}
			}
		}
	},
	
	
	
	/**
	 * 不选择e
	 */
	unCheck: function(e) {
		this.check(e, false);
	},
	
	/**
	 * 全选
	 */
	checkAll: function(checked) {
		checked = (checked || Object.isUndefined(checked)) ? true : false;
		
		var checkbox = document.body.getElementsByTagName("input");
		for (var i = 0, l = checkbox.length; i < l; i++){
			this.check(checkbox[i], checked);
		}
		
		if (checked) {
			if(Object.isFunction(this.onCheckAll)) {
				this.onCheckAll();
			}
		} else {
			if(Object.isFunction(this.onUnCheckAll)) {
				this.onUnCheckAll();
			}
		}
	},
	
	/**
	 * 全不选
	 */
	unCheckAll: function() {
		this.checkAll(false);
	},
	
	/* 选中所有操作按钮 */
	checkAllOptionCheckBoxes: function(checked) {
		checked = (checked || Object.isUndefined(checked)) ? true : false;
		
		for (var i = 0, l = this.operationCheckBoxNames.length; i < l; i++) {
			var checkbox = document.getElementsByName(this.operationCheckBoxNames[i]);
			for (var j = 0, m = checkbox.length; j < m; j++){
				this.check(checkbox[j], checked);
			}
		}
	},
	
	/**
	 * 全不选
	 */
	unCheckAllOptionCheckBoxes: function() {
		this.checkAllOptionCheckBoxes(false);
	},
	
	/**
	 * 反选
	 */
	checkOther: function() {
		var checkbox = document.body.getElementsByTagName("input");
		for (var i = 0, l = checkbox.length; i < l; i++){
			if (Form.isFieldOfCheckbox(checkbox[i])) {
				this.check(checkbox[i], !checkbox[i].checked);
			}
		}
		
		if (Object.isFunction(this.onCheckOther)) {
			this.onCheckOther();
		}
	},
	
	/* 是否至少选中了一个 */
	isCheckedOne: function() {
		return this.getCheckedNumber() > 0;
	},
	
	/**
	 * 是否全选
	 * 返回false:表示非全选，否则为选中的个数
	 */
	isCheckedAll: function() {
		var n = 0;
		var checkbox = document.body.getElementsByTagName("input");
		for (var i = 0, l = checkbox.length; i < l; i++){
			if (Form.isFieldOfCheckbox(checkbox[i]) && 
				(StringHelper.indexInArray(checkbox[i].name, this.checkBoxNames) > -1)) {
				if (!checkbox[i].checked) {
					return false;
				}
				n++;
			}
		}
		
		return n;
	},
	
	/* 选中的数量 */
	getCheckedNumber: function() {
		var n = 0;
		var checkbox = document.body.getElementsByTagName("input");
		for (var i = 0, l = checkbox.length; i < l; i++){
			if (Form.isFieldOfCheckbox(checkbox[i]) && 
				(StringHelper.indexInArray(checkbox[i].name, this.checkBoxNames) > -1) && checkbox[i].checked) {
				n += 1;
			}
		}
		
		return n;
	}
}



/* 为Helper添加子对象 */
Helper.add("FormHelper", FormHelper);