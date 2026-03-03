package egovframework.com.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class AuthorizeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith("/error") || uri.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String secretCodeId = request.getHeader("X-CODE-ID");

        if (!request.getRequestURI().contains("/uat/uia")) {
            String secretCode = "-WzAnecnlNewSEQwDgJ2BQ";
            if (ObjectUtils.isEmpty(secretCodeId) || !secretCode.equals(secretCodeId)) {
                log.warn("##### Access Denied: Unauthorized Access Attempt uri={}", uri);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":403,\"message\":\"Forbidden\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|woff|woff2|ttf|otf|eot|ico|html)$") ||
                path.startsWith("/error") ||
                path.startsWith("/actuator");
    }

}
