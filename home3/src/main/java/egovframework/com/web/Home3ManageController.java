package egovframework.com.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Controller
public class Home3ManageController {

    private final TemplateEngine templateEngine;

    public Home3ManageController(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @RequestMapping(value = "/home3", method = { RequestMethod.GET, RequestMethod.POST })
    public String index(Locale locale, org.springframework.ui.Model model,
            @org.springframework.web.bind.annotation.CookieValue(value = "accessToken", required = false) String accessToken) {
        model.addAttribute("isLoggedIn", accessToken != null);
        return "home";
    }

    @RequestMapping(value = "/home3/en", method = { RequestMethod.GET, RequestMethod.POST })
    public String indexEn(Locale locale, org.springframework.ui.Model model,
            @org.springframework.web.bind.annotation.CookieValue(value = "accessToken", required = false) String accessToken) {
        model.addAttribute("isLoggedIn", accessToken != null);
        return "home_en";
    }

    /**
     * 공유 헤더 fragment 반환 - 다른 MSA 모듈에서 fetch로 사용 가능
     * GET /home3/fragments/header
     */
    @GetMapping(value = "/home3/fragments/header", produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String headerFragment() {
        Context ctx = new Context();
        String full = templateEngine.process("fragments/header", ctx);

        // <body> 태그 내 fragment 부분만 추출
        int start = full.indexOf("<div th:fragment");
        if (start == -1)
            start = full.indexOf("<div ");
        int end = full.lastIndexOf("</body>");
        if (start >= 0 && end > start) {
            return full.substring(start, end).trim();
        }
        return full;
    }

    /**
     * 공유 푸터 fragment 반환 - 다른 MSA 모듈에서 fetch로 사용 가능
     * GET /home3/fragments/footer
     */
    @GetMapping(value = "/home3/fragments/footer", produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String footerFragment() {
        Context ctx = new Context();
        String full = templateEngine.process("fragments/footer", ctx);

        int start = full.indexOf("<footer");
        int end = full.lastIndexOf("</body>");
        if (start >= 0 && end > start) {
            return full.substring(start, end).trim();
        }
        return full;
    }

}
