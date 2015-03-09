<%@page import="de.dentrassi.pm.sec.DatabaseDetails"%>
<%@page import="de.dentrassi.pm.sec.UserInformationPrincipal"%>
<%@page import="java.security.Principal"%>
<%@page import="javax.servlet.http.HttpServletRequest"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<%
Principal principal = request.getUserPrincipal ();
if ( principal instanceof UserInformationPrincipal )
{
    DatabaseDetails db = ((UserInformationPrincipal)principal).getUserInformation().getDetails ( DatabaseDetails.class  );
    if ( db != null )
    {
        pageContext.setAttribute ( "email", db.getEmail () );
    }
}
%>
<h:main title="Default Mail" subtitle="Setup">

<div class="container-fluid form-padding">

<div class="row">

    <div class="col-md-8">

		    <form:form action="" method="POST" cssClass="form-horizontal">
		    
		        <h:formEntry label="User" path="username" command="command">
		            <form:input path="username" cssClass="form-control" placeholder="Username for the mail server"/>
		        </h:formEntry>
		        
		        <h:formEntry label="Password" path="password" command="command">
		            <form:input path="password" cssClass="form-control" type="password" placeholder="Password for the mail server"/>
		        </h:formEntry>
		        
		        <h:formEntry label="Host" path="host" command="command">
		            <form:input path="host" cssClass="form-control"  placeholder="Hostname or IP of the SMTP server"/>
		        </h:formEntry>
		        
		        <h:formEntry label="Port" path="port" command="command">
		            <form:input path="port" cssClass="form-control" type="number"  placeholder="Optional port number of the SMTP server"/>
		        </h:formEntry>
		        
		        <h:formEntry label="From" path="from" command="command">
                    <form:input path="from" cssClass="form-control"  placeholder="Optional sender e-mail"/>
                </h:formEntry>
                
                <h:formEntry label="Prefix" path="prefix" command="command">
                    <form:input path="prefix" cssClass="form-control"  placeholder="Optional subject prefix"/>
                </h:formEntry>
		    
		        <div class="form-group">
		            <div class="col-sm-offset-2 col-sm-10">
		                <button type="submit" class="btn btn-primary">Update</button>
		                <button type="reset" class="btn btn-default">Reset</button>
		            </div>
		        </div>
		    </form:form>
		
	</div><%-- form col --%>
	
	<div class="col-md-4">
	
	   <div class="panel panel-info">
	       <div class="panel-heading"><h3 class="panel-title">Mail Service</h3></div>
	       
	       <%-- info about mail service --%>
	       
	       <table class="table">
	           <tbody>
	               <tr><th>Service Present</th><td id="servicePresent">${servicePresent }</td></tr>
	           </tbody>
	       </table>
	       
	       
         <c:if test="${servicePresent }">
	         <div class="panel-body">
             <form class="form-inline" action="<c:url value="/default.mail/config/sendTest"/>" method="post">
                 <div class="form-group">
                    <p class="form-control-static">Test E-Mail</p>
                 </div>
                 <div class="form-group">
                     <label class="sr-only" for="testEmailReceiver">Receiver Email address</label>
                     <input type="email" class="form-control"  id="testEmailReceiver" name="testEmailReceiver" placeholder="Receiver of test e-mail" value="${fn:escapeXml(email) }"/>
                 </div>
                 <button type="submit" class="btn btn-default">Send</button>
             </form>
             </div>
          </c:if>
	       
	   </div>
	
	</div> <%-- info col --%>

</div><%-- outer row --%>

</div><%-- outer container --%>

</h:main>