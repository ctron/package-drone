<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@attribute name="entry" required="true" type="de.dentrassi.pm.common.web.menu.Node"%>

<c:if test="${not empty entry }">
<c:if test="${not empty entry.icon }"><span class="glyphicon glyphicon-${entry.icon }"></span> </c:if>${fn:escapeXml(entry.label) }
</c:if>
