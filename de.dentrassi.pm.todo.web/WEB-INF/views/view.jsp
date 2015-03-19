<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Maintance tasks" subtitle="Open maintancen tasks">

<div class="container-fluid">

    <div class="row">
    
        <div class="col-md-12">
        
            <c:choose>
            
                <c:when test="${empty openTasks }">
                    <div class="well well-lg">
                        <h2>No open tasks</h2>
                        <p>Looks like you cleaned it all up! ;-)</p>
                    </div>
                </c:when>
            
                <c:otherwise>
                    <div class="table-responsive">
		            <table class="table table-striped table-hover">
		            
		                <thead>
		                    <tr>
		                        <th>Task</th>
		                        <th>Description</th>
		                        <th></th>
		                    </tr>
		                </thead>
		            
		                <tbody>
		                    <c:forEach var="task" items="${ openTasks }">
		                        <tr>
		                            <td><strong>${fn:escapeXml(task.title) }</strong></td>
		                            <td>${task.description }</td>
		                            <td>
		                                <c:choose>
		                                  <c:when test="${task.targetRequestMethod eq 'GET' }">
		                                      <a class="btn btn-link" href="<c:url value="${task.target.render(pageContext.request) }"/>">Link</a>
		                                  </c:when>
		                                  <c:otherwise>
		                                      <form action="<c:url value="${task.target.render(pageContext.request) }"/>" method="${task.targetRequestMethod }">
		                                          <button class="btn btn-default" type="submit">Perform</button>
		                                      </form>
		                                  </c:otherwise>
		                                </c:choose>
		                                
		                            </td>
		                        </tr>
		                    </c:forEach>
		                </tbody>
		            
		            </table>
		            </div>
	            
	            </c:otherwise>
            
            </c:choose>
        
        </div>
    
    </div>

</div>

</h:main>