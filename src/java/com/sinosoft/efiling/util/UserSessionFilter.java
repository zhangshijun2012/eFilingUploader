package com.sinosoft.efiling.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.opensymphony.xwork2.interceptor.MethodFilterInterceptorUtil;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.cas.AuthenticationFilter;

public class UserSessionFilter implements Filter {
	/** 根应用的contextPath */
	public static final String ROOT_CONTEXT_PATH = "/";

	/** 不需要校验的地址,可使用通配符*,注意如果匹配的数据中包含/,则需要使用两个* */
	protected Set<String> exclude = new HashSet<String>();
	/** 需要校验的地址,可使用通配符* */
	protected Set<String> include = new HashSet<String>();

	/**
	 * 得到配置的属性
	 * 
	 * @param filterConfig
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	protected final String getPropertyFromInitParams(FilterConfig filterConfig, String propertyName, String defaultValue) {
		String value = filterConfig.getInitParameter(propertyName);

		if (!StringHelper.isEmpty(value)) {
			return value;
		}

		value = filterConfig.getServletContext().getInitParameter(propertyName);

		if (!StringHelper.isEmpty(value)) {
			return value;
		}
		return defaultValue;
	}

	/**
	 * 检查是否登录
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean validate(HttpServletRequest request) throws IOException, ServletException {
		HttpSession session = request.getSession();
		Object user = session.getAttribute(SystemUtils.USER_SESSION_NAME);
		if (user == null) {
			SystemUtils.initializeUserSession(request);
			session = request.getSession();
			// session.getAttribute(SystemUtils.USER_SESSION_NAME);
			user = session.getAttribute(SystemUtils.USER_SESSION_NAME);
		}

		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (!StringHelper.isEmpty(contextPath) && !ROOT_CONTEXT_PATH.equalsIgnoreCase(contextPath)) {
			uri = uri.substring(contextPath.length());
		}

		/* 判断uri是否需要进行session校验 */
		if (!MethodFilterInterceptorUtil.applyMethod(exclude, include, uri)) return true;

		return user != null;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		String include = filterConfig.getInitParameter(AuthenticationFilter.INCLUDE_URI_PARAMETER_NAME);
		if (StringHelper.isEmpty(include)) {
			include = getPropertyFromInitParams(filterConfig, AuthenticationFilter.CAS_INCLUDE_URI_PARAMETER_NAME, null);
		}
		if (!StringHelper.isEmpty(include)) {
			this.include.addAll(TextParseUtil.commaDelimitedStringToSet(include));
		}
		String exclude = filterConfig.getInitParameter(AuthenticationFilter.EXCLUDE_URI_PARAMETER_NAME);
		if (StringHelper.isEmpty(exclude)) {
			exclude = getPropertyFromInitParams(filterConfig, AuthenticationFilter.CAS_EXCLUDE_URI_PARAMETER_NAME, null);
		}
		if (!StringHelper.isEmpty(exclude)) {
			this.exclude.addAll(TextParseUtil.commaDelimitedStringToSet(exclude));
		}

		if (!this.include.isEmpty() && !this.exclude.isEmpty() && !this.include.contains("*")
				&& !this.exclude.contains("*")) {
			// 防止没有写*而无法包含所有的请求地址
			this.include.add("*");
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		request.setCharacterEncoding(SystemUtils.ENCODING);
		response.setCharacterEncoding(SystemUtils.ENCODING);
		HttpSession session = request.getSession();
		if (!validate(request)) {
			session.invalidate(); // 禁用所有session
			String uri = request.getRequestURI();
			try {
				response.reset();
				if (uri.toLowerCase().endsWith(".js")) {
					// 请求为js文件
					response.setContentType("text/javascript");
					response.setHeader("Content-Type", "text/javascript; charset=" + SystemUtils.ENCODING);
					// alert('尚未登录或会话已经过期,请重新登录!');
					response.getWriter().println("login();");
				} else {
					// 跳转到登录页面
					response.setContentType("text/html");
					response.setHeader("Content-Type", "text/html; charset=" + SystemUtils.ENCODING);
					response.getWriter().println("尚未登录或会话已经过期,请重新登录!");
				}
			} catch (Exception e) {
				// TODO
			}
			return;
		}

		// request.setCharacterEncoding(SystemUtils.ENCODING);
		// response.setCharacterEncoding(SystemUtils.ENCODING);

		chain.doFilter(request, response);
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

}
