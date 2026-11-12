<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="editing" value="${course.id != 0}"/>
<c:set var="pageTitle" value="${editing ? 'Edit Course' : 'Add Course'}" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="card form-card">
    <h1>${editing ? 'Edit course' : 'Add a course'}</h1>

    <c:if test="${not empty errors}">
        <div class="alert error">
            <ul class="plain">
                <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
            </ul>
        </div>
    </c:if>

    <form method="post" action="${ctx}/admin/course-form">
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="id" value="${course.id}">

        <div class="row">
            <div>
                <label for="code">Course code</label>
                <input type="text" id="code" name="code" value="<c:out value='${course.code}'/>"
                       maxlength="20" placeholder="e.g. CS101" required>
            </div>
            <div>
                <label for="credits">Credits</label>
                <input type="number" id="credits" name="credits" min="1" max="10"
                       value="${course.credits > 0 ? course.credits : ''}" required>
            </div>
            <div>
                <label for="seats">Seats</label>
                <input type="number" id="seats" name="seats" min="1" max="500"
                       value="${course.seats > 0 ? course.seats : ''}" required>
            </div>
        </div>

        <label for="title">Title</label>
        <input type="text" id="title" name="title" value="<c:out value='${course.title}'/>" maxlength="150" required>

        <label for="instructor">Instructor</label>
        <input type="text" id="instructor" name="instructor" value="<c:out value='${course.instructor}'/>"
               maxlength="100" required>

        <label for="description">Description <small class="muted">(optional)</small></label>
        <textarea id="description" name="description" rows="4" maxlength="500"><c:out value="${course.description}"/></textarea>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary">${editing ? 'Save changes' : 'Add course'}</button>
            <a class="btn btn-light" href="${ctx}/admin/courses">Cancel</a>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
