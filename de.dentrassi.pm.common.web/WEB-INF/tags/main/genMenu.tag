<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<ul class="nav nav-stacked nav-pills">
   <li class="">Default</li>
    <c:url value="/channel/${channelId }/add" var="url" />
    <li class='${pageContext.request.servletPath == url ? "active": "" }'><a href="${ url }">Upload</a></li>
    <li class="">Generated</li>
    <c:forEach items="${generators}" var="gen">
       <li
       class='${pageContext.request.servletPath == gen.addTarget.renderFull(pageContext) ? "active": "" }'
       ><a href="${gen.addTarget.renderFull(pageContext) }">${fn:escapeXml(gen.label) }</a></li>
    </c:forEach>
</ul>
