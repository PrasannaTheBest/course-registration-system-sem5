package com.course.servlet.admin;

import com.course.dao.CourseDAO;
import com.course.util.Flash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/course-delete")
public class CourseDeleteServlet extends HttpServlet {
    private final CourseDAO courses = new CourseDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            courses.delete(Integer.parseInt(req.getParameter("id")));
            Flash.success(req, "Course deleted.");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course.");
            return;
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/courses");
    }
}
