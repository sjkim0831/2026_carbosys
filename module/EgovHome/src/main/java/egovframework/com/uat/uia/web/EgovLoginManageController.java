package egovframework.com.uat.uia.web;

import egovframework.com.uat.uia.service.LoginVO;
import egovframework.com.uat.uia.util.EgovJwtProvider;
import egovframework.com.uss.umt.service.EntrprsManageVO;
import egovframework.com.uss.umt.service.impl.EntrprsManageDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller("uiaEgovLoginManageController")
@RequestMapping({"/signin", "/admin/login"})
@RequiredArgsConstructor
public class EgovLoginManageController {

    private final EgovJwtProvider jwtProvider;
    private final EntrprsManageDAO entrprsManageDAO;

    @GetMapping(value = "/index")
    public String login(LoginVO loginVO, Model model, HttpServletRequest request) {
        return this.loginView(null, loginVO, model, request);
    }

    @RequestMapping(value = "/loginView", method = { RequestMethod.GET, RequestMethod.POST })
    public String loginView(@RequestParam(value = "language", required = false) String language, LoginVO loginVO,
            Model model, HttpServletRequest request) {
        boolean adminLoginRequest = request.getRequestURI().startsWith("/admin/login");
        String accessToken = jwtProvider.getCookie(request, "accessToken");
        if (ObjectUtils.isEmpty(accessToken)) {
            loginVO = new LoginVO();
            model.addAttribute("loginVO", loginVO);
            if ("en".equals(language)) {
                return adminLoginRequest ? "egovframework/com/uat/uia/admin_login_en"
                        : "egovframework/com/uat/uia/login_en";
            }
            return adminLoginRequest ? "egovframework/com/uat/uia/admin_login"
                    : "egovframework/com/uat/uia/login";
        } else {
            return adminLoginRequest ? "redirect:/adminmain/" : "redirect:/home";
        }
    }

    @GetMapping("/authChoice")
    public String authChoice(@RequestParam(value = "language", required = false) String language, Model model) {
        if ("en".equals(language)) {
            return "egovframework/com/uat/uia/auth_choice_en";
        }
        return "egovframework/com/uat/uia/auth_choice";
    }

    @GetMapping("/findId")
    public String findId(@RequestParam(value = "language", required = false) String language, Model model) {
        model.addAttribute("activeTab", "domestic");
        model.addAttribute("language", language);
        if ("en".equals(language)) {
            return "egovframework/com/uat/uia/find_id_en";
        }
        return "egovframework/com/uat/uia/find_id";
    }

    @GetMapping("/findId/overseas")
    public String findIdOverseas(@RequestParam(value = "language", required = false) String language, Model model) {
        model.addAttribute("activeTab", "overseas");
        model.addAttribute("language", language);
        if ("en".equals(language)) {
            return "egovframework/com/uat/uia/find_id_overseas_en";
        }
        return "egovframework/com/uat/uia/find_id_overseas";
    }

    @GetMapping("/findPassword")
    public String findPassword(@RequestParam(value = "language", required = false) String language, Model model) {
        model.addAttribute("activeTab", "domestic");
        model.addAttribute("language", language);
        if ("en".equals(language)) {
            return "egovframework/com/uat/uia/find_password_en";
        }
        return "egovframework/com/uat/uia/find_password";
    }

    @GetMapping("/findPassword/overseas")
    public String findPasswordOverseas(@RequestParam(value = "language", required = false) String language, Model model) {
        model.addAttribute("activeTab", "overseas");
        model.addAttribute("language", language);
        if ("en".equals(language)) {
            return "egovframework/com/uat/uia/find_password_overseas_en";
        }
        return "egovframework/com/uat/uia/find_password_overseas";
    }

    @GetMapping("/findPassword/result")
    public String findPasswordResult(@RequestParam(value = "language", required = false) String language, Model model) {
        model.addAttribute("language", language);
        if ("en".equals(language)) {
            return "egovframework/com/uat/uia/find_password_result_en";
        }
        return "egovframework/com/uat/uia/find_password_result";
    }

    @GetMapping("/findId/result")
    public String findIdResult(@RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "applcntNm", required = false) String applcntNm,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "tab", required = false, defaultValue = "domestic") String tab,
            Model model) {
        model.addAttribute("language", language);
        String normalizedTab = "overseas".equalsIgnoreCase(tab) ? "overseas" : "domestic";
        model.addAttribute("tab", normalizedTab);
        String foundId = null;
        if (!ObjectUtils.isEmpty(applcntNm) && !ObjectUtils.isEmpty(email)) {
            EntrprsManageVO searchVO = new EntrprsManageVO();
            searchVO.setApplcntNm(applcntNm.trim());
            searchVO.setApplcntEmailAdres(email.trim());
            foundId = entrprsManageDAO.selectEntrprsMberIdByNameAndEmail(searchVO);
        }
        model.addAttribute("maskedId", maskUserId(foundId));
        model.addAttribute("found", !ObjectUtils.isEmpty(foundId));

        String passwordResetUrl = "overseas".equals(normalizedTab)
                ? "/signin/findPassword/overseas"
                : "/signin/findPassword";
        if ("en".equals(language)) {
            passwordResetUrl += "?language=en";
        }
        model.addAttribute("passwordResetUrl", passwordResetUrl);

        if ("en".equals(language)) {
            return "egovframework/com/uat/uia/find_id_result_en";
        }
        return "egovframework/com/uat/uia/find_id_result";
    }

    private String maskUserId(String userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return null;
        }
        String id = userId.trim();
        if (id.isEmpty()) {
            return null;
        }
        if (id.length() <= 2) {
            return id.charAt(0) + "*";
        }
        if (id.length() <= 4) {
            return id.substring(0, 1) + "**" + id.substring(id.length() - 1);
        }
        return id.substring(0, 3) + "****" + id.substring(id.length() - 2);
    }

    @RequestMapping(value = "/loginForbidden", method = { RequestMethod.GET, RequestMethod.POST })
    public String loginForbidden(
            @RequestParam(value = "pathCode", required = false, defaultValue = "1") String pathCode, Model model) {
        model.addAttribute("pathCode", pathCode);
        return "egovframework/com/uat/uia/forbidden";
    }

}
