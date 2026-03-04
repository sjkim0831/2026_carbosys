package egovframework.com.web;

import egovframework.com.uat.uia.util.EgovJwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/adminmain")
@RequiredArgsConstructor
public class EgovAdminMainManageController {

    private final EgovJwtProvider jwtProvider;

    @RequestMapping(value = { "", "/" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String adminMainEntry(HttpServletRequest request) {
        String accessToken = jwtProvider.getCookie(request, "accessToken");
        if (ObjectUtils.isEmpty(accessToken)) {
            return "redirect:/admin/login/loginView";
        }
        return "egovframework/com/adminmain/index";
    }

    @RequestMapping(value = "/en", method = { RequestMethod.GET, RequestMethod.POST })
    public String indexEn() {
        return "egovframework/com/adminmain/index_en";
    }

    @RequestMapping(value = "/member/stats", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberStats() {
        return "egovframework/com/adminmain/memberStats";
    }

    @RequestMapping(value = { "/member/admin-account", "/admin/account" }, method = {
            RequestMethod.GET,
            RequestMethod.POST
    })
    public String adminAccount() {
        return "egovframework/com/adminmain/adminAccount";
    }

    @RequestMapping(value = "/member/list", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberList() {
        return "egovframework/com/adminmain/memberList";
    }

    @RequestMapping(value = { "/member/auth-group", "/auth/group" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String authGroup() {
        return "egovframework/com/adminmain/authGroup";
    }
}
