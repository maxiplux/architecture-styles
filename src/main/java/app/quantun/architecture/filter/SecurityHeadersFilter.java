package app.quantun.architecture.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // Prevent MIME sniffing
        httpServletResponse.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking
        httpServletResponse.setHeader("X-Frame-Options", "DENY");

        // Enable XSS filtering
        httpServletResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // strict CSP
        httpServletResponse.setHeader("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'; object-src 'none';");

        chain.doFilter(request, response);
    }
}
