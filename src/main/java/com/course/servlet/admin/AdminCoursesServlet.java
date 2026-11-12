package com.course.servlet.admin;

import com.course.dao.CourseDAO;
import com.course.dao.EnrollmentDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Admin dashboard: headline numbers plus the full course table. */
@WebServlet("/admin/courses")
public class AdminCoursesServlet extends HttpServlet {
    private final CourseDAO courses = new CourseDAO();
    private final EnrollmentDAO enrollments = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("courses", courses.search(null));
            req.setAttribute("stats", enrollments.stats());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/courses.jsp").forward(req, resp);
    }
}
