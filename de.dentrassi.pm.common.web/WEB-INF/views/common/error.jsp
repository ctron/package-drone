<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<%
pageContext.setAttribute ( "showStackTrace", Boolean.getBoolean ( "package.drone.showStackTrace" ) );
%>
<h:main title="${title }">

<c:choose>

    <c:when test="${showStackTrace }">
    
	    <h:error title="${fn:escapeXml(result) }" icon="flash">
	       <p>${fn:escapeXml(message) }</p>
		      <pre>${fn:escapeXml(stacktrace) }</pre>
	    </h:error>
    
    </c:when>
    
    <c:otherwise>
        <div class="container">
            <div class="row">
                <div class="col-md-offset-2 col-md-8">
                    <div class="alert alert-danger"><strong>${fn:escapeXml(result) }!</strong> ${fn:escapeXml(message) }</div>
                </div>
            </div>
        </div>
    </c:otherwise>

</c:choose>




</h:main>