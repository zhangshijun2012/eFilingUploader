var Base = {
	logined: true,	// 已登录
	SYSTEM_CODE: 'uploader',
	SERVER_ROOT: "/uploader/",			// 站点根目录,如不为空,则以/开始,以/结束.如:/path/
	DEFAULT_TEMPLATE : "default",
	encoding: "utf-8",	// 使用的编码
	template: '',		// 当前模板目录
	LOCALE: 'zh_CN'		// 国际化语言
};
var LOCALE = Base.LOCALE;
var SYSTEM_CODE = Base.SYSTEM_CODE;
var SERVER_ROOT = Base.SERVER_ROOT;
var DEFAULT_TEMPLATE = Base.DEFAULT_TEMPLATE;
/**
 * 登录/登出
 * @param loginOnly 是否仅执行login操作，如果为false，则会弹出提示框"尚未登录或会话已经过期,请重新登录!"
 */
function login(loginOnly) {
	var win = window;
	if (parent && parent[SYSTEM_CODE] === true) win = parent;
	if (!loginOnly) alert('尚未登录或会话已经过期,请重新登录!');
	win.location = SERVER_ROOT + 'logout.do';
}