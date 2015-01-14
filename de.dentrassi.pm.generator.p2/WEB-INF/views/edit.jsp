<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Edit generated P2 feature artifact">

<ul class="button-bar">
	<li><a class="btn btn-default" href="/artifact/${artifactId }/view">Cancel</a></li>
</ul>

<form:form action="" method="POST" cssClass="form-horizontal">
	<fieldset>
		<legend>Edit generated P2 feature</legend>

        <h:formEntry label="Feature ID" path="id" command="command">
            <form:input path="id" cssClass="form-control"/>
        </h:formEntry>
		
        <h:formEntry label="Feature Version" path="version" command="command">
            <form:input path="version" cssClass="form-control"/> 
        </h:formEntry>
        
        <h:formEntry label="Label" path="label" command="command">
            <form:input path="label" cssClass="form-control"/> 
        </h:formEntry>

        <h:formEntry label="Provider" path="provider" command="command">
            <form:input path="provider" cssClass="form-control"/> 
        </h:formEntry>            

        <h:formEntry label="Description" path="description" command="command">
            <form:textarea path="description" cssClass="form-control"/> 
        </h:formEntry> 
           
		<button type="submit" class="btn btn-primary">Update</button>
		<button type="reset" class="btn btn-default">Reset</button>
	</fieldset>
</form:form>

</h:main>