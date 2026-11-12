package com.course.servlet.admin;

import com.course.dao.CourseDAO;
import com.course.dao.EnrollmentDAO;
import com.course.model.Course;
import com.course.util.Flash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Lists the students enrolled in one course. */
@WebServlet("/admin/enrollments")
public class CourseStudentsServlet extends HttpServlet {
    private final CourseDAO courses = new CourseDAO();
    private final EnrollmentDAO enrollments = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Course course = courses.findById(Integer.parseInt(req.getParameter("courseId")));
            if (course == null) {
                Flash.error(req, "Course not found.");
                resp.sendRedirect(req.getContextPath() + "/admin/courses");
                return;
            }
            req.setAttribute("course", course);
            req.setAttribute("students", enrollments.studentsForCourse(course.getId()));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course.");
            return;
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/enrollments.jsp").forward(req, resp);
    }
}
