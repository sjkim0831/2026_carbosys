package egovframework.com.sr.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sr")
public class SrController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("statusSummary", statusSummary());
        model.addAttribute("srList", sampleSrList());
        model.addAttribute("trainingDocs", sampleTrainingDocs());
        return "sr/dashboard";
    }

    private Map<String, Integer> statusSummary() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("RECEIVED", 4);
        map.put("ANALYSIS", 2);
        map.put("DEVELOP", 3);
        map.put("VERIFY", 1);
        map.put("DONE", 5);
        return map;
    }

    private List<Map<String, String>> sampleSrList() {
        return Arrays.asList(
                row("SR-2026-001", "기업회원 가입 절차 보완", "HIGH", "DEVELOP", "kim"),
                row("SR-2026-002", "문서 출력 PDF 개선", "MEDIUM", "ANALYSIS", "lee"),
                row("SR-2026-003", "접근성 라벨 수정", "LOW", "VERIFY", "park")
        );
    }

    private List<Map<String, String>> sampleTrainingDocs() {
        return Arrays.asList(
                row("TR-2026-011", "ISMS 기본교육", "COMPLETED", "2026-02-14", "kim"),
                row("TR-2026-012", "개인정보 처리 교육", "COMPLETED", "2026-02-20", "lee"),
                row("TR-2026-013", "운영 배포 절차 교육", "PLANNED", "2026-03-05", "park")
        );
    }

    private Map<String, String> row(String c1, String c2, String c3, String c4, String c5) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("c1", c1);
        map.put("c2", c2);
        map.put("c3", c3);
        map.put("c4", c4);
        map.put("c5", c5);
        return map;
    }
}
