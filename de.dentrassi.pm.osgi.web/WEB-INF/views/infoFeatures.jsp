<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/de.dentrassi.pm.osgi.web" prefix="osgi" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrass.de/pm/storage" prefix="pm" %>

<h:main title="Eclipse Features" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<table class="table table-striped">

<thead>
    <tr>
        <th>Symbolic Name</th>
        <th>Version</th>
        <th>Name</th>
    <tr>
</thead>

<tbody>
    <c:forEach var="feature" items="${features }">
    <tr>
        <td>${fn:escapeXml(feature.id) }</td>
        <td>${feature.version }</td>
        
        <td>
        <c:choose>
            <c:when test="${not empty feature.description }">
                <a tabindex="0" href="#" data-toggle="popover" data-trigger="hover" data-placement="left" title="${fn:escapeXml(feature.translate(feature.label)) }" data-content="${fn:escapeXml(feature.translate(feature.description)) }">${fn:escapeXml(feature.translate(feature.label)) }</a><osgi:translatedLabels data="${feature }" property="label" />
            </c:when>
            <c:otherwise>
                <osgi:translated data="${feature }" property="label" />
            </c:otherwise>
        </c:choose>
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