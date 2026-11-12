<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Something went wrong" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="card auth-card center">
    <h1>
        <c:choose>
            <c:when test="${pageContext.errorData.statusCode == 404}">Page not found</c:when>
            <c:when test="${pageContext.errorData.statusCode == 403}">Access denied</c:when>
            <c:otherwise>Something went wrong</c:otherwise>
        </c:choose>
    </h1>
    <p class="muted">
        <c:choose>
            <c:when test="${pageContext.errorData.statusCode == 404}">The page you are looking for does not exist.</c:when>
            <c:when test="${pageContext.errorData.statusCode == 403}">You do not have permission to view this page.</c:when>
            <c:otherwise>An unexpected error occurred. Please try again.</c:otherwise>
        </c:choose>
    </p>
    <a class="btn btn-primary" href="${ctx}/">Back to home</a>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
