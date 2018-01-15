(function() {
	/** 模块要加载的JS文件的根目录 */
	var ROOT_DIRECTORY = 'resources/javascript/service/';
	/** 配置每个模块及其需要加载的JS文件等信息. */
	var modules = {
		// 注意每一个对象的service属性不能赋值
		'eFiling.setting.file.type' : { // 文件类型设置
			file : 'FileType.js', // 要加载的JS文件
			imports : null, // 需要引入的其他文件
			className : 'FileType' // 类名,此参数必须
		},
		'eFiling.setting.file.box' : { // 档案盒设置，设置档案盒可装份数等信息
			file : 'FileBoxVersion.js', // 要加载的JS文件
			className : 'FileBoxVersion'
		},
		'eFiling.setting.archive' : { // 归档期限设置
			className : 'FileDeadline',
			file : 'FileDeadline.js',
			imports : null
		},

		'eFiling.file.scan' : { // 批量文件扫描
			className : 'FileScanManager',
			file : 'FileScanManager.js'
		},

		'eFiling.file.manager' : { // 单一文件扫描菜单
			className : 'FileManager',
			file : 'FileManager.js',
			imports : [ 'FileScanManager.js' ]
		},
		
		'eFiling.file.image': {	// 影像文件上传
			className : 'FileImage',
			file : 'FileImage.js',
			imports : [ 'FileService.js' ]
			
		},
		
		'eFiling.file.core': {	// 承保资料上传
			className : 'FileCore',
			file : 'FileCore.js',
			imports : [ 'FileService.js' ]
			
		},
		
		'eFiling.file.core.approve': {	// 上传资料审核
			className : 'FileCoreApprove',
			file : 'FileCoreApprove.js',
			imports : [ 'FileService.js' ]
			
		},
		'eFiling.file.query' : { // 综合查询
			className : 'FileQuery',
			file : 'FileQuery.js',
			imports : [ 'FileService.js' ]
		},

		'eFiling.file.report' : { // 档案报表
			className : 'FileLackReport',
			file : 'FileLackReport.js',
			imports : [ 'FileService.js' ]
		},

		'eFiling.file.lending' : { // 档案借阅管理
			className : 'FileLending',
			file : 'FileLending.js',
			imports : [ 'FileService.js' ]
		},

		'eFiling.file.type.barcode.print' : { // 投保资料条码打印
			className : 'FileTypeBarcodePrint',
			file : 'FileTypeBarcodePrint.js'
		},

		'eFiling.barcode.print' : { // 条码打印
			className : 'BarcodePrint',
			file : 'BarcodePrint.js'
		}
	};

	window['Module'] = {
		modules: modules,
		transport : new Ajax.Support(true, false), // 仅创建request对象
		/** 根据关键字加载对应的模块信息 */
		get : function(key) {
			if (Object.isUndefined(key)) return this.module;
			// alert(key);
			var module;
			if (Helper.isObject(key)) {
				module = key;
				key = module['className'];
				if (!key) return;
				if (!modules[key]) {
					modules[key] = module;
				} else {
					module = modules[key];
				}
			} else {
				var cls = key;
				module = modules[cls];
				while (!module) {
					var p = cls.lastIndexOf('.');
					if (p <= 0) break;
					cls = cls.substring(0, p);
					module = modules[cls];
				}
			}
			if (!module) {
				// alert(key);
				var cls = key.split('.');
				cls = cls[key.length - 1];
				cls = cls.substring(0, 1).toUpperCase() + cls.substring(1);
				module = modules[key] = {
					'className' : cls
				};
			}
			module['id'] = key; // 加载模块时的taskCode
			if (!modules[key]) {
				modules[key] = module;
			}

			if (!module['file']) {
				module['file'] = module['className'] + '.js';
			}

			if (Object.isArray(module['imports']) && module['imports'].length <= 0) {
				module['imports'] = null;
			}

			return module;
		},

		module : null, // 当前加载的模块
		service : null, // 当前模块使用的主要JS对象
		loading : false, // 正在加载
		/** 成功加载module后的调用方法 */
		onLoad : function(module, options) {
			this.previousModule = this.module;
			this.previousService = this.service;

			if (!module['service']) {
				module['service'] = window[module['className']];
			}
			if (this.previousService && this.previousService.exit) {
				// 调用前一个的exit方法
				this.previousService.exit();
			}

			// alert(module['className'] + ' = ' + window[module['className']]);
			this.module = module;
			this.service = module['service'];

			if (Object.isFunction(options['onSuccess'])) {
				options['onSuccess']();
			}
			
			// try {
			if (Object.isFunction(module['onSuccess'])) {
				// 有onSuccess方法
				return module['onSuccess']();
			} else if (this.service && Object.isFunction(this.service['main'])) {
				// 执行service的main方法
				return this.service['main']();
			}
			// } catch(e){
			// if (e != $break) throw e;
			// } finally {
			this.loading = false;
			// }
		},

		/**
		 * 加载module
		 * 
		 * @param module modules中配置的模块信息
		 */
		load : function(module, options) {
			if (this.loading) this.transport.abort();
			if (this.service) this.service.abort();

			this.loading = true;
			module = Object.isUndefined(module) ? this.module : this.get(module);
			this.loadingModule = module;
			options = options || {};

			var onSuccess = (function(module, options) {
				this.onLoad(module, options);
			}).bind(this, this.loadingModule, options);

			if (module['service']) {
				// 已经加载过此数据
				return onSuccess();
			}

			var loadModule = function() {
				if (!module['service']) module['service'] = window[module['className']];
				if (module['service']) {
					// 此JS文件已经加载
					return onSuccess();
				}
				
				FileHelper.Loader.load(module['file'], "text/javascript", null, {
					absolute : true, // 使用绝对路径
					directory : ROOT_DIRECTORY, // js所在的根目录
					'onSuccess' : onSuccess
				});
			};
			// try {
			if (module.imports) {
				// 有需要引入的其他文件
				FileHelper.Loader.load(module['imports'], "text/javascript", null, {
					absolute : true, // 使用绝对路径
					directory : ROOT_DIRECTORY, // js所在的根目录
					'onSuccess' : function() {
						loadModule();
					}
				});
			} else {
				loadModule();
			}
			// } catch (error) {
			// this.loading = false;
			// alert('数据加载失败:\n' + error.message);
			// throw error;
			// }
		}
	};
})();