/**
 * 可拦截的对象
 */
var Interceptable = {
	/**
	 * 
	 * 判断method是否会被拦截
	 * 
	 * @param method 方法名称
	 * @param includeMethods 要拦截的方法
	 * @param excludeMethods 排除的方法
	 * @returns {Boolean}
	 */
	applyMethod : function(method, includeMethods, excludeMethods) {
		var include = false;
		if (!includeMethods || includeMethods === '*') {
			include = true;
		} else {
			includeMethods.each(function(includeMethod) {
				if (includeMethod.include('*')) { // *号为通配符
					var regExp = new RegExp(includeMethod.replaceAll('\\*', '\.\*'), "g");
					if (include = regExp.test(method)) throw $break;
				} else if (include = includeMethod == method) {
					throw $break;
				}
			});
		}

		if (!include) return false;

		if (!excludeMethods) return include;

		excludeMethods.each(function(excludeMethod) {
			if (excludeMethod.include('*')) { // *号为通配符
				var regExp = new RegExp(excludeMethod.replaceAll('\\*', '\.\*'), "g");
				if (regExp.test(method)) {
					include = false;
					throw $break;
				}
			} else if (excludeMethod == method) {
				include = false;
				throw $break;
			}
		});

		return include;
	},
	/**
	 * 拦截obj对象，如果obj为函数则拦截其原型对象
	 * @param obj
	 * @param includeMethods 要拦截的方法
	 * @param excludeMethods 排除的方法
	 */
	intercept : function(obj, includeMethods, excludeMethods) {
		if (Object.isFunction(obj)) obj = obj.prototype;
		var initialize = obj['initialize'];
		if (initialize && !initialize['intercepted']) {
			obj['initialize'] = (function(initialize, afterInitialize) {
				return function() {
		    		var args = $A(arguments);
		    		var value = initialize.apply(this, args);
		    		afterInitialize.apply(this);	// 不带参数调用，用于初始化时添加事件
		    		return value;
				};
			})(initialize, this.initialize);
			obj['initialize']['intercepted'] = true;
		}
		for (var method in obj) {
			var applyMethod = obj['applyMethod'] || this['applyMethod'];
		    if (Object.isFunction(obj[method]) && !obj[method]['intercepted'] && (applyMethod(method, includeMethods, excludeMethods))) {
		    	obj[method] = (function(method, exec) {
		    		return function() {
			    		var args = $A(arguments);
			    		if (!this['validate'](method, args)) return false;	// 校验失败则直接返回
			    		try {
			    			this['before'](method, args);	// before,在method执行前调用
			    		} catch (e) {
			    			// 方法中可通过抛出$break异常而跳过后面的调用
			    			if (e != $break) throw e;
			    			return;	// before中抛出$break异常则不执行方法
			    		}

			    		var value;
		    			//try {
			    			value = exec.apply(this, args);
			    			this['after'](method, args);	// after,在method执行完毕后调用
			    		///} catch (e) {
			    			// 方法中可通过抛出$break异常而跳过后面的调用
			    		//	if (e != $break) throw e;
			    		//}
		    			return value;
		    		};
		    	})(method, obj[method]);
		    	obj[method]['intercepted'] = true;
		    }
		}
		Object.extend(obj, Helper.clone(this.interceptor));
	},
	
	/** 初始化 */
	initialize: function(options, once) {
		options = options || this['options'];
		if (!options) return;
		if (options['beforeListeners']) {
			for (var method in options['beforeListeners']) {
				this.addBeforeListener(method, options['beforeListeners'][method], once);
			}
		}
		if (options['listeners']) {
			for (var method in options['listeners']) {
				this.addListener(method, options['listeners'][method], once);
			}
		}
		if (options['validators']) {
			for (var method in options['validators']) {
				this.addValidator(method, options['validators'][method]);
			}
		}
	},
	/** 拦截器对象 */
	interceptor: {
		/**
		 * 触发method的事件
		 * @param method
		 * @param args 格式化为数组的参数
		 * @param before 是否为before事件
		 * @returns
		 */
		fire: function(method, args, before) {
			if (!this.beforeListeners) this.beforeListeners = { };
			if (!this.listeners) this.listeners = { };
			before = !(!before || before === 'after' || before === 'on');
			try {
				var listeners = this[before ? 'beforeListeners' : 'listeners'][method];
				var value;
				var exception;
				if (listeners) {
					listeners = listeners.clone();	// 使用clone的对象是为了防止在方法中有可能删除了监听事件
					var me = this;
					listeners.each(function(listener, index) {
						// 返回false则不继续执行
						try {
							value = listener.apply(me, args);
						} catch (e) {
							exception = e;
							throw e;
						}
						if (value === false) throw $break;
					});
				}
				if (exception) throw exception;
				
				if (value !== false) {
					var listener = this[(before ? 'before' : 'after') + method.substring(0, 1).toUpperCase() + method.substring(1)];
					if (listener) value == listener.apply(this, args);
				}
				return value;
			} finally {
				if (before) return;
				
				// onMethod必然会执行
				var listener = this['on' + method.substring(0, 1).toUpperCase() + method.substring(1)];
				if (listener) {
					return listener.apply(this, args);
				}
			}
		},
		/** 执行before方法用的事件 */
		// beforeListeners : {},
		/**
		 * 执行方法前的调用
		 * 
		 * @param method 正在执行的方法
		 * @param args 调用method的参数，已转换为数组形式
		 */
		before : function(method, args) {
			return this.fire(method, args, 'before');
		},
		/**
		 * 添加beforeMethod事件的监听
		 * @param method
		 * @param listener
		 * @param once
		 * @returns
		 */
		addBeforeListener : function(method, listener, once) {
			return this.addListener(method, listener, once, 'before');
		},
		/**
		 * 删除beforeMethod事件的监听
		 * @param method
		 * @param listener
		 * @returns
		 */
		removeBeforeListener : function(method, listener) {
			return this.removeListener(method, listener, 'before');
		},
	
		/** 所有监听事件 */
		// listeners : {},
		/**
		 * 执行方法后的调用
		 * 
		 * @param method 正在执行的方法
		 * @param args 调用method的参数，已转换为数组形式
		 */
		after : function(method, args) {
			return this.fire(method, args, 'after');
		},
		/**
		 * 增加一个监听事件
		 * 
		 * @param method
		 * @param listener
		 * @param once	此事件是否仅执行一次
		 * @param before
		 */
		addListener : function(method, listener, once, before) {
			if (!method) return;
			if (Helper.isObject(method)) {
				// method为{ method: listener }
				before = before || once;
				before = !(!before || before === 'after' || before === 'on');
				once = listener;
				var listeners = method;
				for (var method in listeners) {
					this.addListener(method, listeners[method], once, before);
				}
				
				return;
			}
			
			if (!this.beforeListeners) this.beforeListeners = { };
			if (!this.listeners) this.listeners = { };
			
			before = !(!before || before === 'after' || before === 'on');
			var listeners = this[before ? 'beforeListeners' : 'listeners'][method];
			if (!listener) return listeners;
			if (!listeners) {
				listeners = this[before ? 'beforeListeners' : 'listeners'][method] = [];
			}
			if (once) {
				// 此事件只执行一次
				var onceListener =(function(listener, method, before){
					return function() {
						listener.apply(this, $A(arguments));
						this.removeListener(method, onceListener, before);
					};
				})(listener, method, before);
				listener = onceListener;
			}
			listeners.push(listener);
			return listeners;
		},
	
		/**
		 * 删除method的监听事件
		 * 
		 * @param method
		 * @param listener
		 */
		removeListener : function(method, listener, before) {
			if (Object.isUndefined(method)) {
				// 删除所有事件
				this.beforeListeners = { };
				this.listeners = { };
				return;
			}
			if (Helper.isObject(method)) {
				// method为{ method: listener }
				before = before || listener;
				before = !(!before || before === 'after' || before === 'on');
				var listeners = method;
				for (var method in listeners) {
					this.removeListener(method, listeners[method], before);
				}				
				return;
			}

			if (!this.beforeListeners) this.beforeListeners = { };
			if (!this.listeners) this.listeners = { };
			
			before = !(!before || before === 'after' || before === 'on');
			if (!method || method === true) {
				if (!listener) return this[before || method === true ? 'beforeListeners' : 'listeners'] = { };
				var listeners = this[before || method === true ? 'beforeListeners' : 'listeners'];
				for (var method in listeners) {
					this.removeListener(method, listener, before);
				}
				return;
			}
			var listeners = this[(before ? 'beforeListeners' : 'listeners')];
	
			if (!listener || listener === true) {
				var v = listeners[method];
				listeners[method] = null;
				return v;
			}
			var listeners = listeners[method];
			if (listeners) {
				var index = listeners.indexOf(listener);
				if (index >= 0) return listeners.splice(index, 1)[0];
			}
		},

		/** 所有校验器 */
		// validators : {},
		/**
		 * 为method方法添加一个校验器
		 * 
		 * @param method
		 * @param validator
		 */
		addValidator : function(method, validator) {
			if (!this.validators) this.validators = { };
			if (Helper.isObject(method)) {
				// method为{ method: listener }
				var validators = method;
				for (var method in validators) {
					this.addValidator(method, validators[method]);
				}
				return;
			}
			
			var validators = this.validators[method];
			if (!validator) return validators;
			if (!validators) {
				validators = this.validators[method] = [];
			}
			validators.push(validator);
			return validators;
		},
	
		/**
		 * 删除method的校验器
		 * 
		 * @param method
		 * @param validator
		 */
		removeValidator : function(method, validator) {
			if (!method) return this.validators = { };
			if (!this.validators) return this.validators = { };
			if (Helper.isObject(method)) {
				// method为{ method: listener }
				var validators = method;
				for (var method in validators) {
					this.removeValidator(method, validators[method]);
				}
				return;
			}
			if (!validator) {
				var v = this.validators[method];
				this.validators[method] = null;
				return v;
			}
			var validators = this.validators[method];
			if (validators) {
				var index = validators.indexOf(validator);
				if (index >= 0) return validators.splice(index, 1)[0];
			}
		},
		/**
		 * 检验method方法是否可执行
		 * 
		 * @param method
		 * @returns {Boolean}
		 */
		validate : function(method, args) {
			if (!this.validators) this.validators = { };
			var validators;
			var value = true;
			if (validators = this.validators[method]) {
				if (Object.isFunction(validators)) {
					value = validators.apply(this, args);
				} else {
					for ( var i = 0, l = validators.length; i < l; i++) {
						value = validators[i].apply(this, args);
						if (value === false) break;
					}
				}
			}
	
			if (value === false) return value; // 一旦返回false则不继续执行
	
			if (validators = this['validate' + method.substring(0, 1).toUpperCase() + method.substring(1)]) {
				if (Object.isFunction(validators)) {
					value = validators.apply(this, args);
				}
			}
			return value || Object.isUndefined(value); // undfiend则视为true
		}
	}
};