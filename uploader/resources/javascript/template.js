/** 加载样式 */
(function(){
	var Template = {
		namespace : 'resources/template/',
		templates : {
			'default' : {
				id : 'default',
				files : [ 'default.css', 'layout.css', 'menu.css', 'table.css', 'dialog.css', 'shortcutBar.css', 'calendar.css' ],
				namespace: ''	// 模板目录以,若有则以'/'结尾,此目录下应该有images和css两个目录.其中css存放css文件
			}
		},
	
		load : function(key) {
			var template = key;
			if (Helper.isObject(template)) {
				this.templates[template['id']] = template;
			} else {
				template = this.templates[template];
				if (!template['id'])
					template[id] = template;
			}
			this.template = template;
			if (!template['namespace']) {
				template['namespace'] = this.namespace + template['id'] + '/';
			}
			var directory = template['namespace'] + 'css';
			FileHelper.Includer.include(template['files'], FileHelper.Includer.Types.css, {
				directory : directory
			});
		}
	};
	Template.load(DEFAULT_TEMPLATE || 'default');
	Base.Template = Template;
	Base.template = Template.template['namespace'];
})();
