<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@attribute name="title" required="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>

<head>
	<title><%=title %> | OSGi EE with Spring</title>
	
	<meta charset="UTF-8">
	
	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/default.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pure-fix.css" />
	
	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/pure/0.5.0/pure-min.css" />
	

	<meta name="viewport" content="width=device-width, initial-scale=1">
	
	<!--[if lte IE 8]>
    	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/pure/0.5.0/grids-responsive-old-ie-min.css">
	<![endif]-->
	<!--[if gt IE 8]><!-->
    	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/pure/0.5.0/grids-responsive-min.css">
	<!--<![endif]-->
	
</head>

<body>

<nav>

<div class="pure-menu pure-menu-open pure-menu-horizontal">
	<a href="<c:url value="/"/>" class="pure-menu-heading">Package Drone</a>
	<ul>
		<c:forEach items="${menuManager.entries }" var="entry">
		<li <c:if test="${currentUrl eq entry.location}" >class="pure-menu-selected"</c:if>><a href="<c:url value="${entry.location }" />"><c:out value="${entry.label}" escapeXml="true"/></a></li>
		</c:forEach>
    </ul>
</div>

</nav>

<c:if test="${ not empty success }">
<div class="success"><c:out escapeXml="true" value="${success }" /></div>
</c:if>

<div class="pure-g">
    <div class="pure-u-1 main-title">
    	<header>
			<h1><%= title %></h1>
		</header>
    </div>
</div>

<section>

<div id="content">
<jsp:doBody/>
</div>

</section>

</body>

</html>