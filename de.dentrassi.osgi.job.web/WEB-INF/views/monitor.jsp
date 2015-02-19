<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="de.dentrassi.osgi.job.JobHandle"%>

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

<c:choose>
    <c:when test="${empty job }">Job not found!</c:when>
    <c:otherwise>

		<div class="panel panel-${type }">
		    <div class="panel-heading">
		        <h4 class="panel-title">${fn:escapeXml(job.label) }</h4>
		    </div>
		    
		    <div class="panel-body">
		        <c:if test="${not empty errorFormatted }">
		            <pre>${fn:escapeXml(errorFormatted) }</pre>
		        </c:if>
		    </div>
		</div>
		
		<c:if test="${job.complete }">
			<script type="text/javascript">
			$('#job-${job.id}').trigger ('job.complete', {
				error:${job.error ne null},
			} );
			</script>
		</c:if>
		<c:if test="${not job.complete }">
			<script type="text/javascript">
			setTimeout ( function (){reload();}, 1000 );
			</script>
		</c:if>

    </c:otherwise>
</c:choose>
