<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Artifact" subtitle="${fn:escapeXml(artifact.information.name) } (${fn:escapeXml(artifact.id) })">

<h:buttonbar menu="${menuManager.getActions(artifact) }" />

<ul class="nav nav-tabs" role="tablist">
    <li role="presentation" class="active"><a href="#home" aria-controls="home" role="tab" data-toggle="tab">Meta Data</a></li>
    <li role="presentation"><a href="#relations" aria-controls="relations" role="tab" data-toggle="tab">Relations</a></li>
    <li role="presentation"><a href="#info" aria-controls="info" role="tab" data-toggle="tab">Information</a></li>
</ul>

<p>
 
<div class="tab-content">

<%-- META DATA --%>

<div role="tabpanel" class="tab-pane active" id="home">
<h:metaDataTable metaData="${artifact.information.metaData }"/>
</div>

<%-- INFO --%>

<div role="tabpanel" class="tab-pane" id="info">
<table class="table table-condensed">
<tr><th>Name</th><td>${fn:escapeXml(artifact.information.name) }</td></tr>
<tr><th>Facets</th><td><c:forEach var="i" items="${artifact.information.facets }"><span class="label label-default">${fn:escapeXml(i) }</span> </c:forEach></td></tr>
</table>
</div>

<%-- RELATIONS --%>

<div role="tabpanel" class="tab-pane" id="relations">
<dl class="dl-horizontal">

<c:if test="${not empty artifact.information.parentId }">
<dt>Parent<dt><dd><a href="<c:url value="/artifact/${artifact.information.parentId }/view"/>">${ artifact.information.parentId }</a></dd>
</c:if>

<c:if test="${not empty artifact.information.childIds }">
<dt>Children<dt><dd>
    <ul>
        <c:forEach var="child" items="${artifact.information.childIds }">
          <li><a href="<c:url value="/artifact/${child }/view"/>">${ child }</a></li>
        </c:forEach>
    </ul>
</dd>
</c:if>
</dl>
</div>

<%-- end of tab --%>
</div><%-- tabpanel --%>

</h:main>