<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Edit generated P2 feature artifact">

<ul class="button-bar">
	<li><a class="pure-button" href="/artifact/${artifactId }/view">Cancel</a></li>
</ul>

<form:form action="" method="POST" cssClass="pure-form pure-form-aligned">
	<fieldset>
		<legend>Edit generated P2 feature</legend>

		<div class="pure-control-group">
			<form:label path="id">Feature ID:</form:label>
			<form:input path="id" disabled="disabled"/>
			<div class="pure-form-message-inline">
				<form:errorList path="id" cssClass="validation-error" />
			</div>
		</div>
		
		<div class="pure-control-group">
               <form:label path="version">Version:</form:label>
               <form:input path="version"/>
               <div class="pure-form-message-inline">
                   <form:errorList path="version" cssClass="validation-error" />
               </div>
           </div>
           
           <div class="pure-control-group">
               <form:label path="label">Label:</form:label>
               <form:input path="label"/>
               <div class="pure-form-message-inline">
                   <form:errorList path="label" cssClass="validation-error" />
               </div>
           </div>

           <div class="pure-control-group">
               <form:label path="provider">Provider:</form:label>
               <form:input path="provider"/>
               <div class="pure-form-message-inline">
                   <form:errorList path="provider" cssClass="validation-error" />
               </div>
           </div>

            <div class="pure-control-group">
               <form:label path="description">Description:</form:label>
               <form:textarea path="description"/>
               <div class="pure-form-message-inline">
                   <form:errorList path="description" cssClass="validation-error" />
               </div>
           </div>
           
		<button type="submit" class="pure-button pure-button-primary">Edit</button>
		<button type="reset" class="pure-button">Reset</button>
	</fieldset>
</form:form>

</h:main>