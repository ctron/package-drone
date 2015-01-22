<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrass.de/pm" prefix="pm" %>

<h:main title="Channel" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<%--<h:channelNav channel="${channel}"/> --%>
<h:nav menu="${menuManager.getViews(channel) }"/>

<h:metaDataTable metaData="${channel.metaData}"/>

</h:main>