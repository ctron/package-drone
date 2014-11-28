<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:main title="Channel not found - ${channelId }">

<ul class="button-bar">
<li><a class="pure-button" href="<c:url value="/channel"/>">All channels</a></li>
</ul>

<h:error title="Not found">Channel ${channelId } does not exists!</h:error>

</h:main>