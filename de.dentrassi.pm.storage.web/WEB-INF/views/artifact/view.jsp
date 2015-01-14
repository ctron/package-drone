<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Artifact - ${fn:escapeXml(artifact.id) }">

<ul class="button-bar">
	<li><a class="btn btn-danger" href="delete"><span class="glyphicon glyphicon-trash"></span> Delete</a></li>
	
	<c:if test="${artifact.stored }">
	<li><a class="btn btn-primary" href="attach">Attach Artifact</a></li>
	</c:if>
	
	<c:if test="${artifact.generator }">
	<li><a class="btn btn-success" href="<c:url value="/artifact/${artifact.id }/generate"/>"><span class="glyphicon glyphicon-refresh"></span> Regenerate</a></li>
	   <c:if test="${not empty artifact.editTarget }">
	       <li><a class="btn btn-primary" href="<c:url value="${artifact.editTarget.render(pageContext) }"/>">Edit</a></li>
	   </c:if>
	</c:if>
	
	<li><a class="btn btn-default"  href="<c:url value="/channel/${artifact.channel.id }/view"/>">Channel</a></li>
	<li><a href="get" class="btn btn-link">Download</a></li>
</ul>

<h2>Relations</h2>
<ul>
<c:if test="${not empty artifact.parentId }">
<li><a href="<c:url value="/artifact/${artifact.parentId }/view"/>">Parent</a></li>
</c:if>
</ul>

<h2>Meta Data</h2>

<table>
<tr><th>Name:</th><td>${fn:escapeXml(artifact.name) }</td></tr>
</table>

<table class="table table-condensed">

<tr>
	<th>Namespace</th>
	<th>Key</th>
	<th>Value</th>
</tr>

<c:forEach items="${artifact.metaData }" var="entry">
	<tr>
		<td>${fn:escapeXml(entry.key.namespace) }</td>
		<td style="white-space: nowrap;">${fn:escapeXml(entry.key.key) }</td>
		<td style="white-space: pre;"><code>${fn:escapeXml(entry.value) }</code></td>
	</tr>
</c:forEach>

</table>

</h:main>