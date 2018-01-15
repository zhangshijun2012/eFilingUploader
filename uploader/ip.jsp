<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="com.sinosoft.util.HttpHelper,java.net.InetAddress"%>
服务端: <%=InetAddress.getLocalHost()%>, 
Server Name: <%=request.getServerName()%>,
客户端IP: <%=HttpHelper.getClientIP(request)%>