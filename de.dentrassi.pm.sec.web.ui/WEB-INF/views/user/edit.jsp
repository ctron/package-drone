<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="de.dentrassi.pm.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>


<h:main title="Edit user" subtitle="${empty user.details.name ? user.details.email : user.details.name }">

<h:breadcrumbs/>

<style>
<!--
fieldset {
    padding: 1em;
}
-->
</style>

<div class="container-fluid">
	<form:form action="" method="POST"  cssClass="form-horizontal">
	
	   
	    <div class="col-md-6">
            <div class="row">
                <fieldset>
                    <legend>User details</legend>
       
				    <h:formEntry label="E-Mail"  command="command" path="email">
				        <form:input path="email" cssClass="form-control" type="email"/>
				    </h:formEntry>
				    
				    <h:formEntry label="Real Name"  command="command" path="name">
				        <form:input path="name" cssClass="form-control" placeholder="Optional real name"/>
				    </h:formEntry>
			    </fieldset>
		    </div>
        </div>
        
        <div class="col-md-6">
            <div class="row">
                <fieldset>
                    <legend>Security</legend>
                    <h:formEntry label="Roles" path="roles" command="command">
	                    <form:select path="roles" cssClass="form-control" multiple="true">
	                        <form:option value="ADMIN"/>
	                        <form:option value="MANAGER"/>
	                    </form:select>
	                    <span class="help-block">
	                       This is a multi select list. The user will only have the roles which are selected/highlighted.
	                    </span>
                    </h:formEntry>
                    
                </fieldset>
            </div>
        </div>
        
        <div class="row">
			<div class="col-md-12" >
				<input type="submit" value="Submit" class="btn btn-primary">
				<input type="reset" value="Reset" class="btn btn-default">
			</div>
	    </div>

		</form:form>
</div>

</h:main>