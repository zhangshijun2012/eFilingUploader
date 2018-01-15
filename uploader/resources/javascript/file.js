/**
 * 文件上传管理相关JS
 */
FileHelper.Uploader = {
	dialog: null,	// 文件上传对话框
	form: null,		// form表单
	url: Base.SERVER_ROOT + "file/upload.do",		// 上传的地址
	types: null,	// 允许的文件类型
	autoClose: false,	// 上传成功之后是否关闭
	/**
	 * 得到本地文件路径
	 * e: 文件选择框对象
	 */
	getLocalFile: function(e) {
		if (!e) return "";
		try {
			if (Prototype.Browser.IE) {
				e.select();
				var v = document.selection.createRange().text;
				if (!StringHelper.isEmpty(v)) {
					return v;
				}
			}
		} catch (error) {
			
		}
		// return e.files.item(0).getAsDataURL(); 	// 使用于FF,得到被加密过的文件路径
		return e.value;
	},
	queryProgressUrl: Base.SERVER_ROOT + "file/queryProgress.do",
	/**
	 * 查询上传进度
	 */
	queryProgress: function(options) {
		if (this.complete) return;	// 可由外部中断
		options = options || { };
		var url = this.queryProgressUrl;
		if (options.url) {
			url = options.url
		}
		//this._onQueryProgressSuccess = options.onSuccess;
		var uploader = this;
		var _options = {
			showLoading: false,
			onSuccess: function(response, options, headerJSON) {
				uploader.onQueryProgressSuccess(response, options, headerJSON);
			}
		};
		Object.extend(_options, options);
		AjaxHelper.send(url, _options);
	},
	K: 1024,
	UNITS: ["B", "KB", "MB", "GB", "TB"],	// 单位
	onQueryProgressSuccess: function(response, options, headerJSON) {
		// alert('onQueryProgressSuccess');
		if (Object.isFunction(this._onQueryProgressSuccess)) {
			this._onQueryProgressSuccess(response, options, headerJSON);
		} else {
			/*if (!Message.isShow()) {	// 默认是在Message上的操作
				return;
			}*/
			
			var text = response.responseText;
			// alert(text);
			var json = StringHelper.evalJSON(text);
			
			json = json || { };
			
			if (json.changed || json.complete) { // json.changed == false表示数据未变化,则不需要调用此数据
				var progress = Math.max(0, NumberHelper.intValue(json.progress)); // 上传进度
				var size = NumberHelper.intValue(json.size);			// 总大小
				size = size == 0 ? -1 : size;
				
				var percent = progress / size * 100;
				if (percent >= 100 && !json.complete) percent = 99;	// 尚未complete时不能显示为100%.虽然已经上传完成,单还需要一些保存操作
				this.progressBar.style.width = percent + "%";
				this.progressPercent.innerHTML = NumberHelper.format(percent, 0) + "%";
				var unit = 0;
				while (progress >= this.K && unit < this.UNITS.length - 1) {
					progress = progress / this.K;
					unit++;
				}
				this.progressSize.innerHTML = NumberHelper.format(progress) + this.UNITS[unit];
				this.progressMessage.innerHTML = StringHelper.trim(json.message);
			}
			if (json.complete) {	// 上传完毕
				// alert(text);
				this.onComplete(json);
				return;
			}
			
			// 等待300ms继续查询
			var o = this;
			window.setTimeout(function() {	o.queryProgress(options); }, 300);
			
		}
	},
	upload: function(options) {
		if (!this.validateUpload()) {
			return false;
		}
		
		this.complete = false;
		if (Object.isFunction(this.onUpload)) {
			this.onUpload();
		}
		
		this.form.submit();		// 提交表单
		this.disable();
		this.queryProgress(options ? options.queryProgressOptions : null);
	},
	complete: false,
	success: false,
	onComplete: function(json) {
		this.complete = true;
		this.success = json && json.success;
		if (json && json.success) {
			var v = this.onSuccess(json);
			if (v) return v;
			this.dialog.buttons["continueUpload"].disabled = false;
			this.dialog.buttons["cancel"].disabled = false;
			this.reset();
		} else {
			this.onFailure(json);
			this.enable();
		}
	},
	
	separator: " ",	// 多个附件之间使用的分隔符,参见org.anywnyu.util.hibernate.type.ArrayStringType
	/**
	 * 上传之后的调用
	 * 默认会回写到Message中的filesId和files元素中
	 */
	onSuccess: function(json) {		// 上传完成之后的回调函数
		if (Object.isFunction(this._onSuccess)) {
			var v = this._onSuccess(json);
			if (v) return v;
		} else {
			/*if (!Message.isShow()) {	// 默认是在Message上的操作
				return;
			}*/
			json = json || { };
			var file = json.file || { };
			
			this.progressMessage.innerHTML = StringHelper.isEmpty(json.message) ? "上传完毕" : json.message;
			var e = Selector.findChildElements(Message.messageLayer, "#filesId")[0];
			if (e) {
				var id = StringHelper.trim(e.value);
				id = id + this.separator + file.id;
				e.value = id.trim();
			}
			
			e = Selector.findChildElements(Message.messageLayer, "#files")[0];
			if (e) {
				var name = file.name;
				name = "<span id=\"file_" + file.id + "\"><a href=\"javascript: void(0)\" onclick=\"FileHelper.Uploader.download('" 
							+ file.id + "')\">" + name + "</a>[<a href=\"javascript: void(0)\" "
							+ "onclick=\"FileHelper.Uploader.remove('" + file.id + "')\">删除</a>] &nbsp;</span>";
				e.innerHTML += name;
			}
		}
		
		if (this.autoClose) {
			this.hide();
		}
	},
	onFailure: function(json) {		// 上传失败的回调函数
		if (Object.isFunction(this._onFailure)) {
			this._onFailure(json);
			return;
		}
		json = json || { };
		this.progressMessage.innerHTML = StringHelper.isEmpty(json.message) ? "上传失败" : json.message;
		
		if (!json.quiet) {	// 不弹出提示框
			alert(StringHelper.isEmpty(json.message) ? "文件上传失败,请稍候重试..." : json.message.trim());
		}
	},
	
	validateUpload: function() {
		var file = this.getLocalFile(this.form.elements["file"]); //.value.trim();
		if (StringHelper.isEmpty(file)) {
			alert("请选择要上传的文件");
			return false;
		}
		this.form.elements["uploadPath"].value = file;
		if (!this.types) {
			return true;	// 未限制上传类型
		}
		if (Object.isString(this.types)) {
			this.types = this.types.split(",");	// 可以为用逗号分开的多种文件类型
		}
		if (!this.types || this.types.length <= 0) {
			return true;	// 未限制上传类型
		}
		var p = file.lastIndexOf(".");
		var type = (p >= 0 && p < file.length - 1) ? file.substring(p + 1) : "";
		if (!StringHelper.isInArray(type, this.types, true)) {
			alert("不允许上传所选择的文件类型\n只允许上传类型为:" + this.types.join(",") + "的文件!");
			return false;
		}
		return true;
	},
	/** 隐藏 */
	hide: function() {
		this.dialog.hide();
	},
	/** 显示文件上传窗口 */
	show: function(options) {
		options = options || { };
		this.autoClose = options.autoClose;
		var uploader = this;
		var _options = {
			zIndex: 11000,
			width: 385,
			buttons: {
				confirm: {
					text: "上 传",
					handle: function() {
						uploader.upload(options.uploadOptions);
					}
				},
				
				upload: {
					text: "上传并关闭",
					show: !uploader.autoClose && !options['onlyUpload'],
					handle: function() {
						uploader.autoClose = true;
						uploader.upload(options.uploadOptions);
					}
				},
				
				continueUpload: {
					text: "继续上传",
					show: false && !uploader.autoClose,
					disabled: true,
					handle: function() {
						uploader.reset();
					}
				},
				
				cancel: {
					text: "取 消",
					handle: function() {
						this.hide();
					}
				}
			}
		};
		var url = this.url;
		if (options.url) {
			url = options.url;
		}
		this.types = options.types;	// 允许的文件类型
		this._onSuccess = options.onSuccess;
		this._onFailure = options.onFailure;
		Object.extend(_options, options.dialogOptions || { });
		var title = options.title ? options.title : "上传文件";
		var html = "<div id=\"fileUploader\"><form action=\"" + url + "\" name=\"fileUploadForm\" id=\"fileUploadForm\" method=\"post\" enctype=\"multipart/form-data\" "
					+ "target=\"hiddenIframe\">"
					+ "<input type=\"hidden\" name=\"uploadPath\" id=\"uploadPath\" />"
					+ "<input type=\"hidden\" name=\"foreignId\" id=\"foreignId\" value=\"" + (options['foreignId'] || options['foreign'] || '').trim() + "\" />"
					+ "请选择要上传的文件:"
					+ "<div class=\"row\"><input type=\"file\" name=\"file\" id=\"file\" size=\"" 
					+ (Prototype.Browser.IE ? 53 : 48) + "\" /></div>"
					+ "<div class=\"row" + (options['uploaderNo'] === false || options['uploaderNo'] === '' ? ' hidden' : '') + "\" id=\"uploaderNo\">"
					+ (options['uploaderNo'] === false || options['uploaderNo'] === '' ? '' : (options['uploaderNo'] || (
					"	<div class=\"title\">文件编号:</div>"
					+ "	<div class=\"input\"><input name=\"no\" class=\"widthAll\" value=\"" + (options['no'] || options['fileNo'] || '').trim() + "\" /></div>"
					  )))
					+ "</div>"
					+ "<div class=\"row" + (options['uploaderName'] === false || options['uploaderName'] === '' ? ' hidden' : '') + "\" id=\"uploaderName\">"
					+ ((options['uploaderName'] === false || options['uploaderName'] === '') ? '' : (options['uploaderName'] || (
					"	<div class=\"title\">文&nbsp;&nbsp;件&nbsp;&nbsp;名:</div>"
					+ "	<div class=\"input\"><input name=\"name\" class=\"widthAll\" value=\"" + (options['name'] || options['fileName'] || '').trim() + "\" /></div>"
					  )))
					+ "</div>"
					+ "<div class=\"row" + (options['message'] ? '' : ' hidden') + "\">" + (options['message'] || '')+ "</div>"
					+ "<div id=\"uploaderProgress\" class=\"progress\">"
					+ "	<div id=\"progressBar\" class=\"progressBar\"><span id=\"progressBarImage\"></span></div>"
					+ "	<div id=\"progressPercent\" class=\"progressPercent\">0.00%</div>"
					+ "	<div class=\"progressSize\">已上传:<span id=\"progressSize\">大小</span></div>"
					+ "</div>"
					+ "<div class=\"progressMessage\" id=\"progressMessage\"></div>"
					+ "</form></div>";
		if (this.dialog) {
			// this.dialog.show(html, title, _options);
			// this.form.action = url;
			// this.onShow(options);
			// return;
		} else {
			this.dialog = new MessagePane("fileUploaderDialog");
		}
		this.dialog.show(html, title, _options);
		
		this.form = Selector.findChildElements(this.dialog.messageLayer, "form")[0];
		this.progressBar = Selector.findChildElements(Selector.findChildElements(this.form, "#progressBar")[0], "span")[0];
		this.progressPercent = Selector.findChildElements(this.form, "#progressPercent")[0];
		this.progressSize  = Selector.findChildElements(this.form, "#progressSize")[0];
		this.progressMessage  = Selector.findChildElements(this.form, "#progressMessage")[0];
		
		this.onShow(options);
	},
	
	onShow: function(options) {
		this.reset();
		options = options || { };
		
		if (options['uploaderNo'] === false || options['uploaderNo'] === '') {
			// 不需要编号
		} else if (Object.isUndefined(options.no) || !!options.no) {
			// 需要填写编号
			Element.show(Selector.findChildElements(this.form, "#uploaderNo")[0]);
		} else {
			Element.hide(Selector.findChildElements(this.form, "#uploaderNo")[0]);
		}
		

		if (options['uploaderName'] === false || options['uploaderName'] === '') {
			// 不需要文件名
		} else if (Object.isUndefined(options.name) || !!options.name) {
			// 需要填写文件名
			Element.show(Selector.findChildElements(this.form, "#uploaderName")[0]);
		} else {
			Element.hide(Selector.findChildElements(this.form, "#uploaderName")[0]);
		}
	},
	removeUrl: Base.SERVER_ROOT + "file/delete.do",
	remove: function(id, options) {
		if (!id) return;
		if (!confirm("确定要删除该文件?")) return;
		var o = this;
		var _options = Object.extend({ }, options || { });
		Object.extend(_options, {
			parameters:  "id=" + encodeURIComponent(id) + (options.parameters ? ('&' + options.parameters) : ''),
			onSuccess: function(response, headerJSON) {
				var fn = options['onSuccess'] || o['onRemove'];
				if (Object.isFunction(fn)) {
					fn(response, id, headerJSON);
				}
			}
		});
		var url = _options.url || this.removeUrl;		
		AjaxHelper.send(url, _options);
	},
	
	onRemove: function(response, id, headerJSON) {
		var text = response.responseText;
		var json = StringHelper.evalJSON(text) || { };
		if (json.success) {	// 删除成功
			if (id) {
				var e = Selector.findChildElements(Message.messageLayer, "#filesId")[0];
				if (e) {
					var ids = StringHelper.trim(e.value).split(",");
					var l = ids.length;
					while (l--) {
						if (ids[l] == id) ids.splice(l, 1);
					}
					e.value = ids.join(",");
				}
				
				e = Selector.findChildElements(Message.messageLayer, "#file_" + id)[0];
				if (e) {
					e.parentNode.removeChild(e);
				}
			}
		}
		if (!json.quiet) {	// 不弹出提示框
			alert(StringHelper.isEmpty(json.message) ? (json.success ? "删除成功!" : "删除失败!") : json.message.trim());
		}
	},
	readUrl: Base.SERVER_ROOT + "file/read.do",
	downloadUrl: Base.SERVER_ROOT + "file/download.do",
	download: function(id) {
		if (!id) return;
		$("hiddenIframe").src = this.downloadUrl + "?id=" + encodeURIComponent(id);
	},
	read: function(id) {
		if (!id) return;
		window.open(this.readUrl + "?id=" + encodeURIComponent(id));
	},
	
	disable: function() {
		this.dialog.disable();
		Form.disable(this.form);
	},
	enable: function() {
		Form.enable(this.form);
		this.dialog.enable();
		this.dialog.buttons["continueUpload"].disabled = true;
	},
	reset: function() {
		this.form.reset();
		this.progressBar.style.width = 0;
		this.progressPercent.innerHTML = "";
		this.progressSize.innerHTML = "";
		this.progressMessage.innerHTML = "";
		this.enable();
	}
};
FileHelper.Uploader.Types = {
	image: ["gif","jpg","jpeg","png","bmp","ico"]		// 图片类型
}