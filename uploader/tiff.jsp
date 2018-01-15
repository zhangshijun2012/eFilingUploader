<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- 用于tiff图片的查看,使用控件查看 --%>
<!DOCTYPE HTML>
<html>
<head>
<title>${ entity.fileTitle } - TIFF图片</title>
<style type="text/css">
html, body {
	margin: 0;
	pading: 0;
	width: 100%;
	height: 100%;
}
</style>
<script type="text/javascript">
	function init() {
		if (!fileViewer) fileViewer = document.getElementById('fileViewer');
		try {
			// 默认为正常的宽度和高度
			fileViewer.setValue(5, 0);
		} catch (error) {
			// 可能是控件没有正常安装
			var html = ''
				+ '<p>'
				+ '	<font color="red">如果未能自动安装TIFF控件，请点击这里下载安装文件进行手工安装：</font>'
				+ '		<a href="alternatiff-pl-w32-2.0.6.exe">32位Windows</a>,'
				+ '		<a href="alternatiff-pl-w64-2.0.6.exe">64位Windows</a>'
				+ '</p>';
			document.body.innerHTML += html;
		}
	}
</script>
</head>
<body onload="init()">
	<object width="100%" height="100%" id="fileViewer" name="fileViewer"
		classid="CLSID:106E49CF-797A-11D2-81A2-00E02C015623"
		codebase="alternatiff-ax-w32-2.0.5.cab#version=2,0,5,1">
		<param name="src" value="read.do?id=${ id }">
		<param name="href" value="#">
	</object>
</body>
</html>