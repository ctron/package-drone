<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:main title="Channels">

<ul class="button-bar">
<li><a class="btn btn-primary" role="button" href="channel/create">Create Channel</a></li>
</ul>

<table class="table table-striped" style="width: 100%">

<thead>
	<tr>
    	<th>Name</th>
    	<th>ID</th>
	</tr>
</thead>

<tbody>
<c:forEach items="${channels}" var="channel">
<tr>
    <td><a href="<c:url value="/channel/${channel.id }/view"/>">${channel.name }</a></td>
	<td><a href="<c:url value="/channel/${channel.id }/view"/>">${channel.id }</a></td>
</tr>
</c:forEach>
</tbody>

</table>

</h:main>