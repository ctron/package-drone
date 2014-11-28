<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@attribute name="title" required="true" %>
<%@attribute name="cssClass" required="true" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="${cssClass} box">
<div class="title">${fn:escapeXml (title) }</div>
<div><jsp:doBody/></div>
</div>