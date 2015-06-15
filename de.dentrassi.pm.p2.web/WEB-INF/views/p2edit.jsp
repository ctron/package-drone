<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Edit P2 Channel Information" subtitle="${pm:channel(channel) }">

<h:breadcrumbs/>

<div class="container form-padding">

<form:form action="" method="POST" cssClass="form-horizontal">
    
    <h:formEntry label="Title" path="title" command="command">
        <form:input path="title" cssClass="form-control" placeholder="Optional title for the repository" required="false"/>
    </h:formEntry>
    
    <h:formEntry label="System Bundle Alias" path="systemBundleAlias" command="command">
        <form:input path="systemBundleAlias" cssClass="form-control" required="false"/>
        <span class="help-block">
        If set this value will be used a dependency installable unit ID when a bundle
        declares a dependency on the OSGi <code>system.bundle</code>.
        </span>
        <span class="help-block">
        See also <a href="http://wiki.osgi.org/wiki/System_Bundle">http://wiki.osgi.org/wiki/System_Bundle</a>.
        </span>
    </h:formEntry>
    
    <h:formButtons>
        <button type="submit" class="btn btn-primary">Update</button>
        <button type="reset" class="btn btn-default">Reset</button>
    </h:formButtons>
    
</form:form>
</div>

</h:main>