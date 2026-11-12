<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Register" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="card auth-card">
    <h1>Create your account</h1>
    <p class="muted">Student registration takes less than a minute.</p>

    <c:if test="${not empty errors}">
        <div class="alert error">
            <ul class="plain">
                <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
            </ul>
        </div>
    </c:if>

    <form method="post" action="${ctx}/register">
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <label for="name">Full name</label>
        <input type="text" id="name" name="name" value="<c:out value='${name}'/>" maxlength="100" required autofocus>

        <label for="email">Email</label>
        <input type="email" id="email" name="email" value="<c:out value='${email}'/>" maxlength="120" required>

        <label for="password">Password <small class="muted">(at least 6 characters)</small></label>
        <input type="password" id="password" name="password" minlength="6" required>

        <label for="confirm">Confirm password</label>
        <input type="password" id="confirm" name="confirm" minlength="6" required>

        <button type="submit" class="btn btn-primary btn-block">Register</button>
    </form>
    <p class="muted center">Already registered? <a href="${ctx}/login">Log in</a></p>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
