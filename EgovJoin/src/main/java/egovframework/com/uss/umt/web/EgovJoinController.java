package egovframework.com.uss.umt.web;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.com.uss.umt.service.EgovEntrprsManageService;
import egovframework.com.uss.umt.service.EntrprsManageVO;

@Controller
@RequestMapping("/join")
public class EgovJoinController {

    @Resource(name = "entrprsManageService")
    private EgovEntrprsManageService entrprsManageService;

    /**
     * Step 1: 회원유형 선택 화면
     */
    @GetMapping("/step1")
    public String step1View(HttpSession session, Model model) {
        session.removeAttribute("joinVO");
        return "uss/umt/step1_join";
    }

    /**
     * Step 2: 약관 동의 화면
     */
    @PostMapping("/step2")
    public String step2View(@RequestParam(value = "membership_type", required = false) String membershipType,
            HttpSession session, Model model) {
        EntrprsManageVO joinVO = new EntrprsManageVO();
        joinVO.setUserTy(membershipType);
        session.setAttribute("joinVO", joinVO);
        return "uss/umt/step2_terms";
    }

    /**
     * Step 3: 본인 인증 화면
     */
    @PostMapping("/step3")
    public String step3View(@RequestParam(value = "marketing_agree", required = false) String marketingAgree,
            HttpSession session, Model model) {
        // marketingAgree will be "on" if checked
        return "uss/umt/step3_auth";
    }

    /**
     * Step 4: 정보 입력 화면
     */
    @PostMapping("/step4")
    public String step4View(@RequestParam(value = "auth_method", required = false) String authMethod,
            HttpSession session, Model model) {
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute("joinVO");
        if (joinVO == null)
            return "redirect:/join/step1";

        // Mocking auth data
        joinVO.setApplcntNm("홍길동");
        session.setAttribute("joinVO", joinVO);

        model.addAttribute("mberNm", joinVO.getApplcntNm());
        return "uss/umt/step4_info";
    }

    /**
     * Step 5: 가입 완료 처리
     */
    @PostMapping("/step5")
    public String step5Process(@RequestParam("mberId") String mberId,
            @RequestParam("password") String password,
            @RequestParam("mberNm") String mberNm,
            @RequestParam("insttNm") String insttNm,
            @RequestParam("bizrno") String bizrno,
            @RequestParam(value = "deptNm", required = false) String deptNm,
            @RequestParam("moblphonNo1") String tel1,
            @RequestParam("moblphonNo2") String tel2,
            @RequestParam("moblphonNo3") String tel3,
            HttpSession session, Model model) throws Exception {

        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute("joinVO");
        if (joinVO == null)
            return "redirect:/join/step1";

        // Merge data
        joinVO.setEntrprsmberId(mberId);
        joinVO.setEntrprsMberPassword(password);
        joinVO.setApplcntNm(mberNm);
        joinVO.setCmpnyNm(insttNm);
        joinVO.setBizrno(bizrno);
        joinVO.setAreaNo(tel1);
        joinVO.setEntrprsMiddleTelno(tel2);
        joinVO.setEntrprsEndTelno(tel3);
        joinVO.setEntrprsMberSttus("A");

        // Save to DB
        entrprsManageService.insertEntrprsmber(joinVO);

        model.addAttribute("mberId", joinVO.getEntrprsmberId());
        model.addAttribute("mberNm", joinVO.getApplcntNm());
        model.addAttribute("insttNm", joinVO.getCmpnyNm());

        return "uss/umt/step5_complete";
    }
}