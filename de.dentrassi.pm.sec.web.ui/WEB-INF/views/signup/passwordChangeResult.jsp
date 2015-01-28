<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="de.dentrassi.pm.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Password change">

<div class="container-fluid"><div class="row">

<div class="col-xs-offset-1 col-xs-10">

<c:choose>

	<c:when test="${not empty error }">
	
	   <div class="alert alert-danger">
            ${fn:escapeXml(error) }
        </div>
	
	</c:when>
	
	<c:otherwise>
	
		<div class="alert alert-success">
		    <strong>Password changed!</strong> You can now <a class="alert-link" href="<c:url value="/login"/>">log in</a> using your new password.
		</div>
		
	</c:otherwise>

</c:choose>

</div></div></div>

</h:main>