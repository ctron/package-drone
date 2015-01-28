<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="de.dentrassi.pm.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Login">

<div class="container-fluid">

<c:if test="${not empty errorTitle }">
    <div class="row">
        <div class="col-md-offset-1 col-md-7">
            <c:choose>
                <c:when test="${empty details }">
                    <div class="alert alert-danger">
                        ${fn:escapeXml(errorTitle) }
                    </div>
                </c:when>
                <c:otherwise>
					<div class="alert alert-danger">
						<strong>${fn:escapeXml(errorTitle) }</strong>
						${fn:escapeXml(details) }
    				</div>
                </c:otherwise>
            </c:choose>
        
		    
	    </div>
    </div>
</c:if>

<div class="row">

    <div class="col-md-offset-1 col-md-7">
    
	    <form:form action="" method="POST"  cssClass="form-horizontal">
	        <h:formEntry label="E-Mail"  command="command" path="email">
	            <%--
	              - Although we state that this is an e-mail field, we do allow other user names (e.g. 'admin') as well.
	              - So we cannot set the type to 'email' since some browser validate this on the client side and prevent
	              - the form to be submitted.
	              --%>
	            <form:input path="email" cssClass="form-control" type="text"/>
	        </h:formEntry>
	        
	        <h:formEntry label="Password"  command="command" path="password">
	            <form:input path="password" cssClass="form-control" type="password"/>
	        </h:formEntry>
	        
		    <div class="form-group">
		            <div class="col-sm-offset-2 col-sm-10">
		            <div class="checkbox">
		                <label>
		                    <input name="rememberMe" id="rememberMe" type="checkbox"> Remember me on this computer
		                </label>
		            </div>
		        </div>
		    </div>
	        
			<div class="form-group">
			    <div class="col-sm-offset-2 col-sm-10">
			        <button type="submit" class="btn btn-primary">Sign in</button>
			    </div>
			</div>
	
	    </form:form>

    </div>
    
    <c:if test="${failureCount gt 2 }">
        <div class="col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading"><h3 class="panel-title">Forgot password?</h3></div>
                <div class="panel-body">
                If you forgot your password, or your account was created without a password, then you can
                <a href="/signup/reset">request a new</a> one.
                </div>            
            </div>
        </div>
    </c:if>
        
</div></div>



</h:main>