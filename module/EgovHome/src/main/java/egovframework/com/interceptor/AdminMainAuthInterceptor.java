package egovframework.com.interceptor;

import egovframework.com.uat.uia.util.EgovJwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class AdminMainAuthInterceptor implements HandlerInterceptor {

    private final EgovJwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessToken = jwtProvider.getCookie(request, "accessToken");
        if (ObjectUtils.isEmpty(accessToken) || jwtProvider.accessValidateToken(accessToken) != 200) {
            response.sendRedirect("/admin/login/loginView");
            return false;
        }
        return true;
    }
}
