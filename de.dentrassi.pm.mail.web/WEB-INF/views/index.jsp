<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrass.de/pm" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Default Mail" subtitle="Setup">

<div class="container-fluid form-padding">

    <form:form action="" method="POST" cssClass="form-horizontal">
    
	    <h:formEntry label="User" path="username" command="command">
	        <form:input path="user" cssClass="form-control" placeholder="Username for the mail server"/>
	    </h:formEntry>
	    
	    <h:formEntry label="Password" path="password" command="command">
            <form:input path="password" cssClass="form-control" type="password" placeholder="Password for the mail server"/>
        </h:formEntry>
        
        <h:formEntry label="Host" path="host" command="command">
            <form:input path="host" cssClass="form-control"  placeholder="Hostname or IP of the SMTP server"/>
        </h:formEntry>
        
        <h:formEntry label="Port" path="port" command="command">
            <form:input path="port" cssClass="form-control"  placeholder="Optional port number of the SMTP server"/>
        </h:formEntry>
    
	    <div class="form-group">
	        <div class="col-sm-offset-2 col-sm-10">
	            <button type="submit" class="btn btn-primary">Update</button>
	            <button type="reset" class="btn btn-default">Reset</button>
	        </div>
	    </div>
    </form:form>

</div>

</h:main>