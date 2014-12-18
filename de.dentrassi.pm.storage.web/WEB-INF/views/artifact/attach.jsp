<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Attach Artifact to '${fn:escapeXml(artifact.name) }'">

<ul class="button-bar">
	<li><a class="pure-button" href="view">Back</a></li>
</ul>

<p>
Artifact Id: ${fn:escapeXml(artifact.id) }
</p>

<form method="post" action="" enctype="multipart/form-data" class="pure-form pure-form-stacked">
    <fieldset>
        <legend>Attach artifact</legend>
        
        <label for="name">File Name</label>
        <input type="text" id="name" name="name"/>
        
        <label for="file">File</label>
        <input type="file" id="file" name="file"/>
        
        <button type="submit" class="pure-button pure-button-primary">Upload</button>
    </fieldset>
</form>

</h:main>