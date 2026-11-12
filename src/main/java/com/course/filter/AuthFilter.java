package com.course.filter;

import com.course.model.User;
import com.course.util.Flash;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/** Guards /student/* (any logged-in user) and /admin/* (admins only). */
@WebFilter(urlPatterns = {"/student/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            Flash.error(req, "Please log in to continue.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (req.getRequestURI().startsWith(req.getContextPath() + "/admin") && !user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrators only.");
            return;
        }

        // Stop the browser showing cached protected pages after logout (back button).
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        chain.doFilter(request, response);
    }
}
