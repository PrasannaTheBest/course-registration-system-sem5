package com.course.servlet;

import com.course.dao.UserDAO;
import com.course.model.User;
import com.course.util.Flash;
import com.course.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = value(req, "name");
        String email = value(req, "email");
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirm") == null ? "" : req.getParameter("confirm");

        List<String> errors = new ArrayList<>();
        if (name.length() < 2 || name.length() > 100) {
            errors.add("Enter your full name (2-100 characters).");
        }
        if (!EMAIL.matcher(email).matches() || email.length() > 120) {
            errors.add("Enter a valid email address.");
        }
        if (password.length() < 6) {
            errors.add("Password must be at least 6 characters.");
        }
        if (!password.equals(confirm)) {
            errors.add("Passwords do not match.");
        }

        try {
            if (errors.isEmpty() && !users.create(name, email, PasswordUtil.hash(password), User.STUDENT)) {
                errors.add("An account with this email already exists.");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }
        Flash.success(req, "Account created. Please log in.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    private static String value(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }
}
