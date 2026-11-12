package com.course.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** One-time messages shown after a redirect (the header JSP displays and clears them). */
public final class Flash {
    private Flash() {
    }

    public static void success(HttpServletRequest req, String message) {
        set(req, "success", message);
    }

    public static void error(HttpServletRequest req, String message) {
        set(req, "error", message);
    }

    private static void set(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute("flash", message);
        session.setAttribute("flashType", type);
    }
}
