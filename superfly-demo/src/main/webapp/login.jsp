<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<html>
<head>
<title>Login page</title>
</head>
<body>
	${reason}
	<form action="<%= request.getContextPath() %>/j_superfly_security_check" method="POST">
		<table>
			<tr>
				<td>
					Username
				</td>
				<td>
					<input name="j_username">
				</td>
			</tr>
			<tr>
				<td>
					Password
				</td>
				<td>
					<input name="j_password" type="password">
				</td>
			</tr>
		</table>
		<input type="submit">
	</form>
</body>
</html>