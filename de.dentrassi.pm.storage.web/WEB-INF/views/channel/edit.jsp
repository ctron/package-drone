<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<h:main title="Edit channel - ${command.id }">

<ul class="button-bar">
    <li><a class="pure-button" href="view">Back</a></li>
</ul>

<form:form action="" method="POST" cssClass="pure-form pure-form-aligned">

<fieldset>

	<div class="pure-control-group">
		<form:label path="id">ID:</form:label>
		<form:input path="id" disabled="true"/>
	</div>
	
	<div  class="pure-control-group">
		<form:label path="name">Name:</form:label>
		<form:input path="name"/>
		<div class="pure-form-message-inline"><form:errors path="name" cssClass="validation-error" /></div>
	</div>
	
<input type="submit" value="Submit" class="pure-button pure-button-primary">
<input type="reset" value="Reset" class="pure-button">

</fieldset>

</form:form>

</h:main>