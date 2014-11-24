<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<h:main title="Add artifact">

<ul class="button-bar">
	<li><a class="pure-button" href="view">Cancel</a></li>
</ul>

<form method="post" action="" enctype="multipart/form-data" class="pure-form pure-form-stacked">
	<fieldset>
		<legend>Add artifact to channel</legend>
		
		<label for="name">File Name</label>
		<input type="text" id="name" name="name"/>
		
		<label for="file">File</label>
		<input type="file" id="file" name="file"/>
		
		<button type="submit" class="pure-button pure-button-primary">Submit</button>
	</fieldset>
</form>

</h:main>