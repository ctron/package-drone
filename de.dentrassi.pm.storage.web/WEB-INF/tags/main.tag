<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@attribute name="title" required="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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


<nav class="navbar navbar-default">
<div class="container-fluid">

    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="<c:url value="/"/>">Package Drone</a>
    </div>
    
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
    <ul class="nav navbar-nav">
    <c:forEach items="${menuManager.entries }" var="entry">
        <li <c:if test="${currentUrl eq entry.location}" >class="active"</c:if>><a href="<c:url value="${entry.location }" />" <c:if test="${entry.newWindow }"> target="_blank"</c:if> ><c:out value="${entry.label}" escapeXml="true"/></a></li>
        </c:forEach>
    </ul>
    </div>

</div>
</nav>

<div class="container-fluid">
    <div class="row">
	    <div class="col-xs-12 main-title">
	    	<header>
				<h1><%= title %></h1>
			</header>
	    </div>
    </div>
</div>

<section>

<div id="content">
<jsp:doBody/>
</div>

</section>

</body>

</html>