<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:main title="Database Upgrade">

<table class="table">

<tr><th>Database Schema Version</th><td>${databaseSchemaVersion }</td></tr>
<tr><th>Current Schema Version</th><td>${currentVersion }</td></tr>
<tr><th>Service Present</th><td>${servicePresent }</td></tr>

</table>

<p>
<c:choose>
    <c:when test="${mailServicePresent}">
        <a href="<c:url value="/config"/>" class="btn btn-default">Back</a>
    </c:when>
    <c:otherwise>
        <a href="<c:url value="/setup"/>" class="btn btn-default">Back</a>
    </c:otherwise>
</c:choose>

</p>

</h:main>