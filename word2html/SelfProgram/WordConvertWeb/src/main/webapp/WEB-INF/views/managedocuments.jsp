<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<!-- <title>Upload/Download/Delete Documents</title> -->
	<title>文档管理</title>
	<link href="<c:url value='/static/css/bootstrap.css' />" rel="stylesheet"></link>
	<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
</head>

<body>
	<div class="generic-container">
		<div class="panel panel-default">
			  <!-- Default panel contents -->
		  	<div class="panel-heading"><span class="lead">文件列表</span></div>
		  	<div class="tablecontainer">
				<table class="table table-hover">
		    		<thead>
			      		<tr>
					        <th>序号</th>
					        <th>文件名</th>
					        <th>类型</th>
					        <th>描述</th>
					        <th width="100"></th>
					        <th width="100"></th>
						</tr>
			    	</thead>
		    		<tbody>
					<c:forEach items="${documents}" var="doc" varStatus="counter">
						<tr>
							<td>${counter.index + 1}</td>
							<td>${doc.name}</td>
							<td>${doc.type}</td>
							<td>${doc.description}</td>
							<td><a href="<c:url value='/view-document-${user.id}-${doc.id}' />" class="btn btn-success custom-width">预览</a></td>
							<td><a href="<c:url value='/download-document-${user.id}-${doc.id}' />" class="btn btn-success custom-width">下载</a></td>
							<c:if test="${admin}">
								<td><a href="<c:url value='/delete-document-${user.id}-${doc.id}' />" class="btn btn-danger custom-width">删除</a></td>
							</c:if>
						</tr>
					</c:forEach>
		    		</tbody>
		    	</table>
		    </div>
		</div>
		
		<c:if test="${admin}">
			<div class="panel panel-default">				
				<div class="panel-heading"><span class="lead">上传新文档</span></div>
				<div class="uploadcontainer">
					<form:form method="POST" modelAttribute="fileBucket" enctype="multipart/form-data" class="form-horizontal">
				
						<div class="row">
							<div class="form-group col-md-12">
								<label class="col-md-3 control-lable" for="file">上传文档</label>
								<div class="col-md-7">
									<form:input type="file" accept=".docx" path="file" id="file" class="form-control input-sm"/>
									<div class="has-error">
										<form:errors path="file" class="help-inline"/>
									</div>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="form-group col-md-12">
								<label class="col-md-3 control-lable" for="file">文档描述</label>
								<div class="col-md-7">
									<form:input type="text" path="description" id="description" class="form-control input-sm"/>
								</div>
								
							</div>
						</div>
				
						<div class="row">
							<div class="form-actions floatRight">
								<input type="submit" value="上传" class="btn btn-primary btn-sm">
							</div>
						</div>
		
					</form:form>
					</div>
			</div>
		 	<div class="well">
		 		<a href="<c:url value='/list' />">用户列表</a>
		 	</div>
		 	<div class="well">
		 		<a href="<c:url value='/logout' />">退出登录</a>
		 	</div>
	 	</c:if>
   	</div>
</body>
</html>