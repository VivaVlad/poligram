<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Albums List</title>
	<link href="<c:url value='/static/css/bootstrap.min.css' />" rel="stylesheet"></link>
	<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
</head>
	<body>
		<div class="generic-container">
			<div class="panel panel-default">
				  <!-- Default panel contents -->
				<div class="panel-heading"><span class="lead">List of Albums </span></div>
				<div class="tablecontainer">
					<table class="table table-hover">
						<thead>
							<tr>
								<th>Title</th>
								<th width="100"></th>
								<th width="100"></th>
								<th width="100"></th>
							</tr>
						</thead>
						<tbody>
						<c:forEach items="${albums}" var="album">
							<tr>
								<td>${album.title}</td>
								<td><a href="<c:url value='/edit-album-${album.id}' />" class="btn btn-success custom-width">edit</a></td>
								<td><a href="<c:url value='/album-${album.id}' />" class="btn btn-primary custom-width">images</a></td>
								<td><a href="<c:url value='/delete-album-${album.id}' />" class="btn btn-danger custom-width">delete</a></td>
							</tr>
						</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
			<div class="well">
				<a href="<c:url value='/newalbum' />">Add New Album</a>
			</div>
		</div>
	</body>
</html>