<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web" prefix="web" %>

<%@attribute name="channel" required="true" type="de.dentrassi.pm.storage.Channel"%>

<ul class="nav nav-tabs">
  <c:url var="url" value="/channel/${channel.id }/view" />
  <li role="presentation" class='${web:active(pageContext.request, url)}'><a href="${url }">List</a></li>
  
  <c:url var="url" value="/channel/${channel.id }/details" />
  <li role="presentation" class='${web:active(pageContext.request, url)}'><a href="${url }">Details</a></li>
</ul>