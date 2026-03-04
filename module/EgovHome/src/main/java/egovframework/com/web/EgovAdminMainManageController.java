package egovframework.com.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class EgovAdminMainManageController {

    @RequestMapping(value = { "/", "" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String index() {
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
