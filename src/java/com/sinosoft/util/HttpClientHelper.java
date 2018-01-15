package com.sinosoft.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.sinosoft.efiling.util.SystemUtils;

public class HttpClientHelper {
	static String trim(Object o) {
		return o == null ? "" : o.toString().trim();
	}

	public static final String ENCODING = SystemUtils.ENCODING;// "utf-8";

	/**
	 * 访问url并获取其返回的html字符串
	 * 
	 * @param url
	 *            地址
	 * @param parameters
	 *            参数,可以是key=value&key2=value2形式的字符串,或者HttpEntity,Map,List<NameValuePair>,NameValuePair对象
	 * @param sendEncoding
	 *            发送使用的字符串编码
	 * @param reciveEncoding
	 *            接收的编码
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public static final String get(String url, Object parameters, String sendEncoding, String reciveEncoding) {
		final DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost method = new HttpPost(url);
		// System.out.println(method.getParams().getParameter("UserID"));
		HttpEntity entity = null;
		if (parameters != null) {
			if (parameters instanceof HttpEntity) {
				entity = ((HttpEntity) parameters);
			} else if ((parameters instanceof Map && ((Map) parameters).size() > 0)
					|| (parameters instanceof List && ((List) parameters).size() > 0)
					|| parameters instanceof NameValuePair) {
				// MultipartEntity reqEntity = new MultipartEntity(); //文件表单
				List<NameValuePair> data = null;
				if (parameters instanceof List) {
					data = (List) parameters;
				} else {
					data = new ArrayList<NameValuePair>();

					if (parameters instanceof NameValuePair) {
						data.add((NameValuePair) parameters);
					} else {
						Map map = (Map) parameters;
						Iterator keys = map.keySet().iterator();
						Object key = null;
						Object o = null;
						Object[] value = null;
						while (keys.hasNext()) {
							key = keys.next();
							o = map.get(key);
							if (o == null) {
								continue;
							}

							if (o instanceof Object[]) {
								value = (Object[]) o;
							} else {
								value = new Object[] { o };
							}

							for (int i = 0, l = value.length; i < l; i++) {
								data.add(new BasicNameValuePair(trim(key), trim(value[i])));
							}
						}
					}

				}

				try {
					// System.out.println(URLEncodedUtils.format(data,
					// sendEncoding));
					entity = (new UrlEncodedFormEntity(data, sendEncoding));
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成 catch 块
					e.printStackTrace();
				}
			} else if (parameters instanceof String) {
				// 直接视parameters为参数字符串key=value&..
				try {
					entity = new StringEntity(trim(parameters), sendEncoding);

					((StringEntity) entity).setContentType(URLEncodedUtils.CONTENT_TYPE + HTTP.CHARSET_PARAM
							+ (sendEncoding != null ? sendEncoding : HTTP.DEFAULT_CONTENT_CHARSET));
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成 catch 块
					e.printStackTrace();
				}
			}

			if (entity != null) {
				method.setEntity(entity);
			}
		}

		HttpResponse response = null;
		String text = null;
		try {
			response = httpclient.execute(method);
			entity = response.getEntity();
		} catch (ClientProtocolException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}

		try {
			text = entity == null ? null : EntityUtils.toString(entity, reciveEncoding);
		} catch (ParseException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}

		httpclient.getConnectionManager().shutdown();

		return trim(text);
	}

	public static final String get(String url, Object parameters, String encoding) {
		return get(url, parameters, encoding, encoding);
	}

	public static final String get(String url, Object parameters) {
		return get(url, parameters, ENCODING, ENCODING);
	}

	public static final String get(String url) {
		return get(url, null, ENCODING, ENCODING);
	}
}
