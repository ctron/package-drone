<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="pm" %>

<h:main title="Channel aspects" subtitle="${pm:channel(channel) }">

<script type="text/javascript">
function doAction ( action, factoryId )
{
    var form = document.getElementById("form-" + factoryId);
    form.setAttribute("action", action);
    form.submit ();
    return false;
}
</script>

<h:breadcrumbs />

<div class="container-fluid">

<div class="row">

<div class="col-sm-6">

<div style="padding: 1em;">
<h2>Assigned aspects</h2>

<c:forEach items="${channel.aspects }" var="aspect">
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">${fn:escapeXml(aspect.label) }
        <c:if test="${not aspect.resolved }"><span class="label label-danger">unresolved</span></c:if>
        </h3>
    </div>
    <div class="panel-body">
    <p>${fn:escapeXml(aspect.description) }</p>
    <p>
    
    <form id="form-${fn:escapeXml(aspect.factoryId) }" action="removeAspect" method="POST">
        <input type="hidden" name="aspect" value="${fn:escapeXml(aspect.factoryId) }" />
        <button onclick="doAction('removeAspect', '${fn:escapeXml(aspect.factoryId)}');" class="btn btn-default" type="button" name="command" value="remove">Remove</button>
        <button onclick="doAction('refreshAspect', '${fn:escapeXml(aspect.factoryId)}');" class="btn btn-default" type="button" name="command" value="refresh" title="Refresh"><span class="glyphicon glyphicon-refresh"></span></button>
    </form>
    </div>
</div>

</c:forEach>

</div>

</div>

<div class="col-sm-6" >
<div style="padding: 1em;">
<h2>Additional aspects</h2>

<c:forEach items="${addAspects }" var="aspect">
<div class="panel panel-default">
    <div class="panel-heading"><h3 class="panel-title">${fn:escapeXml(aspect.label) }</h3></div>
    <div class="panel-body">
    <p>${fn:escapeXml(aspect.description) }</p>
    <p><form action="addAspect" method="POST"><input type="hidden" name="aspect" value="<c:out value="${aspect.factoryId }"></c:out>"><input type="submit" value="Add" class="btn btn-default" /></form>
    </div>
</div>
</c:forEach>

</div>
</div>

</div>

</div> <%-- container --%>

</h:main>