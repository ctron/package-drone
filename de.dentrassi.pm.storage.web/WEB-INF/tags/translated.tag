<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>

<%@attribute name="data" required="true" type="java.lang.Object"%>
<%@attribute name="property" required="true" type="java.lang.String"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

${fn:escapeXml(data.translate(data[property])) }
<h:translatedLabels data="${data }" property="${property }" />