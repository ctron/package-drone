<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrass.de/pm" prefix="pm" %>

<h:main title="Channel" subtitle="${pm:channel(channel) }">

<ul class="button-bar">
	<li><a class="btn btn-primary" href="add">Add Artifact</a></li>
    <li><a class="btn btn-default" href="edit">Edit Channel</a></li>
    <li><a class="btn btn-default" href="aspects">Configure Aspects</a></li>
    
	<li><a class="btn btn-danger" href="delete"><span class="glyphicon glyphicon-trash"></span> Delete Channel</a></li>
	<li><a class="btn btn-warning" href="clear">Clear Channel</a></li>
	
	<li id="upload-refresh" style="display: none;"><a class="btn btn-primary" href="">Reload</a></li>
</ul>

<h:channelNav channel="${channel}"/>

<h:metaDataTable metaData="${channel.metaData}"/>

</h:main>