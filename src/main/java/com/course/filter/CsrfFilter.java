package com.course.filter;

import com.course.util.Flash;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF protection (synchroniser token pattern).
 * <ul>
 *   <li>Every page view makes sure the session holds a random token.</li>
 *   <li>Every form includes it as a hidden field named {@value #PARAM}.</li>
 *   <li>Every POST must send back the same token, otherwise it is rejected.</li>
 * </ul>
 * A malicious site cannot read the token, so it cannot forge a valid POST.
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {
    public static final String ATTR = "csrfToken";
    public static final String PARAM = "_csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Set the encoding before any getParameter() call, otherwise non-ASCII input breaks.
        req.setCharacterEncoding("UTF-8");

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            HttpSession session = req.getSession(false);
            String expected = session == null ? null : (String) session.getAttribute(ATTR);
            String actual = req.getParameter(PARAM);
            if (expected == null || actual == null || !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8))) {
                Flash.error(req, "Your session expired or the form was invalid. Please try again.");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
        } else if (!req.getRequestURI().startsWith(req.getContextPath() + "/css/")) {
            HttpSession session = req.getSession(true);
            if (session.getAttribute(ATTR) == null) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                session.setAttribute(ATTR, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
            }
        }
        chain.doFilter(request, response);
    }
}
