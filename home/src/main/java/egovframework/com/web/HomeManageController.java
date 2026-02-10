package egovframework.com.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.ui.Model;
import java.util.Locale;

@Controller
public class HomeManageController {

    @RequestMapping(value="/home")
    public String index(Locale locale, Model model) {
        if ("en".equals(locale.getLanguage())) {
            return "home_en";
        }
        return "home";
    }

}
