<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>用户列表</title>
	<link href="<c:url value='/static/css/bootstrap.css' />" rel="stylesheet"></link>
	<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
</head>

<body>
	<div class="generic-container">
		<div class="panel panel-default">
			  <!-- Default panel contents -->
		  	<div class="panel-heading"><span class="lead">List of Users </span></div>
		  	<div class="tablecontainer">
				<table class="table table-hover">
		    		<thead>
			      		<tr>
					        <th>用户名</th>
					        <th>密码</th>
					        <th>邮箱</th>
					        <th>ID</th>
					        <th width="100"></th>
					        <th width="100"></th>
						</tr>
			    	</thead>
		    		<tbody>
					<c:forEach items="${users}" var="user">
						<tr>
							<td>${user.name}</td>
							<td>${user.password}</td>
							<td>${user.email}</td>
							<td>${user.id}</td>
							<td><a href="<c:url value='/edit-user-${user.id}' />" class="btn btn-success custom-width">修改</a></td>
							<td><a href="<c:url value='/delete-user-${user.id}' />" class="btn btn-danger custom-width">删除</a></td>
						</tr>
					</c:forEach>
		    		</tbody>
		    	</table>
		    </div>
		</div>
	 	<div class="well">
	 		<a href="<c:url value='/newuser' />">增加新用户</a>
	 	</div>
   	</div>
</body>
</html>