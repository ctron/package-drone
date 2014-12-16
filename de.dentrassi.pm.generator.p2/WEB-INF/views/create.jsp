<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Create generated P2 feature artifact">

<ul class="button-bar">
	<li><a class="pure-button" href="/channel/${channelId }/view">Cancel</a></li>
</ul>

<h:genBlock>

	<form:form action="" method="POST" cssClass="pure-form pure-form-aligned">
		<fieldset>
			<legend>Create channel P2 feature</legend>

			<div class="pure-control-group">
				<form:label path="id">Feature ID:</form:label>
				<form:input path="id"/>
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
            
			<button type="submit" class="pure-button pure-button-primary">Create</button>
		</fieldset>
	</form:form>

</h:genBlock>

</h:main>