/**
 * 校验各个接口的安全
 */
var Security = {
	namespace : "/eFiling/uploader/validate/",
	/**
	 * 修改密码
	 */
	modify : function() {
		var parameters = $('modifyName').serialize();
		var onSuccess = function(response) {
			var text = response.responseText;
			if (text) text = eval('(' + text + ")"); 
			alert(text['message']);
		};
		var options = {
			parameters : parameters,
			onSuccess : onSuccess
		};
		this.send(this.namespace + "modify.do", options);
	},
	/**
	 * 输入域不能够为null
	 */
	validate : function() {
		
	},
	/**
	 *  取消更改密码
	 */
	cancel : function() {
		$('modifyName').reset();
	},
	/**
	 * 新增验证密码的系统
	 */
	append : function() {
		
	},
	/** 
	 * @param url  访问的url
	 * @param options json对象
	 */
	send : function(url, options) {
		var parameters = options ? options['parameters'] : "";
		var onSuccess = options ? options['onSuccess'] : "";
		url += "?" + parameters;
		this.post(url, onSuccess);
	
	},
	/**
	 * @param url 请求后台的url
	 * @param onSuccess 回调函数
	 */
	post : function(url, onSuccess) {
		//创建Ajax访问后台的对象
		var xmlhttp;
		if(window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
			  xmlhttp = new XMLHttpRequest();
		} else {// code for IE6, IE5
			  xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		}
		xmlhttp.onreadystatechange = function() {
			if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
				//请求完毕执行回调函数
				//这儿的this对象是xmlhttp
				onSuccess.apply(this, [xmlhttp]);
			}
			if (xmlhttp.readyState == 3) {
				//alert("正在请求中");
			}
			if (xmlhttp.readyState == 2) {
				//alert("请求已接收");
			}
			
		};
		xmlhttp.open("GET", url);
		xmlhttp.send();
	}
};

var windowBrrow = {
	//绑定事件
	attachEvent: function() {
		document.attachEvent("onclick", function() {
			alert('onload');
		});
	},
	opera: window.opera,
	navigator: {
		appCodeName: navigator.appCodeName,
		appName: navigator.appName,
		appVision: navigator.appVersion,
		cookieEnabled: navigator.cookieEnabled,
		platform: navigator.platform,
		systemLanguage: navigator.systemLanguage,
		userAgent: navigator.userAgent,
		Animal: Class.create({
			name: '小狗',
			initialize: function() {
				alert('initialize');
			},
			speak: function() {
				alert('旺旺');
			}
		}),
	},
	create: function() {
		var properties = $A(arguments);
		var length = 0;
		if((length = properties.length) != 0) {
			for (var i = 0; i < length; i++) {
				//alert(typeof properties[i]);
				if (properties[i] === 'function') {
					//alert(properties[i]);
				}
			}
		} 
	},
	exce: function() {
		this.create('zhangjun', new Array(), (function() {alert('可执行函数');})(), {'name': 'zhangjun', 'init': function(){}});
	}	
			
};
windowBrrow.attachEvent();
windowBrrow.exce();
/*
var factorial = function(x) {
	alert(arguments);
	if (x < 2) return 1;
	else return x * arguments.callee(x - 1);
};


var result = factorial(5);
alert(result);

function check(args) {
	throw new Error("错了");
	var actual = args.length;
	var expected = args.callee.length;
	if (actual != expected) {
		throw new Error("错了");
	}
};
check('ok');
*/
 