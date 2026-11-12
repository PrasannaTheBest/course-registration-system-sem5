<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Browse Courses" scope="request"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="page-head">
    <div>
        <h1>Available courses</h1>
        <p class="muted">Pick a course and register. Seats are first come, first served.</p>
    </div>
    <form method="get" action="${ctx}/student/courses" class="search">
        <input type="search" name="q" value="<c:out value='${q}'/>" placeholder="Search code, title or instructor">
        <button type="submit" class="btn btn-primary">Search</button>
        <c:if test="${not empty q}"><a class="btn btn-light" href="${ctx}/student/courses">Clear</a></c:if>
    </form>
</div>

<c:choose>
    <c:when test="${empty courses}">
        <div class="card empty">No courses found<c:if test="${not empty q}"> for "<c:out value='${q}'/>"</c:if>.</div>
    </c:when>
    <c:otherwise>
        <div class="grid">
            <c:forEach var="c" items="${courses}" varStatus="st">
                <article class="card course" style="--i: ${st.index}">
                    <div class="course-top">
                        <span class="code"><c:out value="${c.code}"/></span>
                        <span class="chip"><c:out value="${c.credits}"/> credits</span>
                    </div>
                    <h2><c:out value="${c.title}"/></h2>
                    <p class="muted instructor">&#128100; <c:out value="${c.instructor}"/></p>
                    <p class="desc"><c:out value="${c.description}"/></p>
                    <div class="meter" title="${c.enrolled} of ${c.seats} seats taken">
                        <div class="meter-fill ${c.full ? 'meter-full' : ''}" style="--w: ${(c.enrolled * 100) div c.seats}%"></div>
                    </div>
                    <div class="course-bottom">
                        <span class="seats ${c.full ? 'seats-full' : ''}">
                            <c:choose>
                                <c:when test="${c.full}">No seats left</c:when>
                                <c:otherwise>${c.seatsLeft} of ${c.seats} seats left</c:otherwise>
                            </c:choose>
                        </span>
                        <c:choose>
                            <c:when test="${enrolledIds.contains(c.id)}">
                                <span class="badge badge-ok">&#10003; Enrolled</span>
                            </c:when>
                            <c:when test="${c.full}">
                                <button class="btn btn-light btn-sm" disabled>Full</button>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${ctx}/student/enroll" class="inline">
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="courseId" value="${c.id}">
                                    <input type="hidden" name="q" value="<c:out value='${q}'/>">
                                    <button type="submit" class="btn btn-primary btn-sm">Enroll</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
