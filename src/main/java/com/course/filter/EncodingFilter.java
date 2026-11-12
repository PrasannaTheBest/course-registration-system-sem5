package com.course.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/** Makes every request/response use UTF-8 so non-English names are stored correctly. */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    // Defined explicitly so the filter also runs on Servlet 3.1 containers (e.g. Jetty 9.4),
    // where Filter.init/destroy are not default methods.
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
