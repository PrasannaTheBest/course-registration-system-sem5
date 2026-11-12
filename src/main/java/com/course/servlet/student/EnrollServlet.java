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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/student/enroll")
public class EnrollServlet extends HttpServlet {
    private final EnrollmentDAO enrollments = new EnrollmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        int courseId;
        try {
            courseId = Integer.parseInt(req.getParameter("courseId"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course.");
            return;
        }

        try {
            EnrollmentDAO.Result result = enrollments.enroll(user.getId(), courseId);
            switch (result) {
                case ENROLLED:
                    Flash.success(req, "You are now enrolled in the course.");
                    break;
                case FULL:
                    Flash.error(req, "Sorry, this course is full.");
                    break;
                case ALREADY_ENROLLED:
                    Flash.error(req, "You are already enrolled in this course.");
                    break;
                default:
                    Flash.error(req, "That course no longer exists.");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        String q = req.getParameter("q") == null ? "" : req.getParameter("q");
        String target = req.getContextPath() + "/student/courses";
        if (!q.isEmpty()) {
            target += "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        }
        resp.sendRedirect(target);
    }
}
