<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<h:main title="Setup">

<form:form action="" method="POST" cssClass="pure-form">

<table>

<tr>
<td>
	<form:label path="jdbcDriver">JDBC Driver:</form:label>
</td>
<td>
	<c:if test="${empty jdbcDrivers}">
	<div class="warning">
		<div class="title">No JDBC drivers were found.</div>
		<div>You need to install some OSGi compatible JDBC drivers!</div>
	</div>
	</c:if>
	<c:if test="${not empty jdbcDrivers}">
	<form:select path="jdbcDriver">
	<form:option value=""  label="Choose JDBC Driver"/>
	<form:options items="${jdbcDrivers }" itemValue="className"/>
	</form:select>
	</c:if>
</td>
<td><form:errors path="jdbcDriver" /> </td>
</tr>

<tr>
	<td><form:label path="url">URL:</form:label></td>
	<td><form:input path="url"/></td>
	<td><form:errors path="url" /> </td>
</tr>

<tr>
	<td><form:label path="user">User:</form:label></td>
	<td><form:input path="user"/></td>
	<td><form:errors path="user" /> </td>
</tr>

<tr>
	<td><form:label path="password">Password:</form:label></td>
	<td><form:input path="password"/> </td>
	<td><form:errors path="password" /> </td>
</tr>

<tr>
	<td><form:label path="additionalProperties">Additional Properties:</form:label></td>
	<td><form:textarea path="additionalProperties" cols="40" rows="10" /></td>
	<td><form:errors path="additionalProperties" /> </td>
</tr>

</table>

<div>
<input type="submit" value="Submit" class="pure-button pure-button-primary">
<input type="reset" value="Reset" class="pure-button">
</div>

</form:form>

</h:main>