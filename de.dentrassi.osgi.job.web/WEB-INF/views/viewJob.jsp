<%@page import="de.dentrassi.osgi.job.JobHandle"%>
<%@page import="org.eclipse.scada.utils.ExceptionHelper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<% 

JobHandle job = (JobHandle)pageContext.getRequest (  ).getAttribute ( "job" );
if ( job != null && job.getError ( ) != null )
{
    String errorMessage = ExceptionHelper.getMessage ( job.getError ( ) );
    String formatted = ExceptionHelper.formatted ( ExceptionHelper.getRootCause ( job.getError ( ) ));
    pageContext.setAttribute ( "errorMessage", errorMessage );
    pageContext.setAttribute ( "errorFormatted", formatted );
}

if ( job.isComplete () && job.getError () == null )
    pageContext.setAttribute ( "type", "success");
else if ( job.isComplete (  ) && job.getError (   ) != null  )
    pageContext.setAttribute ( "type", "danger");
else
    pageContext.setAttribute ( "type", "default");
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="panel panel-${type }">
    <div class="panel-heading">
        <h4 class="panel-title">${fn:escapeXml(job.job.label) }</h4>
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