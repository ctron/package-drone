<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/storage" prefix="s" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="storage" %>
<%@ taglib uri="http://dentrassi.de/osgi/web" prefix="web" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }" />

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="table-responsive">
	<table id="messages" class="table table-striped table-hover">
	
	<thead>
	    <tr>
	        <th>Message</th>
	        <th>Source</th>
	        <th>Artifacts</th>
	    </tr>
	</thead>
	
	<tbody>
	<c:forEach items="${messages }" var="msg">
	    <tr class="${storage:severity(msg.severity) }">
	        <td>${fn:escapeXml(msg.message) }</td>
	        <td nowrap="nowrap">${fn:escapeXml( aspects[msg.aspectId].label ) }</td>
	        <td nowrap="nowrap">
	        <ul>
	            <c:forEach items="${msg.artifactIds }" var="id"><li><a href="<c:url value="/artifact/${id}/view"/>">${id }</a></li></c:forEach>
	        </ul>
	        </td>
	    </tr>
	</c:forEach>
	</tbody>
	
	</table>

</div>


</h:main>