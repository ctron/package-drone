<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:main title="Database Upgrade" subtitle="Update completed">

<div class="container">
    <div class="row">
        <div class="col-md-6 col-md-offset-1">
			<div class="panel panel-success">
			
			    <div class="panel-heading"><h3 class="panel-title">Database information</h3></div>
				    <table class="table">
				
						<tbody>
							<tr><th>Database Schema Version</th><td>${databaseSchemaVersion }</td></tr>
							<tr><th>Current Schema Version</th><td>${currentVersion }</td></tr>
							<tr><th>Service Present</th><td>${storageServicePresent }</td></tr>
						</tbody>
				
				    </table>
			
			</div>
<p>
<c:choose>
    <c:when test="${mailServicePresent}">
        <a href="<c:url value="/config"/>" class="btn btn-success">Back</a>
    </c:when>
    <c:otherwise>
        <a href="<c:url value="/setup"/>" class="btn btn-success">Back</a>
    </c:otherwise>
</c:choose>

</p>
			
		</div><%-- col --%>
    </div><%-- row --%>
</div><%-- container --%>


</h:main>