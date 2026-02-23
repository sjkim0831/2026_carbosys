package egovframework.com.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class EgovAdminMainManageController {

    @RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/member/stats", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberStats() {
        return "memberStats";
    }

    @RequestMapping(value = "/member/admin-account", method = { RequestMethod.GET, RequestMethod.POST })
    public String adminAccount() {
        return "adminAccount";
    }

}
