<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<%@attribute name="command" required="false" type="java.lang.String"%>
<%@attribute name="path" required="false" type="java.lang.String"%>
<%@attribute name="label" required="false" type="java.lang.String" %>
<%@attribute name="optional" required="false" type="java.lang.Boolean" %>

<div class="form-group ${ optional ? ' optional' : '' } ${' ' } ${ (empty path) ?  '' : form:validationState ( pageContext, ( ( empty command ) ? 'command' : command ), path, '', 'has-error')}">
    <form:label path="${path }" cssClass="col-sm-2 control-label">${fn:escapeXml(label) }</form:label>
    
    <div class="col-sm-10">
        <jsp:doBody/>
    </div>
    
    <div class="col-sm-10 col-sm-offset-2">
        <form:errorList path="${path }" cssClass="help-block" />
    </div>
</div>