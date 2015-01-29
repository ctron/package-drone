<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://dentrassi.de/osgi/web" prefix="web" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/security" prefix="sec" %>

<%@attribute name="title" required="true" %>
<%@attribute name="subtitle" %>

<!DOCTYPE html>
<html>

<c:set var="bootstrap" value="${pageContext.request.contextPath}/resources/bootstrap/3.3.1"/>
<c:set var="jquery" value="${pageContext.request.contextPath}/resources/jquery"/>
<c:set var="html5shiv" value="${pageContext.request.contextPath}/resources/html5shiv/3.7.2"/>
<c:set var="respond" value="${pageContext.request.contextPath}/resources/respond/1.4.2"/>

<head>
    <title><%=title %> | Package Drone</title>
    
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <link rel="icon" href="${pageContext.request.contextPath}/resources/favicon.ico" sizes="16x16 32x32 48x48 64x64 128x128 256x256" type="image/vnd.microsoft.icon"/>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/default.css" />
    
    <%-- bootstrap --%>
    
    <link href="${bootstrap}/css/bootstrap.min.css" rel="stylesheet">
    <!--[if lt IE 9]>
      <script src="${html5shiv}/3.7.2/html5shiv.min.js"></script>
      <script src="${respond}/respond.min.js"></script>
    <![endif]-->

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="${jquery}/jquery-1.11.2.min.js"></script>
    <script src="${bootstrap}/js/bootstrap.min.js"></script>

</head>

<body>

<sec:user var="principal"/>
<c:set var="gravatar" value="${web:gravatar(principal.details.email) }"/>

<h:navbar menu="${menuManager.mainMenu }">
    <jsp:attribute name="brand">
        <a class="navbar-brand" href="<c:url value="/"/>"><img alt="Package Drone" src="<c:url value="/resources/pdrone.png" />"/></a>
    </jsp:attribute>
    <jsp:attribute name="after">
        <c:if test="${empty principal }">
            <p class="navbar-text navbar-right"><a href="<c:url value="/signup"/>">Register</a> or <a href="<c:url value="/login"/>">Sign in</a></p>
        </c:if>
        <c:if test="${not empty principal }">
            <p class="navbar-text navbar-right">
                <c:if test="${not empty gravatar }"><img class="gravatar" src="https://secure.gravatar.com/avatar/${gravatar }.jpg?s=24"  width="24" height="24" />&nbsp;</c:if>
                
                <a href="<c:url value="/user/${principal.id}/view"/>">
                <c:choose>
                    <c:when test="${not empty principal.details.name }">${fn:escapeXml(principal.details.name) }</c:when>
                    <c:otherwise>Profile</c:otherwise>
                </c:choose>
                </a>
                
                &mdash;
                
                &nbsp;<a href="<c:url value="/logout"/>">Sign out</a>
            </p>
        </c:if>
    </jsp:attribute>
</h:navbar>

<div class="page-header">
<h1>${fn:escapeXml(title) }<c:if test="${not empty subtitle }">&nbsp;<small>${fn:escapeXml(subtitle) }</small></c:if></h1>
</div>
<section>

<div id="content">
<jsp:doBody/>
</div>

</section>

<footer>

<div class="pull-right"><a href="http://packagedrone.org" target="_blank">Package Drone ${droneVersion }</a></div>

</footer>

</body>

</html>