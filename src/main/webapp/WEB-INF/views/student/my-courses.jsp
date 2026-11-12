<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="My Courses" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="page-head">
    <div>
        <h1>My courses</h1>
        <p class="muted">Courses you are registered for this semester.</p>
    </div>
    <div class="stat-inline"><strong>${totalCredits} / ${maxCredits}</strong><span>credits registered</span></div>
</div>

<c:choose>
    <c:when test="${empty courses}">
        <div class="card empty">
            You have not registered for any course yet.
            <a class="btn btn-primary btn-sm" href="${ctx}/student/courses">Browse courses</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="card table-card">
            <table>
                <thead>
                <tr><th>Code</th><th>Course</th><th>Instructor</th><th>Credits</th><th>Enrolled on</th><th></th></tr>
                </thead>
                <tbody>
                <c:forEach var="c" items="${courses}">
                    <tr>
                        <td><span class="code"><c:out value="${c.code}"/></span></td>
                        <td><c:out value="${c.title}"/></td>
                        <td><c:out value="${c.instructor}"/></td>
                        <td>${c.credits}</td>
                        <td><fmt:formatDate value="${c.enrolledAt}" pattern="dd MMM yyyy, HH:mm"/></td>
                        <td class="right">
                            <form method="post" action="${ctx}/student/drop" class="inline"
                                  onsubmit="return confirm('Drop this course?');">
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="courseId" value="${c.id}">
                                <button type="submit" class="btn btn-danger btn-sm">Drop</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
