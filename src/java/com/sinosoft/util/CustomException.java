package com.sinosoft.util;

import java.util.Locale;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

/**
 * 自定义异常，异常信息通过message指定描述的标签
 * 
 * @author LuoGang
 * 
 */
public class CustomException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7506857103880185166L;

	public CustomException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CustomException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CustomException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CustomException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 异常信息+错误代码构成的异常对象
	 * 
	 * @param code 错误代码
	 * @param message 异常信息
	 */
	public CustomException(String code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * @param message 异常信息
	 * @param parameters 国际化参数
	 */
	public CustomException(String message, String[] parameters) {
		super(message);
		this.parameters = parameters;
	}

	/**
	 * @param code 错误代码
	 * @param message 异常信息
	 * @param parameters 国际化参数
	 */
	public CustomException(String code, String message, String[] parameters) {
		this(code, message);
		this.parameters = parameters;
	}

	/** 异常代码 */
	private String code;

	/** 当前使用的国际化 */
	private Locale locale;

	/**
	 * 资源文件标签替换参数
	 */
	private Object[] parameters;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setLocale(String locale) {
		this.locale = LocalizedTextUtil.localeFromString(locale, Locale.getDefault());
	}

	/**
	 * 得到国际化的Locale对象
	 * 
	 * @return
	 * @see ActionContext#getContext()
	 * @see ActionContext#getLocale()
	 * @see Locale#getDefault()
	 */
	public Locale getLocale() {
		if (locale == null) {
			ActionContext ctx = ActionContext.getContext();
			if (ctx != null) locale = ctx.getLocale();
			if (locale == null) locale = Locale.getDefault();
		}
		return locale;
	}

	@Override
	public String getLocalizedMessage() {
		String message = super.getMessage();
		try {
			message = LocalizedTextUtil.findText(getClass(), message, getLocale(), message, this.parameters);
		} catch (Exception e) {
			// 此时可能无法找到key对应的数据,则可以认为key就是异常信息
		}
		if (message == null) message = "";
		if (code != null) message = code + ":" + message;
		return message;
	}
}
