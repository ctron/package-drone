<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@attribute name="metaData" required="true" type="java.util.Map"%>

<table class="table table-condensed">

<tr>
    <th>Namespace</th>
    <th>Key</th>
    <th>Value</th>
</tr>

<c:forEach items="${metaData }" var="entry">
    <tr>
        <td>${fn:escapeXml(entry.key.namespace) }</td>
        <td style="white-space: nowrap;">${fn:escapeXml(entry.key.key) }</td>
        <td style="white-space: pre;"><code>${fn:escapeXml(entry.value) }</code></td>
    </tr>
</c:forEach>

</table>