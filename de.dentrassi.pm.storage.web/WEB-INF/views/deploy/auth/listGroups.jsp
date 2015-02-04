<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="de.dentrassi.pm.storage.web.deploy.DeployAuthController"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
pageContext.setAttribute ( "TAG", DeployAuthController.GROUP_ACTION_TAG );
%>

<h:main title="Deploy Groups">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<h:defaultPager />

<table class="table table-condensed table-striped">

<thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Keys</th>
    </tr>
</thead>

<tbody>
    <c:forEach var="group" items="${groups }">
    <tr>
        <td><a href="<c:url value="/deploy/auth/group/${group.id }/view"/>">${fn:escapeXml(group.id) }</a></td>
        <td>${fn:escapeXml(group.name) }</td>
        <td>${group.keys.size() }</td>
    </tr>
    </c:forEach>    
</tbody>

</table>

<h:defaultPager />

</h:main>