<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Setup">

<div class="pure-g">

<div class="pure-u-1 pure-u-md-1-2">

<c:if test="${empty jdbcDrivers}">
<div class="warning">
	<div class="title">No JDBC drivers were found.</div>
	<div>You need to install some OSGi compatible JDBC drivers!</div>
</div>
</c:if>

<form:form action="" method="POST" cssClass="pure-form pure-form-aligned">

<fieldset>

	<div  class="pure-control-group">
		<form:label path="jdbcDriver" >JDBC Driver:</form:label>
		
		<form:select path="jdbcDriver">
			<form:option value="" label="Choose JDBC Driver"/>
			<form:optionList items="${jdbcDrivers }" itemValue="className"/>
		</form:select>

		<div class="pure-form-message-inline"><form:errorList path="jdbcDriver" cssClass="validation-error" /></div>
	
	</div>
	
	<div  class="pure-control-group">
		<form:label path="url">URL:</form:label>
		<form:input path="url"/>
		<div class="pure-form-message-inline"><form:errorList path="url" cssClass="validation-error" /></div>
	</div>
	
	<div  class="pure-control-group">
		<form:label path="user">User:</form:label>
		<form:input path="user"/>
		<div class="pure-form-message-inline"><form:errorList path="user" cssClass="validation-error" /></div>
	</div>
	
	<div  class="pure-control-group">
		<form:label path="password">Password:</form:label>
		<form:input path="password"/>
		<div class="pure-form-message-inline"><form:errorList path="password" cssClass="validation-error" /></div>
	</div>
	
	<div  class="pure-control-group">
		<form:label path="additionalProperties">Additional Properties:</form:label>
		<form:textarea path="additionalProperties" cols="40" rows="10" />
		<div class="pure-form-message-inline"><form:errorList path="additionalProperties" cssClass="validation-error" /></div>
	</div>

<input type="submit" value="Submit" class="pure-button pure-button-primary">
<input type="reset" value="Reset" class="pure-button">

</fieldset>

</form:form>

</div>

<div class="pure-u-1 pure-u-md-1-2">

<h2>Database Information</h2>

<table>

<tr><th>Database Schema Version</th><td>${databaseSchemaVersion }</td></tr>
<tr><th>Current Schema Version</th><td>${currentVersion }</td></tr>
<tr><th>Service Present</th><td>${servicePresent }</td></tr>

</table>


<c:if test="${ currentVersion != databaseSchemaVersion }">
<div>
	<form method="post" action="<c:url value="/setup/databaseUpgrade" />">
	   <input type="submit" value="Upgrade Schema" class="pure-button pure-button-primary" />
    </form>
</div>
</c:if>


</div>

</div>


</h:main>