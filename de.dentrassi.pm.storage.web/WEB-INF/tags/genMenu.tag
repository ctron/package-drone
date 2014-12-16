<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="pure-u-1 pure-u-md-1-6">
<div class="pure-menu pure-menu-open">
    <ul>
       <li class="pure-menu-heading">Default</li>
        <c:url value="/channel/${channelId }/add" var="url" />
        <li><a href="${ url }">Upload</a></li>
        <li class="pure-menu-heading">Generated</li>
        <c:forEach items="${generators}" var="gen">
           <li><a href="${gen.addTarget.renderFull(pageContext) }">${fn:escapeXml(gen.label) }</a></li>
        </c:forEach>
    </ul>
</div>
</div>