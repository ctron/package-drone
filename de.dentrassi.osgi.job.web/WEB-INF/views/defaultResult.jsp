<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="de.dentrassi.osgi.job.JobHandle"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% 
JobHandle job = (JobHandle)pageContext.getRequest ().getAttribute ( "job" );

if ( job != null && job.isComplete () && job.getError () == null )
    pageContext.setAttribute ( "type", "success");
else if ( job != null &&  job.isComplete (  ) && job.getError (   ) != null  )
    pageContext.setAttribute ( "type", "danger");
else
    pageContext.setAttribute ( "type", "default");
%>

<h:main title="Job failed" subtitle="${fn:escapeXml(job.label) }">

<div class="container">

	<div class="row">
		<div class="col-md-12">
		
			<c:choose>
			    <c:when test="${empty job }">Job not found!</c:when>
			    <c:otherwise>
			
					<div class="panel panel-${type }">
					    <div class="panel-heading">
					        <h4 class="panel-title">${fn:escapeXml(job.error.message) }</h4>
					    </div>
					    
					    <div class="panel-body">
                            <pre>${fn:escapeXml(job.error.formatted) }</pre>
					    </div>
					</div>
			
			    </c:otherwise>
			</c:choose>
		
		</div>
	</div>

</div>

</h:main>
