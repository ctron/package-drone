<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrass.de/pm" prefix="pm" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>

<%@attribute name="menu" type="de.dentrassi.pm.storage.web.menu.Menu"%>
<%@attribute name="before" fragment="true"%>
<%@attribute name="after" fragment="true"%>

<div class="button-bar btn-toolbar" role="toolbar">

    <c:if test="${not empty before }"><jsp:invoke fragment="before" /></c:if>

<c:if test="${not empty menu }">
    
    <c:forEach items="${menu.nodes }" var="entry">
        <c:choose>
            <c:when test="${entry.getClass().simpleName eq 'Entry'}">
                <c:set var="url" value="${entry.target.renderFull(pageContext)}" />
                <div class="btn-group" role="group">
                    <a role="button" class="btn ${pm:modifier('btn-', entry.modifier) }" href="${url}"><h:menuEntry entry="${entry }" /></a>
                </div>
            </c:when>
        </c:choose>
    </c:forEach>

</c:if>

    <c:if test="${not empty after }"><jsp:invoke fragment="after" /></c:if>
</div>