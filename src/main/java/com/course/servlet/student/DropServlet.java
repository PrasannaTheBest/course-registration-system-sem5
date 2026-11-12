package com.course.servlet.student;

import com.course.dao.EnrollmentDAO;
import com.course.model.User;
import com.course.util.Flash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/student/drop")
public class DropServlet extends HttpServlet {
    private final EnrollmentDAO enrollments = new EnrollmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        try {
            int courseId = Integer.parseInt(req.getParameter("courseId"));
            if (enrollments.drop(user.getId(), courseId)) {
                Flash.success(req, "Course dropped.");
            } else {
                Flash.error(req, "You were not enrolled in that course.");
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course.");
            return;
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/student/my-courses");
    }
}
