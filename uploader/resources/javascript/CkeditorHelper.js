var CkeditorHelper = {
	imageUrl: Base.SERVER_ROOT + "file/read.do",
	replace: function(elementOrIdOrName, config, options) {
		var _config = {
			//enterMode		: Number( 2 ),	// 回车模式:1创建<P>标签;2:<br>;3:<div>
			//shiftEnterMode	: Number( 1 ),
			height: "450"
		}
		Object.extend(_config, config || { });
		var editor = CKEDITOR.replace(elementOrIdOrName, _config);
		var url = CkeditorHelper.imageUrl;// ? options.imageUrl : this.imageUrl;
		/* 此事件只会触发1次 */
		editor.on('customConfigLoaded', function() {
			CKEDITOR.on("dialogDefinition", function (evt) {
				var dialogName = evt.data.name;
				if (dialogName == "image") {
					var dialogDefinition = evt.data.definition;
					var infoTab = dialogDefinition.getContents("info");
					infoTab.add({
						type: "button", 
						id: "upload_image", 
						style: "display:inline-block;margin-top:10px;",
						align: "right bottom", 
						label: "上传本地图片", 
						onClick:function (evt) {
							var dialog = this.getDialog();
							FileHelper.Uploader.show({
								title: "上传图片", 
								types: FileHelper.Uploader.Types.image, 
								autoClose: true,
								no: false, 
								name: false,
								onSuccess: function(json) {
									var o = dialog.getContentElement("info", "txtUrl");
									var e = o.getInputElement().$;//.id;	// 得到url的文本框
									e.value = url + (url.include('?') ? "&" : "?") + "id=" + json.file.id;
									e.fireEvent("onchange");
									this.dialog.hide();
								}
							});
							
						}
					}, "browse"); //place front of the browser button
				}
			});
		});
		
		editor.on('mode', function() {
			this.addCommand("save", {	// 保存事件
				exec: function (evt) {
					var buttons = Selector.findChildElements(Message.messageLayerFooter, ["input"]);
					if(buttons && buttons[0]) {
						buttons[0].click();
					}
					return false;
				}
			});
			if (!Message.lastLocation) {
				Message.lastLocation = {}
			}
			Message.lastLocation.width = "90%";
			Message.repaint();
		});
		//this.addImageUploadButton(editor);
		return editor;
	}
}