<%@ page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web" prefix="web" %>

<web:define name="entryBody">

    <${(empty task.target) ? "div" : "a" }
    
    <c:if test="${ not empty task.target}">
    href="${task.target.render(pageContext.request) }"
    </c:if>
    
    
    
    class="
    list-group-item 
    ${ (task.state eq "DONE" ) ? "list-group-item-success" : "" }
    "
    >

    <h4 class="list-group-item-heading">${fn:escapeXml(task.title) }
    
    <c:if test="${not empty task.target }"><div class="pull-right"><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span></div></c:if>
    
    </h4>
    <p class="list-group-item-text">${task.description }</p>
    
    </${ (empty task.target) ? 'div' : 'a' }>
    
</web:define>

<h:main title="Setup" subtitle="Prepare your system">

<p class="lead">
There are a few things you have to do in order to setup up Package Drone
</p>

<div class="container-fluid"><div class="row">

    <div class="col-md-4">

		<div class="list-group">
			<c:forEach var="task" items="${tasks }">
			     <web:call name="entryBody"/>
			</c:forEach>
		</div>

	</div>

</div></div>

</h:main>