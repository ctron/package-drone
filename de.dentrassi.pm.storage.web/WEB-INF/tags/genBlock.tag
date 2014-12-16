<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>

<div class="pure-g">

<h:genMenu />

<%-- content area --%>

<div class="pure-u-1 pure-u-md-5-6">
<div style="margin-left: 1em;">

<jsp:doBody />

</div> <%-- inner content --%>
</div> <%-- content area --%>

</div> <%-- global --%>