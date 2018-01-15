package com.sinosoft.efiling.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.sinosoft.util.FileHelper;
import com.sinosoft.util.StringHelper;

/**
 * 为一些静态文件设定返回的contentType
 * 
 * @author LuoGang
 * 
 */
public class ContentTypeFilter implements Filter {
	Map<String, String> contentTypes = new LinkedHashMap<String, String>();

	public void init(FilterConfig filterConfig) throws ServletException {
		Enumeration<String> names = filterConfig.getInitParameterNames();
		String name;
		while (names.hasMoreElements()) {
			name = names.nextElement();
			contentTypes.put(name, filterConfig.getInitParameter(name));
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest r = (HttpServletRequest) request;
			String fileType = FileHelper.getFileSuffix(r.getRequestURI());
			String contentType = contentTypes.get(fileType);
			if (!StringHelper.isEmpty(contentType)) {
				// 更改contentType
				response.setContentType(contentType);
			}
		} finally {
			chain.doFilter(request, response);
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

}
