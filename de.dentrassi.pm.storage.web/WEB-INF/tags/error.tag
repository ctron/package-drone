<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@attribute name="title" required="true" %>

<h:box title="${title }" cssClass="error" ><jsp:doBody/></h:box>