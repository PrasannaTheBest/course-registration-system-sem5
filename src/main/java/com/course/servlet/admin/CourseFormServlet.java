package com.course.servlet.admin;

import com.course.dao.CourseDAO;
import com.course.model.Course;
import com.course.util.Flash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Add / edit a course. GET shows the form (with ?id= to edit), POST validates and saves. */
@WebServlet("/admin/course-form")
public class CourseFormServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/admin/course-form.jsp";
    private final CourseDAO courses = new CourseDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Course course = new Course();
        String id = req.getParameter("id");
        if (id != null && !id.isEmpty()) {
            try {
                course = courses.findById(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                course = null;
            } catch (SQLException e) {
                throw new ServletException(e);
            }
            if (course == null) {
                Flash.error(req, "Course not found.");
                resp.sendRedirect(req.getContextPath() + "/admin/courses");
                return;
            }
        }
        req.setAttribute("course", course);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Course course = new Course();
        List<String> errors = new ArrayList<>();

        try {
            course.setId(parseInt(req.getParameter("id"), 0));
            course.setCode(text(req, "code").toUpperCase());
            course.setTitle(text(req, "title"));
            course.setInstructor(text(req, "instructor"));
            course.setDescription(text(req, "description"));
            course.setCredits(parseInt(req.getParameter("credits"), -1));
            course.setSeats(parseInt(req.getParameter("seats"), -1));

            if (course.getCode().length() < 3 || course.getCode().length() > 20) {
                errors.add("Course code must be 3-20 characters.");
            }
            if (course.getTitle().isEmpty() || course.getTitle().length() > 150) {
                errors.add("Title is required (max 150 characters).");
            }
            if (course.getInstructor().isEmpty() || course.getInstructor().length() > 100) {
                errors.add("Instructor is required (max 100 characters).");
            }
            if (course.getCredits() < 1 || course.getCredits() > 10) {
                errors.add("Credits must be a number from 1 to 10.");
            }
            if (course.getSeats() < 1 || course.getSeats() > 500) {
                errors.add("Seats must be a number from 1 to 500.");
            }
            if (course.getDescription().length() > 500) {
                errors.add("Description can be at most 500 characters.");
            }

            if (errors.isEmpty() && course.getId() != 0) {
                Course existing = courses.findById(course.getId());
                if (existing == null) {
                    Flash.error(req, "Course not found.");
                    resp.sendRedirect(req.getContextPath() + "/admin/courses");
                    return;
                }
                if (course.getSeats() < existing.getEnrolled()) {
                    errors.add("Seats cannot be fewer than the " + existing.getEnrolled() + " students already enrolled.");
                }
            }
            if (errors.isEmpty() && !courses.save(course)) {
                errors.add("Another course already uses the code " + course.getCode() + ".");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("course", course);
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }
        Flash.success(req, "Course " + course.getCode() + (course.getId() == 0 ? " added." : " updated."));
        resp.sendRedirect(req.getContextPath() + "/admin/courses");
    }

    private static String text(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
