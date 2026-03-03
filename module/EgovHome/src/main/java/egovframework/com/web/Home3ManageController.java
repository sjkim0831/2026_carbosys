package egovframework.com.web;

import egovframework.com.uat.uia.entity.EntrprsMber;
import egovframework.com.uat.uia.repository.EgovEnterpriseMemberRepository;
import egovframework.com.uat.uia.util.EgovJwtProvider;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Optional;

@Controller
public class Home3ManageController {

    private final TemplateEngine templateEngine;
    private final EgovJwtProvider jwtProvider;
    private final EgovEnterpriseMemberRepository enterpriseMemberRepository;

    public Home3ManageController(
            TemplateEngine templateEngine,
            EgovJwtProvider jwtProvider,
            EgovEnterpriseMemberRepository enterpriseMemberRepository) {
        this.templateEngine = templateEngine;
        this.jwtProvider = jwtProvider;
        this.enterpriseMemberRepository = enterpriseMemberRepository;
    }

    @RequestMapping(value = { "/home", "/home3" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String index(Locale locale, org.springframework.ui.Model model,
            @org.springframework.web.bind.annotation.CookieValue(value = "accessToken", required = false) String accessToken) {
        model.addAttribute("isLoggedIn", accessToken != null);
        return "egovframework/com/home";
    }

    @RequestMapping(value = { "/home/en", "/home3/en" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String indexEn(Locale locale, org.springframework.ui.Model model,
            @org.springframework.web.bind.annotation.CookieValue(value = "accessToken", required = false) String accessToken) {
        model.addAttribute("isLoggedIn", accessToken != null);
        return "egovframework/com/home_en";
    }

    @RequestMapping(value = { "/mypage" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String mypage(Model model, HttpServletRequest request) {
        return renderMypageByInsttStatus(false, model, request);
    }

    @RequestMapping(value = { "/mypage/en" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String mypageEn(Model model, HttpServletRequest request) {
        return renderMypageByInsttStatus(true, model, request);
    }

    private String renderMypageByInsttStatus(boolean en, Model model, HttpServletRequest request) {
        String accessToken = jwtProvider.getCookie(request, "accessToken");
        if (ObjectUtils.isEmpty(accessToken)) {
            return en ? "redirect:/signin/loginView?language=en" : "redirect:/signin/loginView";
        }

        String userId = extractUserId(accessToken);
        if (ObjectUtils.isEmpty(userId)) {
            return en ? "redirect:/signin/loginView?language=en" : "redirect:/signin/loginView";
        }

        model.addAttribute("isLoggedIn", true);
        Optional<EntrprsMber> enterpriseOpt = enterpriseMemberRepository.findById(userId);
        if (enterpriseOpt.isEmpty()) {
            return en ? "egovframework/com/mypage_en" : "egovframework/com/mypage";
        }

        EntrprsMber enterprise = enterpriseOpt.get();
        String entrprsMberSttus = ObjectUtils.isEmpty(enterprise.getEntrprsMberStus())
                ? ""
                : enterprise.getEntrprsMberStus().trim();

        // COMTNENTRPRSMBER 기준: A(승인 대기)일 때만 승인 대기 화면 표시
        if ("A".equalsIgnoreCase(entrprsMberSttus)) {
            model.addAttribute("submittedAt", "-");
            model.addAttribute("userId", userId);
            model.addAttribute("companyName", ObjectUtils.isEmpty(enterprise.getCmpnyNm()) ? "-" : enterprise.getCmpnyNm());
            return en ? "egovframework/com/mypage_pending_en" : "egovframework/com/mypage_pending";
        }

        return en ? "egovframework/com/mypage_en" : "egovframework/com/mypage";
    }

    private String extractUserId(String accessToken) {
        try {
            Claims claims = jwtProvider.accessExtractClaims(accessToken);
            Object encryptedUserId = claims.get("userId");
            if (encryptedUserId == null) {
                return "";
            }
            return jwtProvider.decrypt(encryptedUserId.toString());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 공유 헤더 fragment 반환 - 다른 MSA 모듈에서 fetch로 사용 가능
     * GET /home/fragments/header
     */
    @GetMapping(value = { "/home/fragments/header", "/home3/fragments/header" }, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String headerFragment(
            @org.springframework.web.bind.annotation.CookieValue(value = "accessToken", required = false) String accessToken) {
        Context ctx = new Context();
        ctx.setVariable("isLoggedIn", accessToken != null);
        String full = templateEngine.process("egovframework/com/fragments/header", ctx);

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
     * GET /home/fragments/footer
     */
    @GetMapping(value = { "/home/fragments/footer", "/home3/fragments/footer" }, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String footerFragment() {
        Context ctx = new Context();
        String full = templateEngine.process("egovframework/com/fragments/footer", ctx);

        int start = full.indexOf("<footer");
        int end = full.lastIndexOf("</body>");
        if (start >= 0 && end > start) {
            return full.substring(start, end).trim();
        }
        return full;
    }

}
