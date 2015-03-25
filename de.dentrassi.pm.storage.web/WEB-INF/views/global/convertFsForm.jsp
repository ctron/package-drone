<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="${ (empty command.location ? 'Convert' : 'Relocate' ) }" subtitle="File system storage">

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12 col-sm-10 col-md-8">
		        <form:form action="" cssClass="form-horizontal" method="POST">
		
		            <h:formEntry label="Location" path="location" command="command">
		                <form:input path="location" cssClass="form-control" />
		                <span class="help-block">
		                This is the file system location on the server where the binary
		                data should be stored in. This server process requires read and
		                write permissions on this directory.
		                </span>
		            </h:formEntry>
		        
		            <h:formButtons>
		                <button type="submit" class="btn btn-warning">${ (empty command.location ? 'Convert' : 'Relocate' ) }</button>
		            </h:formButtons>
		        
		        </form:form>
	        </div>
	        
	        <div class="col-xs-12 col-sm-2 col-md-4">
	        </div>
	        
        </div>
    </div>

</h:main>