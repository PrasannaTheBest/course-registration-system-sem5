package com.course.servlet.student;

import com.course.dao.EnrollmentDAO;
import com.course.model.Course;
import com.course.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/student/my-courses")
public class MyCoursesServlet extends HttpServlet {
    private final EnrollmentDAO enrollments = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        try {
            List<Course> mine = enrollments.coursesForUser(user.getId());
            int totalCredits = mine.stream().mapToInt(Course::getCredits).sum();
            req.setAttribute("courses", mine);
            req.setAttribute("totalCredits", totalCredits);
            req.setAttribute("maxCredits", EnrollmentDAO.MAX_CREDITS);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/student/my-courses.jsp").forward(req, resp);
    }
}
