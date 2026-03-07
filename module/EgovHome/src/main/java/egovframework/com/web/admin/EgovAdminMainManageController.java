package egovframework.com.web.admin;

import egovframework.com.uss.umt.service.EgovEntrprsManageService;
import egovframework.com.uss.umt.service.EntrprsMberFileVO;
import egovframework.com.uss.umt.service.EntrprsManageVO;
import egovframework.com.uss.umt.service.InsttInfoVO;
import egovframework.com.uat.uia.util.EgovJwtProvider;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.ui.Model;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EgovAdminMainManageController {

    private final EgovJwtProvider jwtProvider;
    private final EgovEntrprsManageService entrprsManageService;

    @RequestMapping(value = { "", "/" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String adminMainEntry(HttpServletRequest request, Locale locale) {
        String accessToken = jwtProvider.getCookie(request, "accessToken");
        if (ObjectUtils.isEmpty(accessToken)) {
            return "redirect:/admin/login/loginView";
        }
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return "egovframework/com/admin/index_en";
        }
        return "egovframework/com/admin/index";
    }

    @RequestMapping(value = "/system/infra", method = { RequestMethod.GET, RequestMethod.POST })
    public String systemInfra(Locale locale) {
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return "egovframework/com/admin/index_en";
        }
        return "egovframework/com/admin/index";
    }

    @RequestMapping(value = "/member/stats", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberStats() {
        return "egovframework/com/admin/memberStats";
    }

    @RequestMapping(value = "/member/register", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberRegister(Locale locale) {
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return "egovframework/com/admin/memberRegister_en";
        }
        return "egovframework/com/admin/memberRegister";
    }

    @RequestMapping(value = "/member/approve", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberApprove(Locale locale) {
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return "egovframework/com/admin/memberApprove_en";
        }
        return "egovframework/com/admin/memberApprove";
    }

    @RequestMapping(value = "/member/company-approve", method = { RequestMethod.GET, RequestMethod.POST })
    public String companyMemberApprove(Locale locale) {
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return "egovframework/com/admin/memberApprove_en";
        }
        return "egovframework/com/admin/memberApprove";
    }

    @RequestMapping(value = "/member/edit", method = RequestMethod.GET)
    public String memberEdit(
            @RequestParam(value = "memberId", required = false) String memberId,
            @RequestParam(value = "updated", required = false) String updated,
            Locale locale,
            Model model) {
        boolean isEn = locale != null && "en".equalsIgnoreCase(locale.getLanguage());
        String viewName = isEn ? "egovframework/com/admin/memberEdit_en" : "egovframework/com/admin/memberEdit";
        String normalizedMemberId = safeString(memberId);
        model.addAttribute("memberId", normalizedMemberId);
        model.addAttribute("memberEditUpdated", "true".equalsIgnoreCase(safeString(updated)));

        if (normalizedMemberId.isEmpty()) {
            model.addAttribute("memberEditError", isEn ? "Member ID was not provided." : "회원 ID가 전달되지 않았습니다.");
            return viewName;
        }

        try {
            EntrprsManageVO member = entrprsManageService.selectEntrprsmberByMberId(normalizedMemberId);
            if (member == null || safeString(member.getEntrprsmberId()).isEmpty()) {
                model.addAttribute("memberEditError", isEn ? "Member information was not found." : "회원 정보를 찾을 수 없습니다.");
                return viewName;
            }
            populateMemberEditModel(model, member, isEn);
        } catch (Exception e) {
            model.addAttribute("memberEditError", isEn ? "An error occurred while retrieving member information." : "회원 정보 조회 중 오류가 발생했습니다.");
        }
        return viewName;
    }

    @RequestMapping(value = "/member/edit", method = RequestMethod.POST)
    public String memberEditSubmit(
            @RequestParam(value = "memberId", required = false) String memberId,
            @RequestParam(value = "applcntNm", required = false) String applcntNm,
            @RequestParam(value = "applcntEmailAdres", required = false) String applcntEmailAdres,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "entrprsSeCode", required = false) String entrprsSeCode,
            @RequestParam(value = "entrprsMberSttus", required = false) String entrprsMberSttus,
            @RequestParam(value = "zip", required = false) String zip,
            @RequestParam(value = "adres", required = false) String adres,
            @RequestParam(value = "detailAdres", required = false) String detailAdres,
            @RequestParam(value = "marketingYn", required = false) String marketingYn,
            Locale locale,
            Model model) {
        boolean isEn = locale != null && "en".equalsIgnoreCase(locale.getLanguage());
        String viewName = isEn ? "egovframework/com/admin/memberEdit_en" : "egovframework/com/admin/memberEdit";
        String normalizedMemberId = safeString(memberId);
        model.addAttribute("memberId", normalizedMemberId);

        if (normalizedMemberId.isEmpty()) {
            model.addAttribute("memberEditError", isEn ? "Member ID was not provided." : "회원 ID가 전달되지 않았습니다.");
            return viewName;
        }

        EntrprsManageVO member;
        try {
            member = entrprsManageService.selectEntrprsmberByMberId(normalizedMemberId);
        } catch (Exception e) {
            model.addAttribute("memberEditError", isEn ? "An error occurred while retrieving member information." : "회원 정보 조회 중 오류가 발생했습니다.");
            return viewName;
        }

        if (member == null || safeString(member.getEntrprsmberId()).isEmpty()) {
            model.addAttribute("memberEditError", isEn ? "Member information was not found." : "회원 정보를 찾을 수 없습니다.");
            return viewName;
        }

        List<String> errors = new ArrayList<>();
        String normalizedApplicantName = safeString(applcntNm);
        String normalizedEmail = safeString(applcntEmailAdres);
        String normalizedZip = digitsOnly(zip);
        String normalizedAddress = safeString(adres);
        String normalizedDetailAddress = safeString(detailAdres);
        String normalizedType = normalizeMembershipCode(safeString(entrprsSeCode).toUpperCase());
        String normalizedStatus = normalizeMemberStatusCode(entrprsMberSttus);
        String normalizedMarketingYn = "Y".equalsIgnoreCase(safeString(marketingYn)) ? "Y" : "N";
        String[] phoneParts = splitPhoneNumber(phoneNumber);

        if (normalizedApplicantName.isEmpty()) {
            errors.add(isEn ? "Please enter the member name." : "회원명을 입력해 주세요.");
        }
        if (!isValidEmail(normalizedEmail)) {
            errors.add(isEn ? "Please enter a valid email address." : "올바른 이메일 주소를 입력해 주세요.");
        }
        if (phoneParts == null) {
            errors.add(isEn ? "Please enter a valid phone number." : "연락처 형식이 올바르지 않습니다.");
        }
        if (normalizedType.isEmpty()) {
            errors.add(isEn ? "Please select a valid member type." : "유효한 회원 유형을 선택해 주세요.");
        }
        if (normalizedStatus.isEmpty()) {
            errors.add(isEn ? "Please select a valid member status." : "유효한 회원 상태를 선택해 주세요.");
        }

        member.setApplcntNm(normalizedApplicantName);
        member.setApplcntEmailAdres(normalizedEmail);
        if (phoneParts != null) {
            member.setAreaNo(phoneParts[0]);
            member.setEntrprsMiddleTelno(phoneParts[1]);
            member.setEntrprsEndTelno(phoneParts[2]);
        }
        member.setEntrprsSeCode(normalizedType);
        member.setEntrprsMberSttus(normalizedStatus);
        member.setZip(normalizedZip);
        member.setAdres(normalizedAddress);
        member.setDetailAdres(normalizedDetailAddress);
        member.setMarketingYn(normalizedMarketingYn);

        if (!errors.isEmpty()) {
            populateMemberEditModel(model, member, isEn);
            model.addAttribute("memberEditErrors", errors);
            return viewName;
        }

        try {
            entrprsManageService.updateEntrprsmber(member);
            String redirectUrl = "redirect:/admin/member/edit?memberId=" + urlEncode(normalizedMemberId) + "&updated=true";
            if (isEn) {
                redirectUrl += "&language=en";
            }
            return redirectUrl;
        } catch (Exception e) {
            populateMemberEditModel(model, member, isEn);
            model.addAttribute("memberEditError", isEn ? "An error occurred while saving member information." : "회원 정보 저장 중 오류가 발생했습니다.");
            return viewName;
        }
    }

    @RequestMapping(value = "/member/file", method = RequestMethod.GET)
    public void memberFile(
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "download", required = false) String download,
            HttpServletResponse response) throws Exception {
        File file = new File(safeString(filePath));
        String canonicalPath = file.getCanonicalPath();
        if (!file.exists()
                || (!canonicalPath.startsWith("/opt/carbosys/file/")
                && !canonicalPath.startsWith("/srv/file/carbosys/"))) {
            response.sendError(404, "File not found or access denied.");
            return;
        }

        boolean forceDownload = "true".equalsIgnoreCase(safeString(download));
        String fileName = file.getName();
        response.setContentType(resolveMediaType(fileName));
        response.setHeader("Content-Disposition",
                (forceDownload ? "attachment" : "inline") + "; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");

        try (FileInputStream fis = new FileInputStream(file); OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    @RequestMapping(value = "/member/detail", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberDetail(
            @RequestParam(value = "memberId", required = false) String memberId,
            Locale locale,
            Model model) {
        boolean isEn = locale != null && "en".equalsIgnoreCase(locale.getLanguage());
        String viewName = isEn ? "egovframework/com/admin/memberDetail_en" : "egovframework/com/admin/memberDetail";
        String normalizedMemberId = memberId == null ? "" : memberId.trim();
        model.addAttribute("memberId", normalizedMemberId);

        if (normalizedMemberId.isEmpty()) {
            model.addAttribute("memberDetailError", isEn ? "Member ID was not provided." : "회원 ID가 전달되지 않았습니다.");
            return viewName;
        }

        try {
            EntrprsManageVO member = entrprsManageService.selectEntrprsmberByMberId(normalizedMemberId);
            if (member == null || ObjectUtils.isEmpty(member.getEntrprsmberId())) {
                model.addAttribute("memberDetailError", isEn ? "Member information was not found." : "회원 정보를 찾을 수 없습니다.");
                return viewName;
            }

            model.addAttribute("member", member);
            model.addAttribute("membershipTypeLabel", resolveMembershipTypeLabel(member.getEntrprsSeCode()));
            model.addAttribute("statusLabel", resolveStatusLabel(member.getEntrprsMberSttus()));
            model.addAttribute("statusBadgeClass", resolveStatusBadgeClass(member.getEntrprsMberSttus()));
            model.addAttribute("phoneNumber", formatPhoneNumber(member.getAreaNo(), member.getEntrprsMiddleTelno(), member.getEntrprsEndTelno()));
        } catch (Exception e) {
            model.addAttribute("memberDetailError", isEn ? "An error occurred while retrieving member information." : "회원 정보 조회 중 오류가 발생했습니다.");
        }

        return viewName;
    }

    @RequestMapping(value = { "/member/admin-account", "/admin/account" }, method = {
            RequestMethod.GET,
            RequestMethod.POST
    })
    public String adminAccount() {
        return "egovframework/com/admin/adminAccount";
    }

    @RequestMapping(value = { "/member/list", "/member/admin-list" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String memberList(
            @RequestParam(value = "pageIndex", required = false) String pageIndexParam,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "membershipType", required = false) String membershipType,
            @RequestParam(value = "sbscrbSttus", required = false) String sbscrbSttus,
            Locale locale,
            Model model) {
        String viewName = "egovframework/com/admin/memberList";
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            viewName = "egovframework/com/admin/memberList_en";
        }
        return populateMemberList(pageIndexParam, searchKeyword, membershipType, sbscrbSttus, model,
                viewName);
    }

    private String populateMemberList(
            String pageIndexParam,
            String searchKeyword,
            String membershipType,
            String sbscrbSttus,
            Model model,
            String viewName) {
        int pageIndex = 1;
        if (pageIndexParam != null && !pageIndexParam.trim().isEmpty()) {
            try {
                pageIndex = Integer.parseInt(pageIndexParam.trim());
            } catch (NumberFormatException ignored) {
                pageIndex = 1;
            }
        }
        int currentPage = Math.max(pageIndex, 1);
        int pageSize = 10;

        EntrprsManageVO searchVO = new EntrprsManageVO();
        searchVO.setPageIndex(currentPage);
        searchVO.setRecordCountPerPage(pageSize);

        String keyword = searchKeyword == null ? "" : searchKeyword.trim();
        searchVO.setSearchKeyword(keyword);
        searchVO.setSearchCondition("all");

        String memberType = membershipType == null ? "" : membershipType.trim().toUpperCase();
        if (!memberType.isEmpty()) {
            String dbTypeCode = normalizeMembershipCode(memberType);
            if (!dbTypeCode.isEmpty()) {
                searchVO.setEntrprsSeCode(dbTypeCode);
            }
        }

        String status = sbscrbSttus == null ? "" : sbscrbSttus.trim();
        if (!status.isEmpty()) {
            searchVO.setSbscrbSttus(status);
        }

        List<EntrprsManageVO> memberList;
        int totalCount;
        try {
            totalCount = entrprsManageService.selectEntrprsMberListTotCnt(searchVO);
            int totalPages = totalCount == 0 ? 1 : (int) Math.ceil(totalCount / (double) pageSize);
            if (currentPage > totalPages) {
                currentPage = totalPages;
            }
            searchVO.setPageIndex(currentPage);
            searchVO.setFirstIndex((currentPage - 1) * pageSize);
            memberList = entrprsManageService.selectEntrprsMberList(searchVO);
        } catch (Exception e) {
            memberList = Collections.emptyList();
            totalCount = 0;
            model.addAttribute("memberListError", e.getMessage());
        }

        int totalPages = totalCount == 0 ? 1 : (int) Math.ceil(totalCount / (double) pageSize);
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        int startPage = Math.max(1, currentPage - 4);
        int endPage = Math.min(totalPages, startPage + 9);
        if (endPage - startPage < 9) {
            startPage = Math.max(1, endPage - 9);
        }

        model.addAttribute("memberList", memberList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageIndex", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("membershipType", memberType);
        model.addAttribute("sbscrbSttus", status);
        return viewName;
    }

    @RequestMapping(value = { "/member/auth-group", "/auth/group" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String authGroup() {
        return "egovframework/com/admin/authGroup";
    }

    @RequestMapping(value = "/member/list/excel", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<byte[]> memberListExcel(
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "membershipType", required = false) String membershipType,
            @RequestParam(value = "sbscrbSttus", required = false) String sbscrbSttus) throws Exception {
        EntrprsManageVO searchVO = new EntrprsManageVO();
        searchVO.setPageIndex(1);
        searchVO.setFirstIndex(0);

        String keyword = searchKeyword == null ? "" : searchKeyword.trim();
        searchVO.setSearchKeyword(keyword);
        searchVO.setSearchCondition("all");

        String memberType = membershipType == null ? "" : membershipType.trim().toUpperCase();
        if (!memberType.isEmpty()) {
            String dbTypeCode = normalizeMembershipCode(memberType);
            if (!dbTypeCode.isEmpty()) {
                searchVO.setEntrprsSeCode(dbTypeCode);
            }
        }

        String status = sbscrbSttus == null ? "" : sbscrbSttus.trim();
        if (!status.isEmpty()) {
            searchVO.setSbscrbSttus(status);
        }

        int totalCount = entrprsManageService.selectEntrprsMberListTotCnt(searchVO);
        searchVO.setRecordCountPerPage(Math.max(totalCount, 1));

        @SuppressWarnings("unchecked")
        List<EntrprsManageVO> memberList = (List<EntrprsManageVO>) (List<?>) entrprsManageService.selectEntrprsMberList(searchVO);

        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("회원목록");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            DataFormat dataFormat = workbook.createDataFormat();
            CellStyle dateTimeStyle = workbook.createCellStyle();
            dateTimeStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd hh:mm:ss"));
            dateTimeStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"번호", "회원명", "아이디", "회원유형", "소속기관", "가입일", "상태"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            int no = totalCount;
            for (EntrprsManageVO m : memberList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(no--);
                row.createCell(1).setCellValue(safeString(m.getApplcntNm()));
                row.createCell(2).setCellValue(safeString(m.getEntrprsmberId()));
                row.createCell(3).setCellValue(resolveMembershipTypeLabel(m.getEntrprsSeCode()));
                row.createCell(4).setCellValue(safeString(m.getCmpnyNm()));

                Cell joinDateCell = row.createCell(5);
                Date joinDate = parseJoinDate(m.getSbscrbDe());
                if (joinDate != null) {
                    joinDateCell.setCellValue(joinDate);
                    joinDateCell.setCellStyle(dateTimeStyle);
                } else {
                    joinDateCell.setCellValue(safeString(m.getSbscrbDe()));
                }

                row.createCell(6).setCellValue(resolveStatusLabel(m.getEntrprsMberSttus()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 1024, 256 * 50));
            }

            workbook.write(out);
            content = out.toByteArray();
        }

        String baseName = "member_list_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        String encoded = URLEncoder.encode(baseName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    @RequestMapping(value = "/**", method = { RequestMethod.GET, RequestMethod.POST })
    public String adminFallback(HttpServletRequest request, Locale locale) {
        String accessToken = jwtProvider.getCookie(request, "accessToken");
        if (ObjectUtils.isEmpty(accessToken)) {
            return "redirect:/admin/login/loginView";
        }
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return "egovframework/com/admin/index_en";
        }
        return "egovframework/com/admin/index";
    }

    private String normalizeMembershipCode(String membershipType) {
        if ("EMITTER".equals(membershipType)) return "E";
        if ("PERFORMER".equals(membershipType)) return "P";
        if ("CENTER".equals(membershipType)) return "C";
        if ("GOV".equals(membershipType)) return "G";
        if ("E".equals(membershipType) || "P".equals(membershipType) || "C".equals(membershipType) || "G".equals(membershipType)) {
            return membershipType;
        }
        return "";
    }

    private String normalizeMemberStatusCode(String statusCode) {
        String v = safeString(statusCode).toUpperCase();
        if ("P".equals(v) || "A".equals(v) || "R".equals(v) || "D".equals(v) || "X".equals(v)) {
            return v;
        }
        return "";
    }

    private String resolveMembershipTypeLabel(String code) {
        String v = code == null ? "" : code.trim().toUpperCase();
        if ("E".equals(v) || "EMITTER".equals(v)) return "CO2 배출 및 포집 기업";
        if ("P".equals(v) || "PERFORMER".equals(v)) return "CCUS 사업 수행 기업";
        if ("C".equals(v) || "CENTER".equals(v)) return "CCUS 진흥센터";
        if ("G".equals(v) || "GOV".equals(v)) return "주무관청 / 행정기관";
        return v.isEmpty() ? "기타" : v;
    }

    private String resolveMembershipTypeLabelEn(String code) {
        String v = code == null ? "" : code.trim().toUpperCase();
        if ("E".equals(v) || "EMITTER".equals(v)) return "CO2 Emitter/Capture Company";
        if ("P".equals(v) || "PERFORMER".equals(v)) return "CCUS Project Company";
        if ("C".equals(v) || "CENTER".equals(v)) return "CCUS Promotion Center";
        if ("G".equals(v) || "GOV".equals(v)) return "Government / Agency";
        return v.isEmpty() ? "Other" : v;
    }

    private String resolveStatusLabel(String statusCode) {
        String v = statusCode == null ? "" : statusCode.trim().toUpperCase();
        if ("P".equals(v)) return "활성";
        if ("A".equals(v)) return "승인 대기";
        if ("R".equals(v)) return "반려";
        if ("D".equals(v)) return "삭제";
        if ("X".equals(v)) return "차단";
        return v.isEmpty() ? "기타" : v;
    }

    private String resolveStatusLabelEn(String statusCode) {
        String v = statusCode == null ? "" : statusCode.trim().toUpperCase();
        if ("P".equals(v)) return "Active";
        if ("A".equals(v)) return "Pending Approval";
        if ("R".equals(v)) return "Rejected";
        if ("D".equals(v)) return "Deleted";
        if ("X".equals(v)) return "Blocked";
        return v.isEmpty() ? "Other" : v;
    }

    private String resolveStatusBadgeClass(String statusCode) {
        String v = statusCode == null ? "" : statusCode.trim().toUpperCase();
        if ("P".equals(v)) return "bg-emerald-100 text-emerald-700";
        if ("A".equals(v)) return "bg-blue-100 text-blue-700";
        if ("R".equals(v)) return "bg-amber-100 text-amber-700";
        if ("D".equals(v)) return "bg-slate-200 text-slate-700";
        if ("X".equals(v)) return "bg-red-100 text-red-700";
        return "bg-gray-100 text-gray-700";
    }

    private String resolveInstitutionStatusLabel(String statusCode) {
        String v = safeString(statusCode).toUpperCase();
        if ("A".equals(v)) return "검토 중";
        if ("P".equals(v)) return "가입 승인 완료";
        if ("R".equals(v)) return "반려";
        if ("X".equals(v)) return "차단";
        if ("D".equals(v)) return "삭제";
        return v.isEmpty() ? "-" : v;
    }

    private String resolveInstitutionStatusLabelEn(String statusCode) {
        String v = safeString(statusCode).toUpperCase();
        if ("A".equals(v)) return "Under Review";
        if ("P".equals(v)) return "Approved";
        if ("R".equals(v)) return "Rejected";
        if ("X".equals(v)) return "Blocked";
        if ("D".equals(v)) return "Deleted";
        return v.isEmpty() ? "-" : v;
    }

    private String resolveBusinessRoleLabel(String code) {
        String v = safeString(code).toUpperCase();
        if ("E".equals(v)) return "배출량 산정 및 감축 실적 제출 담당";
        if ("P".equals(v)) return "CCUS 사업 수행 및 거래 연계 담당";
        if ("C".equals(v)) return "진흥센터 인증 및 통합 관제 담당";
        if ("G".equals(v)) return "정책 검토 및 행정 승인 담당";
        return "플랫폼 일반 사용자";
    }

    private String resolveBusinessRoleLabelEn(String code) {
        String v = safeString(code).toUpperCase();
        if ("E".equals(v)) return "Emission calculation and reduction submission owner";
        if ("P".equals(v)) return "CCUS execution and trading liaison";
        if ("C".equals(v)) return "Certification and integrated monitoring operator";
        if ("G".equals(v)) return "Policy review and administrative approver";
        return "General platform user";
    }

    private List<String> resolveAccessScopes(String code) {
        String v = safeString(code).toUpperCase();
        List<String> scopes = new ArrayList<>();
        if ("E".equals(v)) {
            scopes.add("배출량 자가산정");
            scopes.add("탄소발자국 모니터링");
            scopes.add("감축 보고서 제출");
            scopes.add("탄소 크레딧 조회");
        } else if ("P".equals(v)) {
            scopes.add("포집·수송·저장 데이터 입력");
            scopes.add("거래 매칭 및 요청 관리");
            scopes.add("실적 보고서 제출");
            scopes.add("거래 현황 모니터링");
        } else if ("C".equals(v)) {
            scopes.add("인증 보고서 검토");
            scopes.add("인증서 승인·발급");
            scopes.add("통합 관제 및 센서 모니터링");
            scopes.add("통계 시각화 관리");
        } else if ("G".equals(v)) {
            scopes.add("행정기관 검토");
            scopes.add("승인 상태 관리");
            scopes.add("정책 통계 조회");
            scopes.add("대외 제출 결과 확인");
        } else {
            scopes.add("기본 조회");
        }
        return scopes;
    }

    private List<String> resolveAccessScopesEn(String code) {
        String v = safeString(code).toUpperCase();
        List<String> scopes = new ArrayList<>();
        if ("E".equals(v)) {
            scopes.add("Self-service emissions calculation");
            scopes.add("Carbon footprint monitoring");
            scopes.add("Reduction report submission");
            scopes.add("Carbon credit lookup");
        } else if ("P".equals(v)) {
            scopes.add("Capture/transport/storage data entry");
            scopes.add("Trade matching and request management");
            scopes.add("Performance report submission");
            scopes.add("Trade status monitoring");
        } else if ("C".equals(v)) {
            scopes.add("Certification report review");
            scopes.add("Certificate approval and issuance");
            scopes.add("Integrated monitoring and sensor oversight");
            scopes.add("Statistics visualization management");
        } else if ("G".equals(v)) {
            scopes.add("Administrative review");
            scopes.add("Approval state control");
            scopes.add("Policy statistics lookup");
            scopes.add("External submission verification");
        } else {
            scopes.add("Basic access");
        }
        return scopes;
    }

    private String resolveDocumentStatusLabel(String filePath) {
        return safeString(filePath).isEmpty() ? "등록 문서 없음" : "사업자등록증 등록됨";
    }

    private String resolveDocumentStatusLabelEn(String filePath) {
        return safeString(filePath).isEmpty() ? "No document registered" : "Business registration file attached";
    }

    private void populateMemberEditModel(Model model, EntrprsManageVO member, boolean isEn) {
        Map<String, Object> institutionInfo = loadInstitutionInfo(member);
        EntrprsManageVO displayMember = mergeMemberWithInstitutionInfo(member, institutionInfo);
        model.addAttribute("member", displayMember);
        model.addAttribute("memberEvidenceFiles", loadEvidenceFiles(displayMember));
        model.addAttribute("memberId", safeString(displayMember.getEntrprsmberId()));
        model.addAttribute("phoneNumber", formatPhoneNumber(displayMember.getAreaNo(), displayMember.getEntrprsMiddleTelno(), displayMember.getEntrprsEndTelno()));
        model.addAttribute("membershipTypeLabel", isEn
                ? resolveMembershipTypeLabelEn(displayMember.getEntrprsSeCode())
                : resolveMembershipTypeLabel(displayMember.getEntrprsSeCode()));
        model.addAttribute("businessRoleLabel", isEn
                ? resolveBusinessRoleLabelEn(displayMember.getEntrprsSeCode())
                : resolveBusinessRoleLabel(displayMember.getEntrprsSeCode()));
        model.addAttribute("accessScopes", isEn
                ? resolveAccessScopesEn(displayMember.getEntrprsSeCode())
                : resolveAccessScopes(displayMember.getEntrprsSeCode()));
        model.addAttribute("statusLabel", isEn
                ? resolveStatusLabelEn(displayMember.getEntrprsMberSttus())
                : resolveStatusLabel(displayMember.getEntrprsMberSttus()));
        model.addAttribute("memberStatusCode", safeString(displayMember.getEntrprsMberSttus()).toUpperCase());
        model.addAttribute("memberTypeCode", safeString(displayMember.getEntrprsSeCode()).toUpperCase());
        model.addAttribute("memberDocumentStatusLabel", isEn
                ? resolveDocumentStatusLabelEn(displayMember.getBizRegFilePath())
                : resolveDocumentStatusLabel(displayMember.getBizRegFilePath()));
        if (institutionInfo != null && !institutionInfo.isEmpty()) {
            model.addAttribute("institutionInfo", institutionInfo);
            model.addAttribute("institutionStatusLabel", isEn
                    ? resolveInstitutionStatusLabelEn(stringValue(institutionInfo.get("INSTT_STTUS")))
                    : resolveInstitutionStatusLabel(stringValue(institutionInfo.get("INSTT_STTUS"))));
            model.addAttribute("institutionInsttId", stringValue(institutionInfo.get("INSTT_ID")));
            model.addAttribute("documentStatusLabel", isEn
                    ? resolveDocumentStatusLabelEn(stringValue(institutionInfo.get("BIZ_REG_FILE_PATH")))
                    : resolveDocumentStatusLabel(stringValue(institutionInfo.get("BIZ_REG_FILE_PATH"))));
        } else {
            model.addAttribute("institutionStatusLabel", "-");
            model.addAttribute("institutionInsttId", "");
            model.addAttribute("documentStatusLabel", isEn ? "No document registered" : "등록 문서 없음");
        }
    }

    private EntrprsManageVO mergeMemberWithInstitutionInfo(EntrprsManageVO member, Map<String, Object> institutionInfo) {
        if (institutionInfo == null || institutionInfo.isEmpty()) {
            return member;
        }
        if (isBlankMemberValue(member.getCmpnyNm())) {
            member.setCmpnyNm(stringValue(institutionInfo.get("INSTT_NM")));
        }
        if (isBlankMemberValue(member.getCxfc())) {
            member.setCxfc(stringValue(institutionInfo.get("REPRSNT_NM")));
        }
        if (isBlankMemberValue(member.getBizrno())) {
            member.setBizrno(stringValue(institutionInfo.get("BIZRNO")));
        }
        if (isBlankMemberValue(member.getApplcntEmailAdres())) {
            member.setApplcntEmailAdres(stringValue(institutionInfo.get("CHARGER_EMAIL")));
        }
        if (isBlankMemberValue(member.getApplcntNm())) {
            member.setApplcntNm(stringValue(institutionInfo.get("CHARGER_NM")));
        }
        return member;
    }

    private boolean isBlankMemberValue(String value) {
        String normalized = safeString(value);
        return normalized.isEmpty()
                || "-".equals(normalized)
                || "000000".equals(normalized)
                || "주소미입력".equals(normalized)
                || "address pending".equalsIgnoreCase(normalized);
    }

    private List<EvidenceFileView> loadEvidenceFiles(EntrprsManageVO member) {
        try {
            List<EntrprsMberFileVO> fileList = entrprsManageService.selectEntrprsMberFiles(member.getEntrprsmberId());
            if (fileList != null && !fileList.isEmpty()) {
                List<EvidenceFileView> evidenceFiles = new ArrayList<>();
                for (EntrprsMberFileVO fileVO : fileList) {
                    String path = safeString(fileVO.getFileStrePath());
                    if (path.isEmpty()) {
                        continue;
                    }
                    String encodedPath = urlEncode(path);
                    evidenceFiles.add(new EvidenceFileView(
                            safeString(fileVO.getOrignlFileNm()).isEmpty() ? new File(path).getName() : fileVO.getOrignlFileNm(),
                            path,
                            "/admin/member/file?filePath=" + encodedPath,
                            "/admin/member/file?filePath=" + encodedPath + "&download=true"));
                }
                if (!evidenceFiles.isEmpty()) {
                    return evidenceFiles;
                }
            }
        } catch (Exception ignored) {
        }
        return buildEvidenceFilesFromPath(member.getBizRegFilePath());
    }

    private List<EvidenceFileView> buildEvidenceFilesFromPath(String filePathValue) {
        String value = safeString(filePathValue);
        if (value.isEmpty()) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .map(path -> {
                    String encodedPath = urlEncode(path);
                    return new EvidenceFileView(
                            new File(path).getName(),
                            path,
                            "/admin/member/file?filePath=" + encodedPath,
                            "/admin/member/file?filePath=" + encodedPath + "&download=true");
                })
                .collect(Collectors.toList());
    }

    private String resolveMediaType(String fileName) {
        String lower = safeString(fileName).toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        }
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    public static class EvidenceFileView {
        private final String fileName;
        private final String filePath;
        private final String previewUrl;
        private final String downloadUrl;

        public EvidenceFileView(String fileName, String filePath, String previewUrl, String downloadUrl) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.previewUrl = previewUrl;
            this.downloadUrl = downloadUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }
    }

    private Map<String, Object> loadInstitutionInfo(EntrprsManageVO member) {
        try {
            if (safeString(member.getInsttId()).isEmpty()
                    && (safeString(member.getBizrno()).isEmpty() || safeString(member.getCxfc()).isEmpty())) {
                return Collections.emptyMap();
            }
            InsttInfoVO insttInfoVO = new InsttInfoVO();
            if (!safeString(member.getInsttId()).isEmpty()) {
                insttInfoVO.setInsttId(member.getInsttId());
            } else {
                insttInfoVO.setBizrno(member.getBizrno());
                insttInfoVO.setReprsntNm(member.getCxfc());
            }
            return entrprsManageService.selectInsttInfoForStatus(insttInfoVO);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String formatPhoneNumber(String areaNo, String middleNo, String endNo) {
        String a = safeString(areaNo);
        String m = safeString(middleNo);
        String e = safeString(endNo);
        if (a.isEmpty() && m.isEmpty() && e.isEmpty()) {
            return "-";
        }
        if (!a.isEmpty() && !m.isEmpty() && !e.isEmpty()) {
            return a + "-" + m + "-" + e;
        }
        if (!m.isEmpty() && !e.isEmpty()) {
            return m + "-" + e;
        }
        return (a + m + e).trim();
    }

    private String[] splitPhoneNumber(String phoneNumber) {
        String digits = digitsOnly(phoneNumber);
        if (digits.length() == 9) {
            return new String[]{digits.substring(0, 2), digits.substring(2, 5), digits.substring(5)};
        }
        if (digits.length() == 10) {
            if (digits.startsWith("02")) {
                return new String[]{digits.substring(0, 2), digits.substring(2, 6), digits.substring(6)};
            }
            return new String[]{digits.substring(0, 3), digits.substring(3, 6), digits.substring(6)};
        }
        if (digits.length() == 11) {
            return new String[]{digits.substring(0, 3), digits.substring(3, 7), digits.substring(7)};
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        String value = safeString(email);
        return !value.isEmpty() && value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private String digitsOnly(String value) {
        return safeString(value).replaceAll("[^0-9]", "");
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(safeString(value), StandardCharsets.UTF_8);
    }

    private Date parseJoinDate(String value) {
        String v = safeString(value);
        if (v.isEmpty()) {
            return null;
        }
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy.MM.dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "yyyyMMddHHmmss",
                "yyyy-MM-dd",
                "yyyy.MM.dd",
                "yyyy/MM/dd",
                "yyyyMMdd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                return sdf.parse(v);
            } catch (Exception ignore) {
                // try next
            }
        }
        return null;
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }
}
