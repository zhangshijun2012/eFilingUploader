<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:if test="#request.uri != null">
<%-- 如果在request属性中指定了uri参数,则需要跳转至对应的uri.此项目下只有login.html进行了单点判断,所以均跳转至此页面,然后重新调回uri --%>
<html>
	<script type="text/javascript" src="resources/javascript/base.js"></script>
	<script type="text/javascript">
		var url = SERVER_ROOT + 'login.html?url=' + encodeURIComponent('<s:property value="#request.uri" escape="false" />');
	<s:iterator value="#parameters" var="param">
		var key = encodeURIComponent('<s:property value="#param.key" />');
		var value;
		<s:iterator value="#param.value" var="paramValue" status="st">
			value = encodeURIComponent('<s:property value="#paramValue" />');
			url += '&' + key + '=' + value;
		</s:iterator>
	</s:iterator>
		// alert(url);
		window.location = url;
	</script>
</html>
</s:if>
<s:else>
<%-- 输出struts2的错误信息 --%>
<s:property value="getErrorJSONObject()" escape="false" />
</s:else>