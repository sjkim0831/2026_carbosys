package egovframework.com.web;

import egovframework.com.uss.umt.service.EgovEntrprsManageService;
import egovframework.com.uss.umt.service.EntrprsManageVO;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EgovAdminMainManageController {

    private final EgovJwtProvider jwtProvider;
    private final EgovEntrprsManageService entrprsManageService;

    @RequestMapping(value = { "", "/" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String adminMainEntry(HttpServletRequest request) {
        String accessToken = jwtProvider.getCookie(request, "accessToken");
        if (ObjectUtils.isEmpty(accessToken)) {
            return "redirect:/admin/login/loginView";
        }
        return "egovframework/com/admin/index";
    }

    @RequestMapping(value = "/en", method = { RequestMethod.GET, RequestMethod.POST })
    public String indexEn(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "pageIndex", required = false) String pageIndexParam,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "membershipType", required = false) String membershipType,
            @RequestParam(value = "sbscrbSttus", required = false) String sbscrbSttus,
            Model model) {
        if ("member-list".equalsIgnoreCase(content)) {
            model.addAttribute("enContent", "member-list");
            return populateMemberList(pageIndexParam, searchKeyword, membershipType, sbscrbSttus, model,
                    "egovframework/com/admin/index_en");
        }
        model.addAttribute("enContent", "dashboard");
        return "egovframework/com/admin/index_en";
    }

    @RequestMapping(value = "/member/stats", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberStats() {
        return "egovframework/com/admin/memberStats";
    }

    @RequestMapping(value = "/member/register", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberRegister() {
        return "egovframework/com/admin/memberRegister";
    }

    @RequestMapping(value = "/member/approve", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberApprove() {
        return "egovframework/com/admin/memberApprove";
    }

    @RequestMapping(value = "/member/company-approve", method = { RequestMethod.GET, RequestMethod.POST })
    public String companyMemberApprove() {
        return "egovframework/com/admin/memberApprove";
    }

    @RequestMapping(value = "/member/edit", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberEdit(
            @RequestParam(value = "memberId", required = false) String memberId,
            Model model) {
        model.addAttribute("memberId", memberId == null ? "" : memberId.trim());
        return "egovframework/com/admin/memberEdit";
    }

    @RequestMapping(value = "/member/detail", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberDetail(
            @RequestParam(value = "memberId", required = false) String memberId,
            Model model) {
        String normalizedMemberId = memberId == null ? "" : memberId.trim();
        model.addAttribute("memberId", normalizedMemberId);

        if (normalizedMemberId.isEmpty()) {
            model.addAttribute("memberDetailError", "회원 ID가 전달되지 않았습니다.");
            return "egovframework/com/admin/memberDetail";
        }

        try {
            EntrprsManageVO member = entrprsManageService.selectEntrprsmberByMberId(normalizedMemberId);
            if (member == null || ObjectUtils.isEmpty(member.getEntrprsmberId())) {
                model.addAttribute("memberDetailError", "회원 정보를 찾을 수 없습니다.");
                return "egovframework/com/admin/memberDetail";
            }

            model.addAttribute("member", member);
            model.addAttribute("membershipTypeLabel", resolveMembershipTypeLabel(member.getEntrprsSeCode()));
            model.addAttribute("statusLabel", resolveStatusLabel(member.getEntrprsMberSttus()));
            model.addAttribute("statusBadgeClass", resolveStatusBadgeClass(member.getEntrprsMberSttus()));
            model.addAttribute("phoneNumber", formatPhoneNumber(member.getAreaNo(), member.getEntrprsMiddleTelno(), member.getEntrprsEndTelno()));
        } catch (Exception e) {
            model.addAttribute("memberDetailError", "회원 정보 조회 중 오류가 발생했습니다.");
        }

        return "egovframework/com/admin/memberDetail";
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
            Model model) {
        return populateMemberList(pageIndexParam, searchKeyword, membershipType, sbscrbSttus, model,
                "egovframework/com/admin/memberList");
    }

    @RequestMapping(value = "/member/list/en", method = { RequestMethod.GET, RequestMethod.POST })
    public String memberListEn(
            @RequestParam(value = "pageIndex", required = false) String pageIndexParam,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "membershipType", required = false) String membershipType,
            @RequestParam(value = "sbscrbSttus", required = false) String sbscrbSttus,
            Model model) {
        UriComponentsBuilder redirectBuilder = UriComponentsBuilder.fromPath("/admin/en")
                .queryParam("content", "member-list");
        if (!ObjectUtils.isEmpty(pageIndexParam)) {
            redirectBuilder.queryParam("pageIndex", pageIndexParam);
        }
        if (!ObjectUtils.isEmpty(searchKeyword)) {
            redirectBuilder.queryParam("searchKeyword", searchKeyword);
        }
        if (!ObjectUtils.isEmpty(membershipType)) {
            redirectBuilder.queryParam("membershipType", membershipType);
        }
        if (!ObjectUtils.isEmpty(sbscrbSttus)) {
            redirectBuilder.queryParam("sbscrbSttus", sbscrbSttus);
        }
        return "redirect:" + redirectBuilder.build().toUriString();
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

    private String resolveMembershipTypeLabel(String code) {
        String v = code == null ? "" : code.trim().toUpperCase();
        if ("E".equals(v) || "EMITTER".equals(v)) return "CO2 배출 및 포집 기업";
        if ("P".equals(v) || "PERFORMER".equals(v)) return "CCUS 사업 수행 기업";
        if ("C".equals(v) || "CENTER".equals(v)) return "CCUS 진흥센터";
        if ("G".equals(v) || "GOV".equals(v)) return "주무관청 / 행정기관";
        return v.isEmpty() ? "기타" : v;
    }

    private String resolveStatusLabel(String statusCode) {
        String v = statusCode == null ? "" : statusCode.trim().toUpperCase();
        if ("P".equals(v)) return "활성";
        if ("A".equals(v)) return "승인 대기";
        if ("D".equals(v)) return "정지";
        return v.isEmpty() ? "기타" : v;
    }

    private String resolveStatusBadgeClass(String statusCode) {
        String v = statusCode == null ? "" : statusCode.trim().toUpperCase();
        if ("P".equals(v)) return "bg-emerald-100 text-emerald-700";
        if ("A".equals(v)) return "bg-blue-100 text-blue-700";
        if ("D".equals(v)) return "bg-red-100 text-red-700";
        return "bg-gray-100 text-gray-700";
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
