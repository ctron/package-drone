<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:main title="Channels">

<h:buttonbar>
    <jsp:attribute name="before">
        <li><a class="btn btn-primary" role="button" href="channel/create">Create Channel</a></li>
    </jsp:attribute>
</h:buttonbar>

<table class="table table-striped" style="width: 100%" id="channels">

<thead>
	<tr>
    	<th>Name</th>
    	<th>ID</th>
	</tr>
</thead>

<tbody>
<c:forEach items="${channels}" var="channel">
<tr>
    <td class="channel-name"><a href="<c:url value="/channel/${channel.id }/view"/>">${channel.name }</a></td>
	<td class="channel-id"><a href="<c:url value="/channel/${channel.id }/view"/>">${channel.id }</a></td>
</tr>
</c:forEach>
</tbody>

</table>

</h:main>