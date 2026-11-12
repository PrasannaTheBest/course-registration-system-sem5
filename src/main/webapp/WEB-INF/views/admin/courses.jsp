<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Manage Courses" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="page-head">
    <div>
        <h1>Manage courses</h1>
        <p class="muted">Add, edit or remove courses and see who is enrolled.</p>
    </div>
    <a class="btn btn-primary" href="${ctx}/admin/course-form">+ Add course</a>
</div>

<div class="stats">
    <div class="card stat"><strong>${stats.courses}</strong><span>Courses</span></div>
    <div class="card stat"><strong>${stats.students}</strong><span>Registered students</span></div>
    <div class="card stat"><strong>${stats.enrollments}</strong><span>Enrolments</span></div>
</div>

<c:choose>
    <c:when test="${empty courses}">
        <div class="card empty">No courses yet. Add the first one.</div>
    </c:when>
    <c:otherwise>
        <div class="card table-card">
            <table>
                <thead>
                <tr><th>Code</th><th>Title</th><th>Instructor</th><th>Credits</th><th>Enrolled</th><th></th></tr>
                </thead>
                <tbody>
                <c:forEach var="c" items="${courses}">
                    <tr>
                        <td><span class="code"><c:out value="${c.code}"/></span></td>
                        <td><c:out value="${c.title}"/></td>
                        <td><c:out value="${c.instructor}"/></td>
                        <td>${c.credits}</td>
                        <td>
                            <span class="seats ${c.full ? 'seats-full' : ''}">${c.enrolled} / ${c.seats}</span>
                        </td>
                        <td class="right actions">
                            <a class="btn btn-light btn-sm" href="${ctx}/admin/enrollments?courseId=${c.id}">Students</a>
                            <a class="btn btn-light btn-sm" href="${ctx}/admin/course-form?id=${c.id}">Edit</a>
                            <form method="post" action="${ctx}/admin/course-delete" class="inline"
                                  onsubmit="return confirm('Delete this course and all its enrolments?');">
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${c.id}">
                                <button type="submit" class="btn btn-danger btn-sm">Delete</button>
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
