/**
 * 复选框处理类
 */
CheckBoxHelper = {
	/**
	 * 选中表单forms下的elements
	 * checked:是否选中.如果为-1则表示反选
	 */
	setChecked: function(checked, forms, elements, onSuccess) {
		checked = checked === -1 ? -1 : (checked || Object.isUndefined(checked) ? true : false);
		forms = !forms ? $A(document.forms) : (Object.isArray(forms) ? forms : [forms]);
		var i = forms.length, j, k;
		elements = !elements ? null : (Object.isArray(elements) ? elements : [elements]);
		while((i--) > 0) {
			if (!(forms[i] = $(forms[i]))) continue;
			if (!elements) {
				// 所有checkbox
				elements = Form.getElements(forms[i], "checkbox");
			}
			j = elements.length;
			while (j-- > 0) {
				if (Object.isElement(elements[j])) {
					Form.Element.setValue(elements[j], checked === -1 ? !elements[j].checked : checked);
				} else if (elements[j] = forms[i].elements[elements[j]]){
					if (Object.isElement(elements[j])) {
						Form.Element.setValue(elements[j], checked === -1 ? !elements[j].checked : checked);
					} else if (elements[j] = $A(elements[j])) {
						k = elements[j].length;
						while(k-- > 0) {
							Form.Element.setValue(elements[j][k], checked === -1 ? !elements[j][k].checked : checked);
						}
					}
				}
			}
		}
		
		if (Object.isFunction(onSuccess)) {
			onSuccess();
		}
	},
	/**
	 * 全选
	 */
	check: function(forms, elements, onSuccess) {
		this.setChecked(true, forms, elements, onSuccess);
	},
	/**
	 * 全不选
	 */
	clear: function(forms, elements, onSuccess) {
		this.setChecked(false, forms, elements, onSuccess);
	},
	/**
	 * 反选
	 */
	turn: function(forms, elements, onSuccess) {
		this.setChecked(-1, forms, elements, onSuccess);
	},
	
	/**
	 * 判断表单forms下的elements是否被全部选中
	 */
	isCheckedAll: function(forms, elements) {
		forms = !forms ? $A(document.forms) : (Object.isArray(forms) ? forms : [forms]);
		var i = forms.length, j, k;
		elements = !elements ? null : (Object.isArray(elements) ? elements : [elements]);
		while((i--) > 0) {
			if (!(forms[i] = $(forms[i]))) continue;
			if (!elements) {
				// 所有checkbox
				elements = Form.getElements(forms[i], "checkbox");
			}
			j = elements.length;
			while (j-- > 0) {
				if (Object.isElement(elements[j])) {
					if (!elements[j].checked) return false;
				} else if (elements[j] = forms[i].elements[elements[j]]){
					if (Object.isElement(elements[j])) {
						if (!elements[j].checked) return false;
					} else if (elements[j] = $A(elements[j])) {
						k = elements[j].length;
						while(k-- > 0) {
							if (!elements[j][k].checked) return false;
						}
					}
				}
			}
		}
		return true;
	},
	
	/**
	 * 判断表单forms下的elements被选中的数量
	 */
	getCheckedCount: function(forms, elements) {
		var count = 0;
		forms = !forms ? $A(document.forms) : (Object.isArray(forms) ? forms : [forms]);
		var i = forms.length, j, k;
		elements = !elements ? null : (Object.isArray(elements) ? elements : [elements]);
		while((i--) > 0) {
			if (!(forms[i] = $(forms[i]))) continue;
			if (!elements) {
				// 所有checkbox
				elements = Form.getElements(forms[i], "checkbox");
			}
			j = elements.length;
			while (j-- > 0) {
				if (Object.isElement(elements[j])) {
					if (elements[j].checked) count++;
				} else if (elements[j] = forms[i].elements[elements[j]]){
					if (Object.isElement(elements[j])) {
						if (elements[j].checked) count++;
					} else if (elements[j] = $A(elements[j])) {
						k = elements[j].length;
						while(k-- > 0) {
							if (elements[j][k].checked) count++;
						}
					}
				}
			}
		}
		return count;
	},
	
	/**
	 * 初始化各选择框的事件
	 */
	observe: function(forms, elements, onclick) {
		// forms = forms ? forms : this.forms["query"];
		// elements = elements ? elements : "ids";
		if (!onclick) {
			return;
		}
		// operationCheckBox = (Object.isString(operationCheckBox) || Object.isElement(operationCheckBox)) ? [operationCheckBox] : $A(operationCheckBox);
		forms = !forms ? $A(document.forms) : (Object.isArray(forms) ? forms : [forms]);
		var i = forms.length, j, k;
		elements = !elements ? null : (Object.isArray(elements) ? elements : [elements]);
		while((i--) > 0) {
			if (!(forms[i] = $(forms[i]))) continue;
			if (!elements) {
				// 所有checkbox
				elements = Form.getElements(forms[i], "checkbox");
			}
			j = elements.length;
			while (j-- > 0) {
				if (Object.isElement(elements[j])) {
						Event.observe(elements[j], "click", onclick);
				} else if (elements[j] = forms[i].elements[elements[j]]){
					if (Object.isElement(elements[j])) {
						Event.observe(elements[j], "click", onclick);
					} else if (elements[j] = $A(elements[j])) {
						k = elements[j].length;
						while(k-- > 0) {
							Event.observe(elements[j][k], "click", onclick);
						}
					}
				}
			}
		}
	}
	
	
}