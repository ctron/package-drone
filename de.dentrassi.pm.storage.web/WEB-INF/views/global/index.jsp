<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm/storage" prefix="pm" %>

<h:main title="Storage operations">

<div class="container-fluid">
    <div class="row">
    
        <div class="col-sm-6">
            <div class="panel panel-default">
                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-import"></span> Import channels</h3></div>
                <div class="panel-body">
                    <p>
                    Import all channels from a full export or from a channel export.
                    </p>
                    <p>
                    Depending on the import operation you have to provide the correct export file type. A full import requires a full export file and
                    a single channel import requires a single channel export file. At the moment it is not possible to do partial imports. 
                    </p>
                </div>
                <div class="panel-body text-right">
                    <a href="<c:url value="/channel/import"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-import"></span> Import Channel</a>
                    <a href="<c:url value="/channel/importAll"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-import"></span> Import Full</a>
                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-export"></span> Export all channels</h3></div>
                <div class="panel-body">
                    <p>
                    Export all channels at once. It is possible to download the archive immediately or spool it out to the file system of the server. 
                    </p>
                    <p>
                    In order to export a single channel only, go to that channel and use the <q>Export</q> action.
                    </p>
                </div>
                <div class="panel-body text-right">
                    <a href="<c:url value="/channel/export"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-export"></span> Download</a>
                    <a href="<c:url value="/system/storage/exportAllFs"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-export"></span> Spool Out</a>
                </div>
            </div>
        </div>
        
        <div class="col-sm-6">
            <div class="panel panel-danger">
                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-trash"></span> Wipe storage</h3></div>
                <div class="panel-body">
                    This options allows you to wipe out the whole storage. All channels and artifacts
                    will be deleted.
                </div>
                <div class="panel-body text-right">
                    <button class="btn btn-danger" data-toggle="modal" data-target="#wipe-modal"><span class="glyphicon glyphicon-trash"></span> Really Wipe</button>
                </div>
            </div>
            
            <c:if test="${empty blobStoreLocation }">
            
	            <div class="panel panel-warning">
	                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-file"></span> File system BLOB store</h3></div>
	                <div class="panel-body">
	                    <p>
	                    Package Drone can use the file system for storing artifact data. Instead of writing the binary data of artifacts to the database
	                    the data is stored in the local filesystem.
	                    </p>
	                    <p>
	                    The conversion simply switches the storage to file system mode, which will store new artifacts in
	                    the file system, but keep existing ones in the database. Requesting an artifact will first try the
	                    filesystem and then the database. So existing artifacts will be served from the database.
	                    </p>
	                    <p>
	                        <strong>Note: </strong> Converting to a file system based storage cannot be reversed at the moment.
	                        See <a href="https://github.com/ctron/package-drone/wiki/File-system-BLOB-store" target="_blank">the wiki</a> for more information.
	                    </p>
	                </div>
	                <div class="panel-body">
						<form method="POST" id="fs-form" action="<c:url value="/system/storage/fileStore"/>">
						    <div class="row">
						        <div class="col-xs-8">
							        <label for="location" class="sr-only">Location</label>
							        <input type="text" id="location" class="form-control" name="location" placeholder="File system path on server"/>
							    </div>
							    <div class="col-xs-4 text-right">
	                                <button id="fs-convert" type="button" class="btn btn-warning" data-toggle="modal" data-target="#fs-modal">Convert</button>
							    </div>
					       </div> 
						</form>
	                </div>
	            </div>
            
            </c:if>
            
            <c:if test="${not empty blobStoreLocation}">
                <div class="panel panel-warning">
                    <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-file"></span> Relocate file storage</h3></div>
                    <div class="panel-body">
                        <p>
                            It is possible to change the location of the file store. However this will not move the actual directory structure, but only
                            update the settings in the database. This can be used if you moved the directory structure and need to update the
                            configuration.
                        </p>
                        <p>
                            See <a href="https://github.com/ctron/package-drone/wiki/File-system-BLOB-store" target="_blank">the wiki</a> for more information.
                        </p>
                    </div>
                    <div class="panel-body">
                        <form method="POST" id="fs-form" action="<c:url value="/system/storage/fileStore"/>">
                            <div class="row">
                                <div class="col-xs-8">
                                    <label for="location" class="sr-only">Location</label>
                                    <input type="text" id="location" class="form-control" name="location" placeholder="File system path on server" value="${fn:escapeXml(blobStoreLocation) }" required="required"/>
                                </div>
                                <div class="col-xs-4 text-right">
                                    <button id="fs-relocate" type="button" class="btn btn-warning" data-toggle="modal" data-target="#fs-modal">Relocate</button>
                                </div>
                             </div>
                        </form>
                    </div>
                </div>
            </c:if>
            
        </div>
        
        
    </div>
</div>

<div class="modal" id="wipe-modal" tabindex="-1" role="dialog"
	aria-labelledby="wipe-modal-label" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<h4 class="modal-title" id="wipe-modal-label">Confirm operation</h4>
			</div>
			<div class="modal-body">
                This will wipe your storage. If you don't have a backup, all your channels and artifacts will be gone.
			</div>
			<div class="modal-footer">
                <form action="<c:url value="/system/storage/wipe"/>" method="POST">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button id="wipe-submit" type="submit" class="btn btn-danger">
                        <span class="glyphicon glyphicon-trash"></span> Wipe storage
                    </button>
                </form>
            </div>
		</div>
	</div>
</div>

<div class="modal" id="fs-modal" tabindex="-1" role="dialog"
    aria-labelledby="fs-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="fs-modal-label">Confirm operation</h4>
            </div>
            <div class="modal-body container">
                <c:choose>
                    <c:when test="${not empty blobStoreLocation }">
               <p>
                   Changing the location of the blob store might corrupt your artifacts if the target is not matching to your database.
               </p>
                    </c:when>
                    <c:otherwise>
               <p>
                   This will convert your storage to use a file system backend. It cannot be reversed at the moment. Do you really want to do this?
               </p>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="modal-footer">
                   <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                   <button id="fs-submit" type="button" onclick="$('#fs-form').submit();" class="btn btn-warning">${ (empty blobStoreLocation ? 'Convert ' : 'Relocate ' ) } storage</button>
            </div>
        </div>
    </div>
    
</div>

</h:main>