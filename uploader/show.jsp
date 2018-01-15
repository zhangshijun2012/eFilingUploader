<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- 用于tiff图片的查看,使用控件查看 --%>
<!DOCTYPE HTML>
<html>
<head>
<title>文件预览</title>
	<link type="text/css" rel="stylesheet" href="resources/template/default/css/uploader.css">
	<script type="text/javascript" src="resources/javascript/prototype-1.7.1.js"></script>
	<script type="text/javascript" src="resources/javascript/prototype-helper.js"></script>
	
	<script type="text/javascript" src="resources/javascript/base.js"></script>
	
	<script type="text/javascript" src="resources/javascript/helper.js"></script>
	<script type="text/javascript" src="resources/javascript/string.js"></script>
	<script type="text/javascript" src="resources/javascript/number.js"></script>
	<script type="text/javascript" src="resources/javascript/ajax.js"></script>
	
	<script type='text/javascript' src="resources/helper.js"></script>
	<script type='text/javascript' src="resources/FileIndexService.js"></script>
	<script type="text/javascript">
		FileIndexService.parameters = <s:property value="#request.queryParameters" escape="false" />;
		//|| getParameter('FileIndexService.downloadDisabled');	
		var downloadDisabled = FileIndexService.parameters['downloadDisabled'] || FileIndexService.parameters['FileIndexService.downloadDisabled'];
		if (downloadDisabled) {
			// 有此参数,根据此参数判断是否禁用下载功能
			downloadDisabled = Boolean.parseBoolean(downloadDisabled);
			FileIndexService.downloadDisabled = downloadDisabled;
		}
		FileIndexService.data = <s:property value="pagingEntity.toJSONString()" escape="false" />;
		function init() {
			FileIndexService.list('images');
		}
	</script>
</head>
<body onload="init()">
	<div id="images" class="preview"><!-- 图片上传后的预览窗口 --></div>
	<div id="more" class="previewContainer" style="display: none">
		<a href="javascript: void(0)" onclick="FileIndexService.next()"><img src="resources/template/default/images/more.png" />更多/more...</a>
	</div>
</body>
</html>