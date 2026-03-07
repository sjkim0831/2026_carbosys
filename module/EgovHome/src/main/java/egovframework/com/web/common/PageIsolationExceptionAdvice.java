package egovframework.com.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice(annotations = Controller.class)
public class PageIsolationExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(PageIsolationExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public Object handlePageException(Exception ex, HttpServletRequest request) {
        final String uri = request.getRequestURI();
        log.error("Page-level error isolated for uri={}", uri, ex);

        // API/AJAX endpoints should receive structured error without affecting main page runtime.
        if (uri.startsWith("/api/") || uri.endsWith(".json")) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "error");
            body.put("message", "페이지 처리 중 오류가 발생했습니다.");
            body.put("path", uri);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }

        ModelAndView mv = new ModelAndView("egovframework/com/legacy/page_error");
        mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mv.addObject("errorPath", uri);
        mv.addObject("errorMessage", "요청한 페이지 처리 중 오류가 발생했습니다. 메인 화면은 계속 사용할 수 있습니다.");
        return mv;
    }
}
