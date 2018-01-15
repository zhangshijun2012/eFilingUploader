package com.sinosoft.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;

public class HttpHelper {
	/**
	 * 使用http的get请求读取url的html内容
	 * 
	 * @param url
	 * @param parameters url的参数
	 * @param charset url服务端使用的编码
	 * @return
	 */
	public static String get(String url, String parameters, String charset) {
		if (!StringHelper.isEmpty(parameters)) {
			url = (url.indexOf("?") < 0 ? "?" : "&") + parameters;
		}
		return get(url, charset);
	}

	/**
	 * 读取url地址的html内容
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url) {
		return get(url, null);
	}

	/**
	 * 使用http的get请求读取url的html内容
	 * 
	 * @param url
	 * @param charset url服务端使用的编码
	 * @return
	 */
	public static String get(String url, String charset) {
		URL u;
		URLConnection connection;
		InputStream in = null;
		BufferedReader reader = null;
		StringBuilder html = new StringBuilder();
		try {
			u = new URL(url);
			connection = u.openConnection();
			in = connection.getInputStream();
			reader = new BufferedReader(StringHelper.isEmpty(charset) ? new InputStreamReader(in)
					: new InputStreamReader(in, charset));
			String line;
			while ((line = reader.readLine()) != null) {
				html.append(line).append(SystemHelper.ENTER);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			MethodHelper.close(in);
			MethodHelper.close(reader);
		}

		return html.toString();
	}

	/**
	 * 
	 * 发送post请求,读取url返回的html内容
	 * 
	 * @param url
	 * @param parameters POST参数
	 * @return
	 */
	public static String post(String url, String parameters) {
		return post(url, parameters, null);
	}

	/**
	 * 发送post请求,读取url返回的html内容
	 * 
	 * @param url
	 * @param parameters post的参数,可以为null
	 * @param charset url服务端使用的编码
	 * @return
	 */
	public static String post(String url, String parameters, String charset) {
		URL u;
		URLConnection connection;
		InputStream in = null;
		BufferedReader reader = null;
		OutputStream out = null;
		OutputStreamWriter writer = null;
		StringBuilder html = new StringBuilder();
		try {
			u = new URL(url);
			connection = u.openConnection();
			if (!StringHelper.isEmpty(parameters)) {
				connection.setDoOutput(true);
				out = connection.getOutputStream();
				writer = StringHelper.isEmpty(charset) ? new OutputStreamWriter(out) : new OutputStreamWriter(out,
						charset);
				writer.write(parameters);
			}
			in = connection.getInputStream();
			reader = new BufferedReader(StringHelper.isEmpty(charset) ? new InputStreamReader(in)
					: new InputStreamReader(in, charset));
			String line;
			while ((line = reader.readLine()) != null) {
				html.append(line).append(SystemHelper.ENTER);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			MethodHelper.close(in);
			MethodHelper.close(reader);
		}

		return html.toString();
	}

	/** 用于获得ip的header name */
	public static final String[] headers = { "x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP" };

	/**
	 * 获得request请求的客户端IP
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientIP(HttpServletRequest request) {
		String ip;
		for (String header : headers) {
			ip = request.getHeader(header);
			if (!StringHelper.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
		}
		return request.getRemoteAddr();
	}

}
