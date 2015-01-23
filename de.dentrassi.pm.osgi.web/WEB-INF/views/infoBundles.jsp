<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrass.de/pm" prefix="pm" %>

<h:main title="OSGi Bundles" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<table class="table table-striped">

<thead>
    <tr>
        <th>Symbolic Name</th>
        <th>Version</th>
        <th>Name</th>
        <th>EE</th>
        <th>Links</th>
    <tr>
</thead>

<tbody>
    <c:forEach var="bundle" items="${bundles }">
    <tr>
        <td>${fn:escapeXml(bundle.id) }</td>
        <td>${bundle.version }</td>
        
        <td>
        <c:choose>
            <c:when test="${not empty bundle.description }">
                <a tabindex="0" href="#" data-toggle="popover" data-trigger="hover" data-placement="left" title="${fn:escapeXml(bundle.translate(bundle.name)) }" data-content="${fn:escapeXml(bundle.translate(bundle.description)) }">${fn:escapeXml(bundle.translate(bundle.name)) }</a><h:translatedLabels data="${bundle }" property="name" />
            </c:when>
            <c:otherwise>
                <h:translated data="${bundle }" property="name" />            
            </c:otherwise>
        </c:choose>
        </td>
        
        <td>
        <c:forEach var="ee" items="${bundle.requiredExecutionEnvironments }">
        <span class="label label-default">${fn:escapeXml(ee) }</span>
        </c:forEach>
        </td>
        
        <td>
        <c:set var="delim" value=""/>
        <c:if test="${not empty bundle.docUrl }"><a target="_blank" href="${bundle.docUrl }">Documentation</a><c:set var="delim" value=", "/></c:if>
        <c:if test="${not empty bundle.license and ( fn:startsWith(bundle.license, 'http://') or fn:startsWith(bundle.license, 'https://') ) }">${delim }<a href="${bundle.license }" target="_blank">License</a><c:set var="delim" value=", "/></c:if>
        </td>
    </tr>
    </c:forEach>
</tbody>

</table>

<script type="text/javascript">
$(function () {
	  $('[data-toggle="popover"]').popover()
	})
</script>

</h:main>