<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
<script type="text/javascript" src="../resources/javascript/prototype-1.7.1.js"></script>
<script type="text/javascript" src="../js/security.js"></script>
</head>
<body>
	<form id="modifyName">
		<div align="center">
			<table>
				<tbody>
					<tr>
						<td>
							<label>标识码:</label>
						</td>
						<td >
							<input name="uniqueCode" id="uniqueCode"  />
						</td>
					</tr>
					<tr>
						<td>
							<label>系统名称:</label>
						</td>
						<td>
							<input name="systemName" id="systemName"  />
						</td>
					</tr>
				    <tr>
						<td>
							<label>原密码:</label>
						</td>
						<td>
							<input name="oldPassword" id="oldPassword"  />
						</td>
					</tr>
					<tr>
						<td>
							<label>新密码:</label>
						</td>
						<td>
							<input name="newPassword" id="newPassword"  />
						</td>
					</tr>
					<tr align="center">
						<td colspan="6">
							<!-- 
						    <input name="modifyButton" id="modifyButton" type="button" 
								value="新增" onclick="Security.append()" />
						     -->
							<input name="modifyButton" id="modifyButton" type="button" 
								value="修改" onclick="Security.modify()" />
							<input name="cancellButton" id="cancellButton" type="button" 
								value="取消" onclick="Security.cancel()" />
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</form>
</body>
</html>