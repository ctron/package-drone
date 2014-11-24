<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Channel - ${channel.id }">

<ul class="button-bar">
	<li><a class="pure-button" href="delete">Delete Channel</a></li>
	<li><a class="pure-button" href="add">Add Artifact</a></li>
	<li><a class="pure-button" href="aspects">Configure Aspects</a></li>
</ul>

<table>

<tr>
	<th>Name</th>
	<th>Size</th>
</tr>

<c:forEach items="${channel.artifacts }" var="artifact">
	<tr>
		<td>${fn:escapeXml(artifact.name) }</td>
		<td>${fn:escapeXml(artifact.size) }</td>
		<td><a href="<c:url value="/artifact/${artifact.id}/get"/>">Download</a></td>
		<td><a href="<c:url value="/artifact/${artifact.id}/delete"/>">Delete</a></td>
		<td><a href="<c:url value="/artifact/${artifact.id}/view"/>">View</a></td>
	</tr>
</c:forEach>

</table>

</h:main>