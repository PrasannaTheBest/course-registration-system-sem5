<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Log in" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="card auth-card">
    <h1>Welcome back</h1>
    <p class="muted">Log in to browse and register for courses.</p>

    <c:if test="${not empty error}">
        <div class="alert error"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${ctx}/login">
        <label for="email">Email</label>
        <input type="email" id="email" name="email" value="<c:out value='${email}'/>" required autofocus>

        <label for="password">Password</label>
        <input type="password" id="password" name="password" required>

        <button type="submit" class="btn btn-primary btn-block">Log in</button>
    </form>
    <p class="muted center">New student? <a href="${ctx}/register">Create an account</a></p>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
