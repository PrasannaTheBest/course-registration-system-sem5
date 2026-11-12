<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty sessionScope.user}">
    <c:redirect url="${sessionScope.user.admin ? '/admin/courses' : '/student/courses'}"/>
</c:if>
<c:set var="pageTitle" value="Home" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<section class="hero">
    <h1>Register for your courses online</h1>
    <p>Browse the course catalogue, check available seats and enrol in seconds. No queues, no paperwork.</p>
    <div class="hero-actions">
        <a class="btn btn-primary" href="${ctx}/register">Create student account</a>
        <a class="btn btn-light" href="${ctx}/login">Log in</a>
    </div>
</section>

<section class="features">
    <div class="card"><h3>&#128269; Find courses</h3><p class="muted">Search by code, title or instructor.</p></div>
    <div class="card"><h3>&#9989; Enrol instantly</h3><p class="muted">Seat limits are enforced, so you always know what is available.</p></div>
    <div class="card"><h3>&#128218; Track your load</h3><p class="muted">See your registered courses and total credits at a glance.</p></div>
</section>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
