<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
<title>Login page - step 2</title>
</head>
<body>
	<form action="${ctxPath}/j_superfly_security_check" method="POST">
		<table>
			<tr>
				<td>
					Role
				</td>
				<td>
					<select name="j_role">
						<c:forEach items="${superflyRoles}" var="role">
							<option value="${role.name}">${role.name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>
		<input type="submit">
	</form>
</body>
</html>