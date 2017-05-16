<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Album Creation Form</title>
	<link href="<c:url value='/static/css/bootstrap.min.css' />" rel="stylesheet"></link>
	<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
</head>

<body>

 	<div class="generic-container">
	<div class="well lead">Album Creation Form</div>
 	<form:form method="POST" modelAttribute="album" class="form-horizontal">
		<form:input type="hidden" path="id" id="id"/>
		
		<div class="row">
			<div class="form-group col-md-12">
				<label class="col-md-3 control-lable" for="title">Title</label>
				<div class="col-md-7">
					<form:input type="text" path="title" id="title" class="form-control input-sm"/>
					<div class="has-error">
						<form:errors path="title" class="help-inline"/>
					</div>
				</div>
			</div>
		</div>

		<div class="row">
			<div class="form-actions floatRight">
				<c:choose>
					<c:when test="${edit}">
						<input type="submit" value="Update" class="btn btn-primary btn-sm"/> or <a href="<c:url value='albums' />">Cancel</a>
					</c:when>
					<c:otherwise>
						<input type="submit" value="Create" class="btn btn-primary btn-sm"/> or <a href="<c:url value='albums' />">Cancel</a>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<sec:authorize access="hasRole('ADMIN') ">
		<c:if test="${edit}">
			<span class="well pull-left">
				<a href="<c:url value='/admin/add-image-${album.id}' />">Click here to upload/manage your images</a>
			</span>
		</c:if>
		</sec:authorize>
		
	</form:form>
	</div>
</body>
</html>