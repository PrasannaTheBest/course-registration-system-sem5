<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Enrolled Students" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="page-head">
    <div>
        <h1><c:out value="${course.code}"/> &ndash; <c:out value="${course.title}"/></h1>
        <p class="muted">
            <c:out value="${course.instructor}"/> &middot; ${course.enrolled} of ${course.seats} seats taken
        </p>
    </div>
    <a class="btn btn-light" href="${ctx}/admin/courses">&larr; Back to courses</a>
</div>

<c:choose>
    <c:when test="${empty students}">
        <div class="card empty">No students have enrolled in this course yet.</div>
    </c:when>
    <c:otherwise>
        <div class="card table-card">
            <table>
                <thead><tr><th>#</th><th>Student</th><th>Email</th><th>Enrolled on</th></tr></thead>
                <tbody>
                <c:forEach var="s" items="${students}" varStatus="st">
                    <tr>
                        <td>${st.count}</td>
                        <td><c:out value="${s.studentName}"/></td>
                        <td><c:out value="${s.studentEmail}"/></td>
                        <td><fmt:formatDate value="${s.enrolledAt}" pattern="dd MMM yyyy, HH:mm"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
