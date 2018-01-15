<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- 用于tiff图片的查看,使用控件查看 --%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>${ entity.fileTitle } - 图片</title>
<link type="text/css" rel="stylesheet" href="resources/template/default/css/ImageViewer.css">
<script type="text/javascript" src="resources/ImageViewer.js"></script>
<script type="text/javascript">
	var viewer;
	var init = function(img) {
		viewer = new ImageViewer(img, {
			downloadUrl : 'download.do?id=${ id }'		
		});
	};
</script>
</head>
<body onload="init('image');">
	<div class="viewer">
		<img id="image" src="read.do?id=${ id }" />
	</div>
</body>
</html>