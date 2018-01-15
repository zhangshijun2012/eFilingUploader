function $(element) {
	if (arguments.length > 1) {
		for ( var i = 0, elements = [], length = arguments.length; i < length; i++)
			elements.push($(arguments[i]));
		return elements;
	}

	if (typeof element == 'string') element = document.getElementById(element);
	return element;
}

if (!window['Event']) Event = {};
/**
 * 为element绑定事件
 */
Event.on = function(element, eventName, responder) {
	if (element.addEventListener) {
		element.addEventListener(eventName, responder, false);
	} else {
		element.attachEvent('on' + eventName, responder);
	}
};

var ImageViewer = function(img, options) {
	this.initialize($(img), options);
};

ImageViewer.prototype = {
	initialize : function(img, options) {
		this.img = this.element = img;
		this.options = options || { };
		this.container = img.parentNode;
		for ( var m in ImageViewer.modes) {
			var mode = ImageViewer.modes[m];
			if (!mode.support) continue;
			this.mode = mode;
			break;
		}

		if (!this.mode) return false;

		this.width = this.img.width;
		this.height = this.img.height;
		this.radian = 0;
		this.x = 1;
		this.y = 1;
		this.zoom = 1;

		this.center();

		for ( var m in this.mode) {
			this[m] = this.mode[m];
		}
		// 调用mode的init方法
		if (this.init) this.init();

		// 为img绑定鼠轮滚动事件
		var $this = this;
		Event.on(this.img, 'mousewheel', function(evt) {
			var wheelDelta = evt.wheelDelta ? evt.wheelDelta / 120 : (evt.detail || 0) / 3;
			// 鼠标滚动一次放大1/50倍
			var zoom = wheelDelta / 15;
			if (zoom != 0) $this.scale(zoom);
		});
		
		this.toolbar = document.createElement('div');
		this.toolbar.className = 'toolbar';
		this.container.appendChild(this.toolbar);
		this.showToolbar();
	},
	/** 显示操作栏 */
	showToolbar: function() {
		this.toolbar.style.width = '100%';
		this.toolbar.innerHTML = '<a href="#" onclick="viewer.reset();return false;">原图</a> | '
			+ '<a href="' + this.options.downloadUrl + '" target="_blank">下载原图</a> | '
			+ '<a href="#" onclick="viewer.left();return false;">向左旋转</a> | '
			+ '<a href="#" onclick="viewer.right();return false;">向右旋转</a> | '
			+ '<a href="#" onclick="viewer.rotate(45);return false;">向左旋转45度</a> | '
			+ '<a href="#" onclick="viewer.rotate(-45);return false;">向右旋转45度</a> | '
			+ '<a href="#" onclick="viewer.scale(0.2);return false;">放大</a> | '
			+ '<a href="#" onclick="viewer.scale(-0.2);return false;">缩小</a> | '
			+ '<a href="#" onclick="viewer.setZoom(1);return false;">100%</a> | '
			+ '<a href="#" onclick="viewer.hideToolbar();return false;">隐藏操作栏&lt;</a>';
		
	},
	/** 隐藏操作栏 */
	hideToolbar: function() {
		this.toolbar.style.width = '12px';
		this.toolbar.innerHTML = '<a title="显示操作栏" href="#" onclick="viewer.showToolbar(); return false;">&gt;</a>';
	},
	/** 恢复正常显示 */
	reset : function() {
		this.radian = 0;
		this.x = 1;
		this.y = 1;
		this.zoom = 1;
		this.show();
	},

	// 垂直翻转
	vertical : function() {
		this.radian = Math.PI - this.radian;
		this.y *= -1;
		this.show();
	},
	// 水平翻转
	horizontal : function() {
		this.radian = Math.PI - this.radian;
		this.x *= -1;
		this.show();
	},
	// 向左转90度
	left : function() {
		this.radian -= Math.PI / 2;
		this.show();
	},
	// 向右转90度
	right : function() {
		this.radian += Math.PI / 2;
		this.show();
	},
	/**
	 * 旋转图片,参数为角度,需要转换为弧度
	 * 
	 * @param angle 旋转的角度
	 */
	rotate : function(angle) {
		this.radian += (angle % 360) * Math.PI / 180;
		this.show();
	},

	/**
	 * 缩放图片
	 * 
	 * @param zoom 放大(> 0)/缩小(< 0)的倍数
	 */
	scale : function(zoom) {
		if (!zoom) return;
		this.zoom += zoom;
		if (this.zoom <= 0) this.zoom = Math.max(this.zoom, 1 / 10000000);
		this.show();
	},
	/**
	 * 设置缩放倍数
	 * @param zoom
	 */
	setZoom: function(zoom) {
		if (!zoom) return;
		this.zoom = zoom;
		if (this.zoom <= 0) this.zoom = Math.max(this.zoom, 1 / 10000000);
		this.show();
	},
	/**
	 * 获取变换参数
	 * 
	 * @returns { M11:, M12:, M21:, M22:}
	 */
	getMatrix : function() {
		var radian = this.radian;
		var x = this.x * this.zoom;
		var y = this.y * this.zoom;
		var cos = Math.cos(radian), sin = Math.sin(radian);
		return {
			M11 : cos * x,
			M12 : -sin * y,
			M21 : sin * x,
			M22 : cos * y
		};
	},

	/** 居中显示图片 */
	center : function() {
		var bodyWidth = document.body.clientWidth;
		var bodyHeight = document.body.clientHeight;

		var max = this.container.offsetWidth;
		var width = this.width * this.zoom;
		if (width > max) {
			this.container.style.width = width + 'px';
		} else if (max > bodyWidth) {
			this.container.style.width = bodyWidth + 'px';
		}

		var max = this.container.offsetHeight;
		var height = this.height * this.zoom;
		if (height > max) {
			this.container.style.height = height + 'px';
		} else if (max > bodyHeight) {
			this.container.style.height = bodyHeight + 'px';
		}
		height = this.container.offsetHeight;
		// alert(height + ',' + (this.height * this.zoom));
		if (height > this.height * this.zoom) height -= 25; // 除去底部工具栏的高度
		// alert(Math.ceil((height - this.height) / 2));
		var top = Math.ceil((height - this.height) / 2);
		this.img.style.top = top + 'px';
		// var max = this.container.offsetWidth;
		// this.img.style.left = (max - this.img.width * this.x) / 2 + 'px';
	}
};

(function() {

	ImageViewer.modes = {
		/** IE滤镜 */
		filter : {
			support : (function() {
				return "filters" in document.createElement("div");
			})(),
			init : function() {
				// 设置滤镜
				this.img.style.filter = "progid:DXImageTransform.Microsoft.Matrix(SizingMethod='auto expand')";
				this.filters = this.img.filters.item("DXImageTransform.Microsoft.Matrix");
			},
			show : function() {
				// 设置滤镜
				var matrix = this.getMatrix();
				for ( var p in matrix) {
					this.filters[p] = matrix[p];
				}
				// this.center();
			}
		},

		css3 : {
			support : (function() {
				var style = document.createElement("div").style;
				var supprt = false;
				var $this = this;
				var transforms = [ "transform", "MozTransform", "webkitTransform", "OTransform" ];
				for ( var i = 0, l = transforms.length; i < l; i++) {
					var css = transforms[i];
					if (!(css in style)) return;
					supprt = css;
					break;
				}
				return supprt;
			})(),
			init : function() {
				this.css3Transform = this.support;
			},
			show : function() {
				var matrix = this.getMatrix();
				matrix = "matrix(" + matrix.M11.toFixed(16) + "," + matrix.M21.toFixed(16) + ","
						+ matrix.M12.toFixed(16) + "," + matrix.M22.toFixed(16) + ", 0, 0)";
				// 设置变形样式
				this.img.style[this.css3Transform] = matrix;
				
				this.center();
				
			}
		}
	};
})();