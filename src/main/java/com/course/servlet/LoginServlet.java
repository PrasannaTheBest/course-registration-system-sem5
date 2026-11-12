package com.course.servlet;

import com.course.dao.UserDAO;
import com.course.model.User;
import com.course.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(homeFor(req, (User) session.getAttribute("user")));
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = param(req, "email");
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        try {
            User user = email.isEmpty() ? null : users.findByEmail(email);
            if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
                req.setAttribute("error", "Invalid email or password.");
                req.setAttribute("email", email);
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }
            // Start a fresh session on login (prevents session fixation) and keep the hash out of it.
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            User sessionUser = new User(user.getId(), user.getName(), user.getEmail(), null, user.getRole());
            req.getSession(true).setAttribute("user", sessionUser);
            resp.sendRedirect(homeFor(req, sessionUser));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    static String homeFor(HttpServletRequest req, User user) {
        return req.getContextPath() + (user.isAdmin() ? "/admin/courses" : "/student/courses");
    }

    private static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }
}
