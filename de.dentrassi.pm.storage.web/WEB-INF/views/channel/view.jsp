<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<h:main title="Channel - ${channel.id }">

<ul class="button-bar">
	<li><a class="pure-button" href="delete">Delete Channel</a></li>
	<li><a class="pure-button" href="add">Add Artifact</a></li>
</ul>

<table>

<tr>
	<th>ID</th>
	<th>Name</th>
	<th>size</th>
</tr>

<c:forEach items="${channel.artifacts }" var="artifact">
	<tr>
		<td>${artifact.id }</td>
		<td>${artifact.name }</td>
		<td>${artifact.size }</td>
		<td><a href="<c:url value="/artifact/${artifact.id}/get"/>">Download</a></td>
		<td><a href="<c:url value="/artifact/${artifact.id}/delete"/>">Delete</a></td>
	</tr>
</c:forEach>

</table>

</h:main>