package com.sinosoft.util.cas;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.authentication.DefaultGatewayResolverImpl;
import org.jasig.cas.client.authentication.GatewayResolver;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;

import com.opensymphony.xwork2.interceptor.MethodFilterInterceptorUtil;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.WildcardHelper;
import com.sinosoft.efiling.util.UserSessionFilter;
import com.sinosoft.util.StringHelper;

/**
 * CAS单点校验
 * 对于配置的exclude和include地址,可以使用通配符*,其中两个*匹配任意字符,一个*匹配除/外的任意字符.
 * 注意地址不能包含contextPath
 * 
 * @see WildcardHelper
 * @see WildcardHelper#compilePattern(String)
 * @see MethodFilterInterceptorUtil#applyMethod(Set, Set, String)
 * 
 * @author LuoGang
 * 
 */
public class AuthenticationFilter extends AbstractCasFilter {

	private String casServerLoginUrl;
	private boolean renew = false;

	private boolean gateway = false;
	private GatewayResolver gatewayStorage = new DefaultGatewayResolverImpl();

	/** 不需要校验的地址,可使用通配符*,注意如果匹配的数据中包含/,则需要使用两个* */
	protected Set<String> exclude = new HashSet<String>();
	/** 需要校验的地址,可使用通配符* */
	protected Set<String> include = new HashSet<String>();

	/** casExcludeUri的参数名 */
	public static final String CAS_EXCLUDE_URI_PARAMETER_NAME = "casExcludeUri";
	/** exclude的参数名 */
	public static final String EXCLUDE_URI_PARAMETER_NAME = "exclude";
	/** casIncludeUri的参数名 */
	public static final String CAS_INCLUDE_URI_PARAMETER_NAME = "casIncludeUri";
	/** include的参数名 */
	public static final String INCLUDE_URI_PARAMETER_NAME = "include";

	protected void initInternal(FilterConfig filterConfig) throws ServletException {
		if (!isIgnoreInitConfiguration()) {
			super.initInternal(filterConfig);
			setCasServerLoginUrl(getPropertyFromInitParams(filterConfig, "casServerLoginUrl", null));
			this.log.trace("Loaded CasServerLoginUrl parameter: " + this.casServerLoginUrl);
			setRenew(parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));
			this.log.trace("Loaded renew parameter: " + this.renew);
			setGateway(parseBoolean(getPropertyFromInitParams(filterConfig, "gateway", "false")));
			this.log.trace("Loaded gateway parameter: " + this.gateway);

			String gatewayStorageClass = getPropertyFromInitParams(filterConfig, "gatewayStorageClass", null);

			if (gatewayStorageClass != null) {
				try {
					this.gatewayStorage = ((GatewayResolver) Class.forName(gatewayStorageClass).newInstance());
				} catch (Exception e) {
					this.log.error(e, e);
					throw new ServletException(e);
				}
			}

			String include = filterConfig.getInitParameter(INCLUDE_URI_PARAMETER_NAME);
			if (StringHelper.isEmpty(include)) {
				include = getPropertyFromInitParams(filterConfig, CAS_INCLUDE_URI_PARAMETER_NAME, null);
				this.log.trace("Loaded casIncludeUri parameter: " + include);
			}
			this.log.trace("Loaded include parameter: " + include);
			if (!StringHelper.isEmpty(include)) {
				this.include.addAll(TextParseUtil.commaDelimitedStringToSet(include));
			}
			String exclude = filterConfig.getInitParameter(EXCLUDE_URI_PARAMETER_NAME);
			if (StringHelper.isEmpty(exclude)) {
				exclude = getPropertyFromInitParams(filterConfig, CAS_EXCLUDE_URI_PARAMETER_NAME, null);
				this.log.trace("Loaded casExcludeUri parameter: " + include);
			}
			this.log.trace("Loaded exclude parameter: " + exclude);
			if (!StringHelper.isEmpty(exclude)) {
				this.exclude.addAll(TextParseUtil.commaDelimitedStringToSet(exclude));
			}

			if (!this.include.isEmpty() && !this.exclude.isEmpty() && !this.include.contains("*")
					&& !this.exclude.contains("*")) {
				// 防止没有写*而无法包含所有的请求地址
				this.include.add("*");
			}
		}
	}

	public void init() {
		super.init();
		CommonUtils.assertNotNull(this.casServerLoginUrl, "casServerLoginUrl cannot be null.");
	}

	/**
	 * requestd的uri地址是否为呗排除的地址
	 * 
	 * @param request
	 * @param exclude 排除的地址URI
	 * @param include 必须包含的URI
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public static boolean exclude(HttpServletRequest request, Set<String> exclude, Set<String> include)
			throws IOException, ServletException {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (!StringHelper.isEmpty(contextPath) && !UserSessionFilter.ROOT_CONTEXT_PATH.equalsIgnoreCase(contextPath)) {
			uri = uri.substring(contextPath.length());
		}

		/* 判断uri是否需要进行session校验 */
		if (!MethodFilterInterceptorUtil.applyMethod(exclude, include, uri)) return true;

		return false;
	}

	public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpSession session = request.getSession(false);
		Assertion assertion = session != null ? (Assertion) session.getAttribute("_const_cas_assertion_") : null;

		if (exclude(request, this.exclude, this.include)) { // 不需进行单点校验的请求
			filterChain.doFilter(request, response);
			return;
		}

		if (assertion != null) {
			filterChain.doFilter(request, response);
			return;
		}

		String serviceUrl = constructServiceUrl(request, response);

		/* 对返回的地址加入firstLogin参数 */
		if (serviceUrl.indexOf('?') > -1) serviceUrl = serviceUrl + "&firstLogin=first";
		else serviceUrl = serviceUrl + "?firstLogin=first";

		String ticket = CommonUtils.safeGetParameter(request, getArtifactParameterName());
		boolean wasGatewayed = this.gatewayStorage.hasGatewayedAlready(request, serviceUrl);

		if ((CommonUtils.isNotBlank(ticket)) || (wasGatewayed)) {
			filterChain.doFilter(request, response);
			return;
		}

		if (request.getRequestURI().toLowerCase().endsWith(".js")) {
			// 请求为JS文件
			response.getOutputStream().println("login();");
			return;
		} else {
			String requestType = request.getHeader("X-Requested-With");
			if ("XMLHttpRequest".equalsIgnoreCase(requestType)) {
				// AJAX请求
				response.getOutputStream().print("<!-- LOGIN HTML -->");
				return;
			}
		}

		this.log.debug("no ticket and no assertion found");
		String modifiedServiceUrl;
		if (this.gateway) {
			this.log.debug("setting gateway attribute in session");
			modifiedServiceUrl = this.gatewayStorage.storeGatewayInformation(request, serviceUrl);
		} else {
			modifiedServiceUrl = serviceUrl;
		}

		if (this.log.isDebugEnabled()) {
			this.log.debug("Constructed service url: " + modifiedServiceUrl);
		}

		String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl, getServiceParameterName(),
				modifiedServiceUrl, this.renew, this.gateway);

		if (this.log.isDebugEnabled()) {
			this.log.debug("redirecting to \"" + urlToRedirectTo + "\"");
		}
		response.sendRedirect(urlToRedirectTo);
	}

	public final void setRenew(boolean renew) {
		this.renew = renew;
	}

	public final void setGateway(boolean gateway) {
		this.gateway = gateway;
	}

	public final void setCasServerLoginUrl(String casServerLoginUrl) {
		this.casServerLoginUrl = casServerLoginUrl;
	}

	public final void setGatewayStorage(GatewayResolver gatewayStorage) {
		this.gatewayStorage = gatewayStorage;
	}
}