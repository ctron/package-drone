<%@page import="de.dentrassi.pm.storage.DeployKey"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="de.dentrassi.pm.storage.DeployGroup"%>
<%@page import="java.util.List"%>
<%@page import="de.dentrassi.pm.storage.Channel"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://dentrassi.de/osgi/web" prefix="web" %>

<%
Channel channel = (Channel)request.getAttribute ( "channel" );
List<DeployGroup> groups = new ArrayList<> (channel.getDeployGroups ()) ;
Collections.sort ( groups, DeployGroup.NAME_COMPARATOR );
pageContext.setAttribute ( "groups", groups );
%>

<web:define name="named">
<c:choose>
   <c:when test="${not empty named.name }">${fn:escapeXml(named.name) } (${fn:escapeXml(named.id) })</c:when>
   <c:otherwise>${fn:escapeXml(named.id) }</c:otherwise>
</c:choose>
</web:define>

<h:main title="Channel" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid form-padding">
    
	<div class="row">
	   <div class="col-md-8">
	   
	       <h3>Deploy Groups</h3>
	       
	       <table class="table table-condensed table-hover">
	       
	           <thead>
	           </thead>
	           
	           <tbody>
		           <c:forEach var="dg" items="${groups }">
		              <tr>
	                  <td><a href="<c:url value="/deploy/auth/group/${dg.id}/view"/>"><web:call name="named" named="${dg }"/></a></td>
	                  <td>
	                    <ul>
	                          <c:forEach var="dk" items="${dg.keys}">
	                            <li><a href="<c:url value="/deploy/auth/key/${dk.id }/edit"/>"><web:call name="named" named="${dk }"/></a></li>
	                        </c:forEach>
	                    </ul>
	                    </td>
	                    <td>
	                    <form action="removeDeployGroup" method="post">
	                       <input type="hidden" name="groupId" value="${fn:escapeXml(dg.id) }"/>
	                       <button title="Unassign deploy group" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span></button>
	                    </form>
	                    </td>
	                   </tr>
	               </c:forEach>       
	           </tbody>
	       </table>
	   
			<ul>
			
			</ul>
		</div>
		
		<div class="col-md-4">
		
            <div class="panel panel-default">
            
                <div class="panel-heading"><h3 class="panel-title">Add Deploy Group</h3></div>
                <div class="panel-body">
	                <form class="form-horizontal" method="post" action="addDeployGroup">
	    
				        <div class="form-group">
				            <label class="col-sm-2 control-label" for="groupId">Group</label>
				            <div class="col-sm-10">
					            <select name="groupId" class="form-control" id="groupId">
					                <c:forEach var="dg" items="${deployGroups }">
					                    <option value="${dg.id }">
					                       <web:call name="named" named="${dg }"/>
					                    </option>
					                </c:forEach>    
					            </select>
				            </div>
				        </div>
				        
				        <div class="form-group">
						    <div class="col-sm-offset-2 col-sm-10">
						      <button type="submit" class="btn btn-primary" ${ ( empty deployGroups ) ? 'disabled="disabled"': '' } >Add</button>
						    </div>
						  </div>
				    </form>
                </div>
		  </div>
		  
		</div>
	</div>

</div>

</h:main>