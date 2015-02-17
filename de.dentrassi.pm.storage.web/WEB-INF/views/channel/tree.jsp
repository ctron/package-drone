<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="storage" %>
<%@ taglib uri="http://dentrassi.de/osgi/web" prefix="web" %>


<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<web:define name="list">
	<c:forEach var="artifact" items="${storage:nameSorted ( artifacts ) }">
		<tr data-level="${level }">
		    <td style="padding-left: ${level*2}em;">
		      <a href="<c:url value="/artifact/${artifact.id}/view"/>">${fn:escapeXml(artifact.name) }</a>
		    </td>
		    
		    <td>${artifact.size }</td>
		    
		    <td style="white-space: nowrap;"><fmt:formatDate value="${artifact.creationTimestamp }" type="both" /> </td>
		    
		    <td><a href="<c:url value="/artifact/${artifact.id}/get"/>">Download</a></td>
	        <td>
	          <c:if test='${artifact.is("deletable") and manager}'><a href="<c:url value="/artifact/${artifact.id}/delete"/>">Delete</a></c:if>
	        </td>
	        <td><a href="<c:url value="/artifact/${artifact.id}/dump"/>">View</a></td>
		</tr>
		
		<web:call name="list" artifacts="${map.get(artifact.id) }" level="${level+1 }"/>
	</c:forEach>
</web:define>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }" />

<h:nav menu="${menuManager.getViews(channel) }" />

<div class="table-responsive">
	<table id="artifacts" class="table table-striped table-condensed table-hover">
	
		<thead>
		    <tr>
		        <th>Name</th>
		        <th>Size</th>
		        <th>Created</th>
		        <th></th>
		        <th></th>
		        <th></th>
		    </tr>
		</thead>
	
		<tbody>
		    <web:call name="list" map="${treeArtifacts }" artifacts="${treeArtifacts.get(null) }" level="${0 }"/>
		</tbody>
	
	</table>
</div>

</h:main>