<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="storage" %>
<%@ taglib uri="http://dentrassi.de/osgi/web" prefix="web" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

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

div.dz-error-message {
    display: none;
}

.dz-error div.dz-error-message {
    display: block;
}

.dz-error-message {
    padding: 1em;
}

#upload {
    margin-bottom: 1em;
}

#dropzone {
    border: 2pt dashed #BBB;
    border-radius: 3pt;
    
    background: #FAFAFA;
    
    padding: 6px 12px;
    
    text-align: center;
        
    cursor: crosshair;
    
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

<h:buttonbar menu="${menuManager.getActions(channel) }">
    <jsp:attribute name="after">
        <c:if test="${manager }">
		    <div class="btn-group" role="group">
		        <div class="btn">
		            <span id="dropzone" title="Drop files here for uploading"><span class="glyphicon glyphicon-upload" aria-hidden="true"></span> Drop Artifacts</span>
		        </div>
		    </div>
		    
		    <div class="btn-group" role="group" id="upload-refresh" style="display: none;">
		        <a class="btn btn-success" href="">Reload</a>
		    </div>
	    </c:if>
    </jsp:attribute>
</h:buttonbar>

<div class="container-fluid" id="upload-container" style="display:none;">
<div class="row">
<div class="col-md-6">
<div id="upload"></div>
</div>
</div>
</div>

<%--<h:channelNav channel="${channel}"/> --%>
<h:nav menu="${menuManager.getViews(channel) }"/>

<table id="artifacts" class="table table-striped table-condensed table-hover">

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
    <tr id="row-${artifact.id }">
        <td>${fn:escapeXml(artifact.name) }</td>
        <td class="text-right"><web:bytes amount="${ artifact.size}"/></td>
        <td style="white-space: nowrap;"><fmt:formatDate value="${artifact.creationTimestamp }" type="both" /> </td>
        <td><a href="<c:url value="/artifact/${artifact.id}/get"/>">Download</a></td>
        <td>
          <c:if test='${artifact.is("deletable") and manager}'><a href="<c:url value="/artifact/${artifact.id}/delete"/>">Delete</a></c:if>
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
var url = "<c:url value="/channel/${channel.id}/drop"/>";
var dz = new Dropzone ( "#dropzone", {
    url: url,
    paramName: "file",
    previewsContainer: "#upload",
    uploadMultiple: false,
    clickable: false,
    previewTemplate: "<div class='panel panel-default'><div class='panel-heading'><div class='dz-filename' data-dz-name></div> <div class='dz-size' data-dz-size></div> </div> <div class='panel-body'><div class='progress'><div class='progress-bar progress-bar-striped' role='progressbar' data-dz-uploadprogress></div></div> <div class='dz-error-message bg-danger' data-dz-errormessage></div></div></div>"
});

dz.on ( "queuecomplete", function () {
    document.getElementById("upload-refresh").setAttribute("style", "");
});

dz.on ( "addedfile", function () {
    $( "#upload-container" ).show ();
});
</script>
<%-- END: drag and drop upload --%>

</h:main>