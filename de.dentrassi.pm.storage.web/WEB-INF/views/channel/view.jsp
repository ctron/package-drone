<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrass.de/pm" prefix="pm" %>

<h:main title="Channel - ${pm:channel(channel) }">

<style type="text/css">
.dz-progress {
    width: 100%;
    height: 18px;
    background: #FAFAFA;
}
.dz-upload {
    height: 18px;
    background: #EEEEEE;
}
.dz-filename {
    display: inline;
}
.dz-size {
    display: inline;
}

#upload {
    margin-bottom: 1em;
}
#upload-refresh {
}

#dropzone {
    border: .15em dashed #AAA;
    border-radius: 2pt;
    
    background: #FAFAFA;
    
    padding: 0.35em 1em;
    
    cursor: pointer;
    
     -webkit-touch-callout: none;
    -webkit-user-select: none;
    -khtml-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    
    color: rgba(0, 0, 0, 0.80);
}

#dropzone:HOVER, .dz-drag-hover {
    border-color: #888;
    background-color: #EEE;
}

</style>

<ul class="button-bar">
    <li><a class="btn btn-default" href="edit">Edit Channel</a></li>
	<li><a class="btn btn-danger" href="delete">Delete Channel</a></li>
	<li><a class="btn btn-primary" href="add">Add Artifact</a></li>
	<li><a class="btn btn-warning" href="clear">Clear Channel</a></li>
	<li><a class="btn btn-default" href="aspects">Configure Aspects</a></li>
	
	<li>
	   <span id="dropzone" title="Drop files here for uploading">Drop Artifacts</span>
	</li>
	
	<li id="upload-refresh" style="display: none;"><a class="btn btn-primary" href="">Reload</a></li>
</ul>

<div id="upload"></div>
<div></div>

<table class="table table-striped table-condensed">

<thead>
	<tr>
		<th>Name</th>
		<th>Size</th>
		<th>Created</th>
		<th></th>
		<th></th>
		<th></th>
		<th></th>
	</tr>
</thead>

<tbody>
<c:forEach items="${sortedArtifacts }" var="artifact">
	<tr>
		<td>${fn:escapeXml(artifact.name) }</td>
		<td>${fn:escapeXml(artifact.size) }</td>
		<td style="white-space: nowrap;"><fmt:formatDate value="${artifact.creationTimestamp }" type="both" /> </td>
		<td><a href="<c:url value="/artifact/${artifact.id}/get"/>">Download</a></td>
		<td>
		  <c:if test="${not artifact.derived }"><a href="<c:url value="/artifact/${artifact.id}/delete"/>">Delete</a></c:if>
		</td>
		<td><a href="<c:url value="/artifact/${artifact.id}/view"/>">Details</a></td>
		<td><a href="<c:url value="/artifact/${artifact.id}/dump"/>">View</a></td>
	</tr>
</c:forEach>
</tbody>

</table>

<%-- START: drag and drop upload --%>
<script src="<c:url value="/resources/js/dropzone.min.js"/>"></script>
<script>
var url = "<c:url value="/channel/${channel.id}/add"/>";
var dz = new Dropzone ( "#dropzone", {
	url: url,
	paramName: "file",
	previewsContainer: "#upload",
	uploadMultiple: false,
	clickable: false,
	previewTemplate: "<div class='dz-preview'><div class='dz-filename' data-dz-name></div> <div class='dz-size' data-dz-size></div> <div class='dz-progress'><div class='dz-upload' data-dz-uploadprogress></div></div> <div class='dz-error-message' data-dz-errormessage></div></div>"
});

dz.on ( "queuecomplete", function () {
	document.getElementById("upload-refresh").setAttribute("style", "");
});
</script>
<%-- END: drag and drop upload --%>

</h:main>