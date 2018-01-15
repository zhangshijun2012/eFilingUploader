package com.sinosoft.util.cas;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.CleanUpTimerTask;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.AbstractTicketValidationFilter;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.ProxyList;
import org.jasig.cas.client.validation.ProxyListEditor;
import org.jasig.cas.client.validation.TicketValidator;

import com.opensymphony.xwork2.interceptor.MethodFilterInterceptorUtil;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.WildcardHelper;
import com.sinosoft.util.StringHelper;

/**
 * 单点CAS的ticket校验.
 * 
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
public class Cas20ProxyReceivingTicketValidationFilter extends AbstractTicketValidationFilter {
	private static final String[] RESERVED_INIT_PARAMS = { "proxyReceptorUrl", "acceptAnyProxy", "allowedProxyChains",
			"casServerUrlPrefix", "proxyCallbackUrl", "renew", "exceptionOnValidationFailure",
			"redirectAfterValidation", "useSession", "serverName", "service", "artifactParameterName",
			"serviceParameterName", "encodeServiceUrl", "millisBetweenCleanUps" };
	private static final int DEFAULT_MILLIS_BETWEEN_CLEANUPS = 60000;
	private String proxyReceptorUrl;
	private Timer timer;
	private TimerTask timerTask;
	private int millisBetweenCleanUps;
	private ProxyGrantingTicketStorage proxyGrantingTicketStorage = new ProxyGrantingTicketStorageImpl();

	/** 不需要校验的地址,可使用通配符*,注意如果匹配的数据中包含/,则需要使用两个* */
	protected Set<String> exclude = new HashSet<String>();
	/** 需要校验的地址,可使用通配符* */
	protected Set<String> include = new HashSet<String>();

	protected void initInternal(FilterConfig filterConfig) throws ServletException {
		super.initInternal(filterConfig);
		setProxyReceptorUrl(getPropertyFromInitParams(filterConfig, "proxyReceptorUrl", null));

		String proxyGrantingTicketStorageClass = getPropertyFromInitParams(filterConfig,
				"proxyGrantingTicketStorageClass", null);

		if (proxyGrantingTicketStorageClass != null) {
			try {
				Class<?> storageClass = Class.forName(proxyGrantingTicketStorageClass);
				this.proxyGrantingTicketStorage = ((ProxyGrantingTicketStorage) storageClass.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		this.log.trace("Setting proxyReceptorUrl parameter: " + this.proxyReceptorUrl);
		this.millisBetweenCleanUps = Integer.parseInt(getPropertyFromInitParams(filterConfig, "millisBetweenCleanUps",
				Integer.toString(DEFAULT_MILLIS_BETWEEN_CLEANUPS)));

		if (!this.isIgnoreInitConfiguration()) {
			String include = filterConfig.getInitParameter(AuthenticationFilter.INCLUDE_URI_PARAMETER_NAME);
			if (StringHelper.isEmpty(include)) {
				include = getPropertyFromInitParams(filterConfig, AuthenticationFilter.CAS_INCLUDE_URI_PARAMETER_NAME,
						null);
			}
			if (!StringHelper.isEmpty(include)) {
				this.include.addAll(TextParseUtil.commaDelimitedStringToSet(include));
			}
			String exclude = filterConfig.getInitParameter(AuthenticationFilter.EXCLUDE_URI_PARAMETER_NAME);
			if (StringHelper.isEmpty(exclude)) {
				exclude = getPropertyFromInitParams(filterConfig, AuthenticationFilter.CAS_EXCLUDE_URI_PARAMETER_NAME,
						null);
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
	}

	public void init() {
		super.init();
		CommonUtils.assertNotNull(this.proxyGrantingTicketStorage, "proxyGrantingTicketStorage cannot be null.");

		if (this.timer == null) {
			this.timer = new Timer(true);
		}

		if (this.timerTask == null) {
			this.timerTask = new CleanUpTimerTask(this.proxyGrantingTicketStorage);
		}
		this.timer.schedule(this.timerTask, this.millisBetweenCleanUps, this.millisBetweenCleanUps);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected final TicketValidator getTicketValidator(FilterConfig filterConfig) {
		String allowAnyProxy = getPropertyFromInitParams(filterConfig, "acceptAnyProxy", null);
		String allowedProxyChains = getPropertyFromInitParams(filterConfig, "allowedProxyChains", null);
		String casServerUrlPrefix = getPropertyFromInitParams(filterConfig, "casServerUrlPrefix", null);
		Cas20ServiceTicketValidator validator;
		if ((CommonUtils.isNotBlank(allowAnyProxy)) || (CommonUtils.isNotBlank(allowedProxyChains))) {
			Cas20ProxyTicketValidator v = new Cas20ProxyTicketValidator(casServerUrlPrefix);
			v.setAcceptAnyProxy(parseBoolean(allowAnyProxy));
			v.setAllowedProxyChains(createProxyList(allowedProxyChains));
			validator = v;
		} else {
			validator = new Cas20ServiceTicketValidator(casServerUrlPrefix);
		}
		validator.setProxyCallbackUrl(getPropertyFromInitParams(filterConfig, "proxyCallbackUrl", null));
		validator.setProxyGrantingTicketStorage(this.proxyGrantingTicketStorage);
		validator.setProxyRetriever(new Cas20ProxyRetriever(casServerUrlPrefix));
		validator.setRenew(parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));

		Map additionalParameters = new HashMap();
		List params = Arrays.asList(RESERVED_INIT_PARAMS);

		for (Enumeration e = filterConfig.getInitParameterNames(); e.hasMoreElements();) {
			String s = (String) e.nextElement();

			if (!params.contains(s)) {
				additionalParameters.put(s, filterConfig.getInitParameter(s));
			}
		}

		validator.setCustomParameters(additionalParameters);

		return validator;
	}

	protected final ProxyList createProxyList(String proxies) {
		if (CommonUtils.isBlank(proxies)) {
			return new ProxyList();
		}

		ProxyListEditor editor = new ProxyListEditor();
		editor.setAsText(proxies);
		return (ProxyList) editor.getValue();
	}

	public void destroy() {
		super.destroy();
		this.timer.cancel();
	}

	protected final boolean preFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		if (AuthenticationFilter.exclude(request, exclude, include)) {
			filterChain.doFilter(servletRequest, servletResponse);
			return false;
		}
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String requestUri = request.getRequestURI();

		if ((CommonUtils.isEmpty(this.proxyReceptorUrl)) || (!requestUri.endsWith(this.proxyReceptorUrl))) {
			return true;
		}

		CommonUtils.readAndRespondToProxyReceptorRequest(request, response, this.proxyGrantingTicketStorage);

		return false;
	}

	public final void setProxyReceptorUrl(String proxyReceptorUrl) {
		this.proxyReceptorUrl = proxyReceptorUrl;
	}

	public void setProxyGrantingTicketStorage(ProxyGrantingTicketStorage storage) {
		this.proxyGrantingTicketStorage = storage;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public void setTimerTask(TimerTask timerTask) {
		this.timerTask = timerTask;
	}

	public void setMillisBetweenCleanUps(int millisBetweenCleanUps) {
		this.millisBetweenCleanUps = millisBetweenCleanUps;
	}
}