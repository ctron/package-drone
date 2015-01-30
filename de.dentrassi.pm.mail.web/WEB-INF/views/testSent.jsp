<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="de.dentrassi.pm.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Test mail" subtitle="Result">

<div class="container-fluid"><div class="row">

<div class="col-xs-offset-1 col-xs-10">

<c:choose>

	<c:when test="${not empty message }">
	
	   <div class="alert alert-danger">
	       <strong>Test message failed!</strong> ${fn:escapeXml(message) }
	   </div>
	   
	   <pre>${fn:escapeXml(stacktrace) }</pre>
	</c:when>
	
	<c:otherwise>
	
		<div class="alert alert-success">
		    <strong>Mail sent!</strong> The test e-mail has been handed over to the first mail server.
		</div>
		
	</c:otherwise>

</c:choose>

</div></div></div>

</h:main>