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
        </div>
        
        <div class="col-sm-6">
            <div class="panel panel-default">
                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-import"></span> Import all channels</h3></div>
                <div class="panel-body">
                    Import all channels from a full export.
                </div>
                <div class="panel-body text-right">
                    <a href="<c:url value="/channel/importAll"/>" role="button" class="btn btn-primary"><span class="glyphicon glyphicon-import"></span> Import</a>
                </div>
            </div>
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
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					<button type="submit" class="btn btn-danger">
						<span class="glyphicon glyphicon-trash"></span> Wipe storage
					</button>
				</form>
			</div>
		</div>
	</div>
</div>

</h:main>