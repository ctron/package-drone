<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Import channel">

<div class="container-fluid">

	<div class="row">
	
		<div class="col-sm-6">
		
			<form:form action="" method="POST" cssClass="form-horizontal" enctype="multipart/form-data">
			    
			    <h:formEntry label="File" command="command" path="file">
			        <form:input path="file" type="file"/>
			        <span class="help-block">
                    Select the channel export file to import. This must be a channel file previously exported by Package Drone.
			        </span>
			    </h:formEntry>
			    
				<h:formButtons>
			        <input type="submit" value="Import" class="btn btn-primary">
			    </h:formButtons>
			
			</form:form>
		</div>
	</div>

</div>

</h:main>