package com.course.servlet.student;

import com.course.dao.CourseDAO;
import com.course.dao.EnrollmentDAO;
import com.course.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Course catalogue with search; shows which courses the student is already in. */
@WebServlet("/student/courses")
public class CourseListServlet extends HttpServlet {
    private final CourseDAO courses = new CourseDAO();
    private final EnrollmentDAO enrollments = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        String q = req.getParameter("q") == null ? "" : req.getParameter("q").trim();
        try {
            req.setAttribute("q", q);
            req.setAttribute("courses", courses.search(q));
            req.setAttribute("enrolledIds", enrollments.enrolledCourseIds(user.getId()));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/student/courses.jsp").forward(req, resp);
    }
}
