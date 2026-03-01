package egovframework.com.uss.umt.web;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import egovframework.com.uss.umt.service.EgovEntrprsManageService;
import egovframework.com.uss.umt.service.EntrprsManageVO;
import egovframework.com.uss.umt.service.InsttInfoVO;
import egovframework.com.uss.umt.service.MberManageVO;

@Controller
@RequestMapping("/join")
public class EgovJoinController {
    private static final String SESSION_JOIN_VO = "joinVO";
    private static final String SESSION_JOIN_STEP = "joinStep";

    @Resource(name = "entrprsManageService")
    private EgovEntrprsManageService entrprsManageService;

    /**
     * 가입 프로세스 초기화 (세션 비우기) 및 홈 이동
     */
    @GetMapping("/reset")
    public String resetJoin(HttpSession session) {
        session.removeAttribute(SESSION_JOIN_VO);
        session.removeAttribute(SESSION_JOIN_STEP);
        return "redirect:/home3";
    }

    /**
     * Step 1: 회원유형 선택 화면
     * ?init=T 파라미터가 있으면 세션을 초기화함 (언어 전환 등에서 사용)
     */
    @GetMapping("/step1")
    public String step1View(@RequestParam(value = "init", required = false) String init, HttpSession session,
            Model model) {
        if ("T".equals(init)) {
            session.removeAttribute(SESSION_JOIN_VO);
            session.removeAttribute(SESSION_JOIN_STEP);
            return "redirect:/join/step1"; // 초기화 후 깨끗한 주소로 리다이렉트
        }

        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null) {
            joinVO = new EntrprsManageVO();
            // 초기 진입 시 기본값 설정 (선택 사항: 원치 않으시면 제거 가능)
            joinVO.setEntrprsSeCode("EMITTER");
            session.setAttribute(SESSION_JOIN_VO, joinVO);
        }
        setJoinStep(session, 1);

        String currType = joinVO.getEntrprsSeCode() != null ? joinVO.getEntrprsSeCode().trim() : "";
        model.addAttribute("joinVO", joinVO);
        model.addAttribute("currType", currType); // 간결한 접근을 위한 속성 추가
        return "uss/umt/step1_join";
    }

    /**
     * Step 1 실시간 저장 API (GET 방식으로 변경하여 호환성 강화)
     */
    @GetMapping("/saveStep1")
    @org.springframework.web.bind.annotation.ResponseBody
    public String saveStep1(@RequestParam("membership_type") String membershipType, HttpSession session) {
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null) {
            joinVO = new EntrprsManageVO();
        }
        if (!hasText(membershipType)) {
            return "invalid_membership_type";
        }
        joinVO.setEntrprsSeCode(membershipType != null ? membershipType.trim() : "");
        joinVO.setUserTy("USR02");
        session.setAttribute(SESSION_JOIN_VO, joinVO);
        setJoinStep(session, 1);
        return "success";
    }

    /**
     * Step 2: 약관 동의 화면
     */
    @PostMapping("/step2")
    public String step2View(@RequestParam(value = "membership_type", required = false) String membershipType,
            HttpSession session, Model model) {
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null) {
            joinVO = new EntrprsManageVO();
        }
        if (membershipType != null) {
            joinVO.setEntrprsSeCode(membershipType);
        }
        if (!hasText(joinVO.getEntrprsSeCode())) {
            return "redirect:/join/step1";
        }
        joinVO.setUserTy("USR02");
        session.setAttribute(SESSION_JOIN_VO, joinVO);
        setJoinStep(session, 2);
        model.addAttribute("joinVO", joinVO);
        return "uss/umt/step2_terms";
    }

    /**
     * Step 2 실시간 저장 API (마케팅 동의 등)
     */
    @GetMapping("/saveStep2")
    @org.springframework.web.bind.annotation.ResponseBody
    public String saveStep2(@RequestParam("marketing_yn") String marketingYn, HttpSession session) {
        if (getJoinStep(session) < 2) {
            return "invalid_step";
        }
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null) {
            joinVO = new EntrprsManageVO();
        }
        joinVO.setMarketingYn(marketingYn);
        session.setAttribute(SESSION_JOIN_VO, joinVO);
        return "success";
    }

    /**
     * Step 3: 본인 인증 화면
     */
    @PostMapping("/step3")
    public String step3View(@RequestParam(value = "marketing_agree", required = false) String marketingAgree,
            HttpSession session, Model model) {
        if (getJoinStep(session) < 2 || session.getAttribute(SESSION_JOIN_VO) == null) {
            return "redirect:/join/step1";
        }
        setJoinStep(session, 3);
        return "uss/umt/step3_auth";
    }

    /**
     * Step 4: 정보 입력 화면
     */
    @PostMapping("/step4")
    public String step4View(@RequestParam(value = "auth_method", required = false) String authMethod,
            HttpSession session, Model model) {
        if (getJoinStep(session) < 3) {
            return "redirect:/join/step1";
        }
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null)
            return "redirect:/join/step1";
        if (!hasText(authMethod)) {
            return "redirect:/join/step3";
        }

        // 본인확인 정보 저장 (Identity verification data)
        joinVO.setAuthTy(authMethod);

        // Mocking auth data based on method
        if ("SIMPLE".equals(authMethod) || "ONEPASS".equals(authMethod)) {
            joinVO.setAuthCi("CI_MOCK_DATA_1234567890");
            joinVO.setAuthDi("DI_MOCK_DATA_0987654321");
            joinVO.setApplcntNm("홍길동");
        } else if ("JOINT".equals(authMethod) || "FINANCIAL".equals(authMethod)) {
            joinVO.setAuthDn("cn=홍길동,ou=User,ou=KICA,o=Government,c=KR");
            joinVO.setApplcntNm("홍길동");
        } else if ("EMAIL".equals(authMethod)) {
            joinVO.setAuthEmail("user@example.com");
            joinVO.setApplcntNm("해외사용자");
        }

        session.setAttribute(SESSION_JOIN_VO, joinVO);
        setJoinStep(session, 4);
        model.addAttribute("joinVO", joinVO);
        model.addAttribute("mberNm", joinVO.getApplcntNm());
        return "uss/umt/step4_info";
    }

    @GetMapping("/step4")
    public String step4View(HttpSession session, Model model) {
        if (getJoinStep(session) < 4) {
            return "redirect:/join/step1";
        }
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO != null) {
            model.addAttribute("joinVO", joinVO);
            model.addAttribute("mberNm", joinVO.getApplcntNm());
        }
        return "uss/umt/step4_info";
    }

    /**
     * 아이디 중복 확인 API
     */
    @GetMapping("/checkId")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> checkId(@RequestParam("mberId") String mberId) throws Exception {
        Map<String, Object> results = new java.util.HashMap<>();
        int cnt = entrprsManageService.checkIdDplct(mberId);

        // 아이디가 이미 존재하면 중복(true), 없으면 사용 가능(false)
        results.put("isDuplicated", cnt > 0);
        return results;
    }

    /**
     * 이메일 중복 확인 API
     */
    @GetMapping("/checkEmail")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> checkEmail(@RequestParam("email") String email) throws Exception {
        Map<String, Object> results = new java.util.HashMap<>();
        int cnt = entrprsManageService.checkEmailDplct(email);

        // 이메일이 이미 존재하면 중복(true), 없으면 사용 가능(false)
        results.put("isDuplicated", cnt > 0);
        return results;
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
            @RequestParam("applcntEmailAdres") String email,
            HttpSession session, Model model) throws Exception {

        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null)
            return "redirect:/join/step1";
        if (getJoinStep(session) < 4 || !hasVerifiedIdentity(joinVO)) {
            return "redirect:/join/step3";
        }
        if (!hasText(mberId) || !hasText(password) || !hasText(mberNm) || !hasText(insttNm) ||
                !hasText(bizrno) || !hasText(tel1) || !hasText(tel2) || !hasText(tel3) || !hasText(email)) {
            return "redirect:/join/step4";
        }

        // Merge data
        joinVO.setEntrprsmberId(mberId);
        joinVO.setEntrprsMberPassword(password);
        joinVO.setApplcntNm(mberNm);
        joinVO.setCmpnyNm(insttNm);
        joinVO.setBizrno(bizrno);
        joinVO.setAreaNo(tel1);
        joinVO.setEntrprsMiddleTelno(tel2);
        joinVO.setEntrprsEndTelno(tel3);
        joinVO.setApplcntEmailAdres(email);
        joinVO.setEntrprsMberSttus("A");

        // Save to DB
        entrprsManageService.insertEntrprsmber(joinVO);

        model.addAttribute("mberId", joinVO.getEntrprsmberId());
        model.addAttribute("mberNm", joinVO.getApplcntNm());
        model.addAttribute("insttNm", joinVO.getCmpnyNm());
        session.removeAttribute(SESSION_JOIN_STEP);
        session.removeAttribute(SESSION_JOIN_VO);

        return "uss/umt/step5_complete";
    }

    // ── English versions ──────────────────────────────────────────

    /** EN Step 1: Member type selection */
    @GetMapping("/en/step1")
    public String step1EnView(@RequestParam(value = "init", required = false) String init, HttpSession session,
            Model model) {
        if ("T".equals(init)) {
            session.removeAttribute(SESSION_JOIN_VO);
            session.removeAttribute(SESSION_JOIN_STEP);
            return "redirect:/join/en/step1"; // 초기화 후 깨끗한 주소로 리다이렉트
        }

        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null) {
            joinVO = new EntrprsManageVO();
            joinVO.setEntrprsSeCode("EMITTER");
            session.setAttribute(SESSION_JOIN_VO, joinVO);
        }
        setJoinStep(session, 1);
        String currType = joinVO.getEntrprsSeCode() != null ? joinVO.getEntrprsSeCode().trim() : "";
        model.addAttribute("joinVO", joinVO);
        model.addAttribute("currType", currType);
        return "uss/umt/step1_join_en";
    }

    /** EN Step 2: Terms (form submit from step1 EN) */
    @PostMapping("/en/step2")
    public String step2EnProcess(@RequestParam(value = "membership_type", required = false) String membershipType,
            HttpSession session, Model model) {
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null) {
            joinVO = new EntrprsManageVO();
        }
        if (membershipType != null) {
            joinVO.setEntrprsSeCode(membershipType);
        }
        if (!hasText(joinVO.getEntrprsSeCode())) {
            return "redirect:/join/en/step1";
        }
        joinVO.setUserTy("USR02");
        session.setAttribute(SESSION_JOIN_VO, joinVO);
        setJoinStep(session, 2);
        model.addAttribute("joinVO", joinVO);
        return "uss/umt/step2_terms_en";
    }

    @GetMapping("/en/step2")
    public String step2EnView(HttpSession session) {
        if (getJoinStep(session) < 2 || session.getAttribute(SESSION_JOIN_VO) == null) {
            return "redirect:/join/en/step1";
        }
        return "uss/umt/step2_terms_en";
    }

    /** EN Step 3: Verification (form submit from step2 EN) */
    @PostMapping("/en/step3")
    public String step3EnProcess(@RequestParam(value = "marketing_agree", required = false) String marketingAgree,
            HttpSession session) {
        if (getJoinStep(session) < 2 || session.getAttribute(SESSION_JOIN_VO) == null) {
            return "redirect:/join/en/step1";
        }
        setJoinStep(session, 3);
        return "uss/umt/step3_auth_en";
    }

    @GetMapping("/en/step3")
    public String step3EnView(HttpSession session) {
        if (getJoinStep(session) < 3 || session.getAttribute(SESSION_JOIN_VO) == null) {
            return "redirect:/join/en/step1";
        }
        return "uss/umt/step3_auth_en";
    }

    /** EN Step 4: Info form (form submit from step3 EN) */
    @PostMapping("/en/step4")
    public String step4EnProcess(@RequestParam(value = "auth_method", required = false) String authMethod,
            HttpSession session, Model model) {
        if (getJoinStep(session) < 3) {
            return "redirect:/join/en/step1";
        }
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null)
            return "redirect:/join/en/step1";
        if (!hasText(authMethod)) {
            return "redirect:/join/en/step3";
        }

        joinVO.setAuthTy(authMethod);

        // Mocking auth data for English users
        if ("SIMPLE".equals(authMethod) || "ONEPASS".equals(authMethod)) {
            joinVO.setAuthCi("CI_EN_MOCK_777");
            joinVO.setAuthDi("DI_EN_MOCK_777");
            joinVO.setApplcntNm("John Doe");
        } else if ("CERT".equals(authMethod) || "JOINT".equals(authMethod) || "FINANCIAL".equals(authMethod)) {
            joinVO.setAuthDn("cn=John Doe");
            joinVO.setApplcntNm("John Doe");
        } else if ("EMAIL".equals(authMethod)) {
            joinVO.setAuthEmail("test@example.com");
            joinVO.setApplcntNm("Global User");
        }

        session.setAttribute(SESSION_JOIN_VO, joinVO);
        setJoinStep(session, 4);
        model.addAttribute("joinVO", joinVO);
        model.addAttribute("mberNm", joinVO.getApplcntNm());
        return "uss/umt/step4_info_en";
    }

    @GetMapping("/en/step4")
    public String step4EnView(HttpSession session, Model model) {
        if (getJoinStep(session) < 4) {
            return "redirect:/join/en/step1";
        }
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO != null) {
            model.addAttribute("joinVO", joinVO);
            model.addAttribute("mberNm", joinVO.getApplcntNm());
        }
        return "uss/umt/step4_info_en";
    }

    /** EN Step 5: Complete (form submit from step4 EN) */
    @PostMapping("/en/step5")
    public String step5EnProcess(@RequestParam("mberId") String mberId,
            @RequestParam("password") String password,
            @RequestParam("mberNm") String mberNm,
            @RequestParam("insttNm") String insttNm,
            @RequestParam("bizrno") String bizrno,
            @RequestParam(value = "deptNm", required = false) String deptNm,
            @RequestParam("moblphonNo1") String tel1,
            @RequestParam("moblphonNo2") String tel2,
            @RequestParam("moblphonNo3") String tel3,
            @RequestParam("applcntEmailAdres") String email,
            HttpSession session, Model model) throws Exception {

        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO == null)
            return "redirect:/join/en/step1";
        if (getJoinStep(session) < 4 || !hasVerifiedIdentity(joinVO)) {
            return "redirect:/join/en/step3";
        }
        if (!hasText(mberId) || !hasText(password) || !hasText(mberNm) || !hasText(insttNm) ||
                !hasText(bizrno) || !hasText(tel1) || !hasText(tel2) || !hasText(tel3) || !hasText(email)) {
            return "redirect:/join/en/step4";
        }

        joinVO.setEntrprsmberId(mberId);
        joinVO.setEntrprsMberPassword(password);
        joinVO.setApplcntNm(mberNm);
        joinVO.setCmpnyNm(insttNm);
        joinVO.setBizrno(bizrno);
        joinVO.setAreaNo(tel1);
        joinVO.setEntrprsMiddleTelno(tel2);
        joinVO.setEntrprsEndTelno(tel3);
        joinVO.setApplcntEmailAdres(email);
        joinVO.setEntrprsMberSttus("A");

        entrprsManageService.insertEntrprsmber(joinVO);

        model.addAttribute("mberId", joinVO.getEntrprsmberId());
        model.addAttribute("mberNm", joinVO.getApplcntNm());
        model.addAttribute("insttNm", joinVO.getCmpnyNm());
        session.removeAttribute(SESSION_JOIN_STEP);
        session.removeAttribute(SESSION_JOIN_VO);

        return "uss/umt/step5_complete_en";
    }

    @GetMapping("/en/step5")
    public String step5EnView(HttpSession session, Model model) {
        EntrprsManageVO joinVO = (EntrprsManageVO) session.getAttribute(SESSION_JOIN_VO);
        if (joinVO != null) {
            model.addAttribute("mberId", joinVO.getEntrprsmberId());
            model.addAttribute("mberNm", joinVO.getApplcntNm());
            model.addAttribute("insttNm", joinVO.getCmpnyNm());
        }
        return "uss/umt/step5_complete_en";
    }

    // ==========================================
    // 신규 회원사(기업/기관) 등록 및 모달 검색 API
    // ==========================================

    @GetMapping("/companyRegister")
    public String companyRegisterView() {
        return "uss/umt/step4_company_register";
    }

    @GetMapping("/en/companyRegister")
    public String companyRegisterViewEn() {
        return "uss/umt/step4_company_register_en";
    }

    @PostMapping("/companyRegisterSubmit")
    public String companyRegisterSubmit(
            @RequestParam("agencyName") String agencyName,
            @RequestParam("representativeName") String repName,
            @RequestParam("bizRegistrationNumber") String bizNo,
            @RequestParam("zipCode") String zipCode,
            @RequestParam("companyAddress") String addr,
            @RequestParam(value = "companyAddressDetail", required = false) String detailAddr,
            @RequestParam(value = "chargerName", required = false) String chargerNm,
            @RequestParam(value = "chargerEmail", required = false) String chargerEmail,
            @RequestParam(value = "chargerTel", required = false) String chargerTel,
            @RequestParam(value = "lang", defaultValue = "ko") String lang,
            @RequestParam("fileUploads") java.util.List<org.springframework.web.multipart.MultipartFile> fileUploads,
            org.springframework.ui.Model model) {

        try {
            InsttInfoVO vo = new InsttInfoVO();
            String tempId = "INSTT_" + System.currentTimeMillis();
            if (tempId.length() > 20)
                tempId = tempId.substring(0, 20);

            vo.setInsttId(tempId);
            vo.setInsttNm(agencyName);
            vo.setReprsntNm(repName);
            vo.setBizrno(bizNo);
            vo.setZip(zipCode);
            vo.setAdres(addr);
            vo.setDetailAdres(detailAddr);
            vo.setChargerNm(chargerNm);
            vo.setChargerEmail(chargerEmail);
            vo.setChargerTel(chargerTel);
            vo.setInsttSttus("P");

            // Upload directory
            String uploadDir = "/opt/carbosys/file/instt";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new Exception("Cannot create upload directory: " + uploadDir);
            }

            java.util.List<String> savedPaths = new java.util.ArrayList<>();
            if (fileUploads != null && !fileUploads.isEmpty()) {
                for (int i = 0; i < fileUploads.size(); i++) {
                    org.springframework.web.multipart.MultipartFile file = fileUploads.get(i);
                    if (file == null || file.isEmpty())
                        continue;

                    String originalFileName = file.getOriginalFilename();
                    String ext = "";
                    if (originalFileName != null) {
                        int lastDotIndex = originalFileName.lastIndexOf(".");
                        if (lastDotIndex > -1) {
                            ext = originalFileName.substring(lastDotIndex);
                        }
                    }

                    String newFileName = tempId + (fileUploads.size() > 1 ? "_" + i : "") + ext;
                    java.io.File targetFile = new java.io.File(dir, newFileName);
                    file.transferTo(targetFile);
                    savedPaths.add(targetFile.getAbsolutePath());
                }
            }

            if (!savedPaths.isEmpty()) {
                vo.setBizRegFilePath(String.join(",", savedPaths));
            }

            entrprsManageService.insertInsttInfo(vo);

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy.MM.dd HH:mm:ss");
            String regDate = now.format(formatter);

            model.addAttribute("insttNm", agencyName);
            model.addAttribute("bizrno", bizNo);
            model.addAttribute("regDate", regDate);

            if ("en".equals(lang)) {
                return "uss/umt/step4_company_complete_en";
            }
            return "uss/umt/step4_company_complete";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "uss/umt/step4_company_register" + ("en".equals(lang) ? "_en" : "");
        }
    }

    @GetMapping("/checkCompanyNameDplct")
    @org.springframework.web.bind.annotation.ResponseBody
    public String checkCompanyNameDplct(@RequestParam("agencyName") String agencyName) throws Exception {
        int count = entrprsManageService.checkCompanyNameDplct(agencyName);
        return String.valueOf(count);
    }

    @GetMapping("/searchCompany")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> searchCompany(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "status", required = false, defaultValue = "") String status) throws Exception {

        int offset = (page - 1) * size;
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("keyword", keyword.trim());
        params.put("offset", offset);
        params.put("pageSize", size);
        params.put("status", status.trim());

        java.util.List<?> list = entrprsManageService.searchCompanyListPaged(params);
        int totalCnt = entrprsManageService.searchCompanyListTotCnt(params);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", list);
        result.put("totalCnt", totalCnt);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) totalCnt / size));
        return result;
    }

    @GetMapping("/searchCompanyAPI")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<?> searchCompanyAPI(@RequestParam("keyword") String keyword) throws Exception {
        return entrprsManageService.searchCompanyList(keyword);
    }

    @GetMapping("/companyJoinStatusSearch")
    public String companyJoinStatusSearch() {
        return "uss/umt/company_join_status_search";
    }

    @GetMapping("/companyJoinStatusDetail")
    public String companyJoinStatusDetail(
            @RequestParam(value = "bizNo", required = false) String bizNo,
            @RequestParam(value = "appNo", required = false) String appNo,
            @RequestParam("repName") String repName,
            org.springframework.ui.Model model) throws Exception {

        InsttInfoVO searchVO = new InsttInfoVO();
        searchVO.setReprsntNm(repName);
        searchVO.setBizrno(bizNo);
        searchVO.setInsttId(appNo);

        java.util.Map<String, Object> result = entrprsManageService.selectInsttInfoForStatus(searchVO);

        if (result == null || result.isEmpty()) {
            model.addAttribute("errorMessage", "입력하신 정보와 일치하는 신청 내역이 없습니다.");
            return "uss/umt/company_join_status_search";
        }

        model.addAttribute("result", result);
        return "uss/umt/company_join_status_detail";
    }

    @GetMapping("/companyReapply")
    public String companyReapply(
            @RequestParam("bizNo") String bizNo,
            @RequestParam("repName") String repName,
            org.springframework.ui.Model model) throws Exception {

        InsttInfoVO searchVO = new InsttInfoVO();
        searchVO.setReprsntNm(repName);
        searchVO.setBizrno(bizNo);

        java.util.Map<String, Object> result = entrprsManageService.selectInsttInfoForStatus(searchVO);

        if (result == null || result.isEmpty()) {
            model.addAttribute("errorMessage", "입력하신 정보와 일치하는 신청 내역이 없습니다.");
            return "uss/umt/company_join_status_search";
        }

        if (!"X".equals(result.get("insttSttus"))) {
            model.addAttribute("errorMessage", "반려된 건만 재신청이 가능합니다.");
            return "redirect:/join/companyJoinStatusDetail?bizNo=" + bizNo + "&repName=" + repName;
        }

        model.addAttribute("result", result);
        return "uss/umt/company_join_reapply";
    }

    @PostMapping("/companyReapplySubmit")
    public String companyReapplySubmit(
            @RequestParam("insttId") String insttId,
            @RequestParam("agencyName") String agencyName,
            @RequestParam("representativeName") String repName,
            @RequestParam("bizRegistrationNumber") String bizNo,
            @RequestParam("zipCode") String zipCode,
            @RequestParam("companyAddress") String addr,
            @RequestParam(value = "companyAddressDetail", required = false) String detailAddr,
            @RequestParam(value = "chargerName", required = false) String chargerNm,
            @RequestParam(value = "chargerEmail", required = false) String chargerEmail,
            @RequestParam(value = "chargerTel", required = false) String chargerTel,
            @RequestParam(value = "fileUploads", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> fileUploads,
            org.springframework.ui.Model model) {

        try {
            String normalizedInsttId = insttId == null ? "" : insttId.trim();

            InsttInfoVO searchVO = new InsttInfoVO();
            searchVO.setInsttId(normalizedInsttId);
            searchVO.setReprsntNm(repName);
            searchVO.setBizrno(bizNo);
            java.util.Map<String, Object> current = entrprsManageService.selectInsttInfoForStatus(searchVO);
            if (current == null || current.isEmpty()) {
                model.addAttribute("errorMessage", "재신청 대상 정보를 찾을 수 없습니다.");
                return "redirect:/join/companyJoinStatusSearch";
            }

            String insttSttus = String.valueOf(current.get("insttSttus"));
            if (!"X".equals(insttSttus)) {
                model.addAttribute("errorMessage", "반려된 건만 재신청이 가능합니다.");
                return "redirect:/join/companyJoinStatusDetail?bizNo=" + bizNo + "&repName=" + repName;
            }

            InsttInfoVO vo = new InsttInfoVO();
            vo.setInsttId(normalizedInsttId);
            vo.setInsttNm(agencyName);
            vo.setReprsntNm(repName);
            vo.setBizrno(bizNo);
            vo.setZip(zipCode);
            vo.setAdres(addr);
            vo.setDetailAdres(detailAddr);
            vo.setChargerNm(chargerNm);
            vo.setChargerEmail(chargerEmail);
            vo.setChargerTel(chargerTel);
            vo.setInsttSttus("P");

            // Handle file uploads
            String uploadDir = "/opt/carbosys/file/instt";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new Exception("Cannot create upload directory: " + uploadDir);
            }

            java.util.List<String> savedPaths = new java.util.ArrayList<>();
            if (fileUploads != null && !fileUploads.isEmpty()) {
                for (int i = 0; i < fileUploads.size(); i++) {
                    org.springframework.web.multipart.MultipartFile file = fileUploads.get(i);
                    if (file == null || file.isEmpty())
                        continue;

                    String originalFileName = file.getOriginalFilename();
                    String ext = "";
                    if (originalFileName != null) {
                        int lastDotIndex = originalFileName.lastIndexOf(".");
                        if (lastDotIndex > -1)
                            ext = originalFileName.substring(lastDotIndex);
                    }
                    String newFileName = normalizedInsttId + "_" + System.currentTimeMillis() + "_" + i + ext;
                    java.io.File targetFile = new java.io.File(uploadDir, newFileName);
                    file.transferTo(targetFile);
                    savedPaths.add(targetFile.getAbsolutePath());
                }
            }

            if (!savedPaths.isEmpty()) {
                vo.setBizRegFilePath(String.join(",", savedPaths));
            } else {
                Object existingPath = current.get("bizRegFilePath");
                vo.setBizRegFilePath(existingPath == null ? null : String.valueOf(existingPath));
            }

            entrprsManageService.updateInsttInfo(vo);

            model.addAttribute("insttNm", agencyName);
            model.addAttribute("bizrno", bizNo);
            model.addAttribute("regDate", java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));

            return "uss/umt/step4_company_complete";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "uss/umt/company_join_reapply";
        }
    }

    @GetMapping("/downloadInsttFile")
    public void downloadInsttFile(@RequestParam("filePath") String filePath,
            javax.servlet.http.HttpServletResponse response) throws Exception {
        java.io.File file = new java.io.File(filePath);
        // Security check: only allow files within the instt directory
        if (!file.exists() || !file.getCanonicalPath().startsWith("/opt/carbosys/file/instt")) {
            response.sendError(404, "File not found or access denied.");
            return;
        }

        String fileName = file.getName();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8") + "\"");

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                java.io.OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    private void setJoinStep(HttpSession session, int step) {
        session.setAttribute(SESSION_JOIN_STEP, step);
    }

    private int getJoinStep(HttpSession session) {
        Object stepObj = session.getAttribute(SESSION_JOIN_STEP);
        if (stepObj instanceof Integer) {
            return (Integer) stepObj;
        }
        if (stepObj instanceof String) {
            try {
                return Integer.parseInt((String) stepObj);
            } catch (NumberFormatException ignore) {
                return 0;
            }
        }
        return 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasVerifiedIdentity(EntrprsManageVO joinVO) {
        return hasText(joinVO.getAuthTy()) &&
                (hasText(joinVO.getAuthCi()) || hasText(joinVO.getAuthDi()) || hasText(joinVO.getAuthDn())
                        || hasText(joinVO.getAuthEmail()));
    }
}
