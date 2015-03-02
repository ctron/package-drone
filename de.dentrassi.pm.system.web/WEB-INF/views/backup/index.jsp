<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="System" subtitle="Backup">

<div class="container-fluid">

    <div class="row">
    
        <div class="col-xs-6">
            
            <div class="panel panel-success clearfix">
                <div class="panel-heading">
                    <h3 class="panel-title">Export configuration</h3>
                </div>
                <div class="panel-body">
                    Click on the following link to to export the system configuration as
                    a complete ZIP file. The ZIP file can later be used to restore the
                    system configuration.
                </div>
                <div class="panel-body pull-right">
                    <a class="btn btn-default" href="<c:url value="/system/backup/export"/>">Export</a>
                </div>
            </div>
            
        </div>
        
        <div class="col-xs-6">
            <div class="alert alert-info">
                <strong>Remember!</strong> This only backs up the system <em>configuration</em>.
                It <em>does not</em> export any system data, like channels, artifacts, users or
                anything else which is stored in the database!
            </div>
        </div>
    
    </div>

</div>

</h:main>