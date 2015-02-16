<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Connection Test" subtitle="Test result">

<script type="text/javascript">
function testConnection () {
	var form = $('#command');
	form.attr("action", "<c:url value="/config/testConnection"/>");
	form.submit ();
	return false;
}
</script>

<div class="container-fluid ">

	<div class="row">
	
		<div class="col-md-8 col-md-offset-2">
		
		  <div class="alert alert-${type }">
		      <strong>${fn:escapeXml(shortMessage) }</strong> ${fn:escapeXml(message) }
		  </div>
		
        </div> <%-- col --%>
	
	</div> <%-- row --%>

</div> <%-- container --%>


</h:main>