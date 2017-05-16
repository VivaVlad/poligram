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
    <div class="background"></div>
        <div class="image" style="background-image: url('/image?id=${image.id}');">
            <a href="/admin/delete-image-${album.id}-${image.id}">
                <span class="glyphicon glyphicon-remove"></span>
            </a>
        </div>

    </body>
</html>