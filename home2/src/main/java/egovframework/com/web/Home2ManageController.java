package egovframework.com.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Locale;

@Controller
public class Home2ManageController {

    @RequestMapping(value = "/home2", method = { RequestMethod.GET, RequestMethod.POST })
    public String index(Locale locale, org.springframework.ui.Model model,
            @org.springframework.web.bind.annotation.CookieValue(value = "accessToken", required = false) String accessToken) {
        model.addAttribute("isLoggedIn", accessToken != null);
        return "home";
    }

    @RequestMapping(value = "/home2/en", method = { RequestMethod.GET, RequestMethod.POST })
    public String indexEn(Locale locale, org.springframework.ui.Model model,
            @org.springframework.web.bind.annotation.CookieValue(value = "accessToken", required = false) String accessToken) {
        model.addAttribute("isLoggedIn", accessToken != null);
        return "home_en";
    }

}
