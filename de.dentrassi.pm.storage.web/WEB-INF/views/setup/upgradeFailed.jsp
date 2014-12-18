<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
Throwable error = (Throwable)request.getAttribute ( "error" );
StringWriter sw = new StringWriter();
error.printStackTrace (new PrintWriter (sw));
pageContext.setAttribute ( "errorString", sw.toString() );
%>

<h:main title="Database Upgrade Failed">

<h:error title="Upgrade failed"><pre>${errorString }</pre></h:error>

</h:main>