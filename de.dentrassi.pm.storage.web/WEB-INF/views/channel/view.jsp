<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<h:main title="Channel - ${channel.id }">

<ul class="button-bar">
	<li><a class="pure-button" href="delete">Delete Channel</a></li>
	<li><a class="pure-button" href="add">Add Artifact</a></li>
</ul>

ID: ${channel.id }

</h:main>