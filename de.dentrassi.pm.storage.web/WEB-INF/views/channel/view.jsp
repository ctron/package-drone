<%@page import="java.util.Comparator"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="de.dentrassi.pm.storage.service.Artifact"%>
<%@page import="de.dentrassi.pm.storage.service.Channel"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrass.de/pm" prefix="pm" %>

<h:main title="Channel - ${pm:channel(channel) }">

<ul class="button-bar">
    <li><a class="pure-button" href="edit">Edit Channel</a></li>
	<li><a class="pure-button" href="delete">Delete Channel</a></li>
	<li><a class="pure-button" href="add">Add Artifact</a></li>
	<li><a class="pure-button" href="clear">Clear Channel</a></li>
	<li><a class="pure-button" href="aspects">Configure Aspects</a></li>
</ul>

<table class="full">

<thead>
	<tr>
		<th>Name</th>
		<th>Size</th>
		<th>Created</th>
		<th></th>
		<th></th>
		<th></th>
		<th></th>
	</tr>
</thead>

<tbody>
<c:forEach items="${sortedArtifacts }" var="artifact">
	<tr>
		<td>${fn:escapeXml(artifact.name) }</td>
		<td>${fn:escapeXml(artifact.size) }</td>
		<td style="white-space: nowrap;"><fmt:formatDate value="${artifact.creationTimestamp }" type="both" /> </td>
		<td><a href="<c:url value="/artifact/${artifact.id}/get"/>">Download</a></td>
		<td>
		  <c:if test="${not artifact.derived }"><a href="<c:url value="/artifact/${artifact.id}/delete"/>">Delete</a></c:if>
		</td>
		<td><a href="<c:url value="/artifact/${artifact.id}/view"/>">Details</a></td>
		<td><a href="<c:url value="/artifact/${artifact.id}/dump"/>">View</a></td>
	</tr>
</c:forEach>
</tbody>

</table>

</h:main>