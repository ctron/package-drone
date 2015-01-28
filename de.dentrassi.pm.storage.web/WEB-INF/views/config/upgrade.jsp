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
<a href="<c:url value="/setup"/>" class="btn btn-default">Back</a>
</p>

</h:main>