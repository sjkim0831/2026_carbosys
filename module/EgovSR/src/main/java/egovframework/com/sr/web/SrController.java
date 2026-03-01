package egovframework.com.sr.web;

import egovframework.com.sr.service.SrBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/sr")
public class SrController {
    @Autowired
    private SrBoardService srBoardService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("statusSummary", srBoardService.statusSummary());
        model.addAttribute("srList", srBoardService.listSrItems());
        model.addAttribute("trainingDocs", srBoardService.listTrainingDocs());
        model.addAttribute("srStatuses", srBoardService.srStatusOptions());
        model.addAttribute("docStatuses", srBoardService.docStatusOptions());
        return "sr/dashboard";
    }

    @PostMapping("/create")
    public String createSr(@RequestParam String title,
                           @RequestParam(required = false) String description,
                           @RequestParam String priority,
                           @RequestParam String requester,
                           @RequestParam(required = false) String assignee,
                           @RequestParam(required = false) String dueDate,
                           RedirectAttributes ra) {
        LocalDate due = null;
        if (dueDate != null && !dueDate.trim().isEmpty()) {
            due = LocalDate.parse(dueDate.trim());
        }
        SrBoardService.SrItem created = srBoardService.createSr(title, description, priority, requester, assignee, due);
        ra.addFlashAttribute("message", "SR 생성 완료: " + created.getId());
        return "redirect:/sr/dashboard";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable String id,
                               @RequestParam String status,
                               @RequestParam(required = false) String note,
                               RedirectAttributes ra) {
        boolean ok = srBoardService.updateSrStatus(id, status, note);
        if (ok) {
            ra.addFlashAttribute("message", id + " 상태 변경 완료");
        } else {
            ra.addFlashAttribute("message", id + " 상태 변경 실패");
        }
        return "redirect:/sr/dashboard";
    }

    @PostMapping("/training/create")
    public String createTraining(@RequestParam String title,
                                 @RequestParam String status,
                                 @RequestParam(required = false) String date,
                                 @RequestParam String owner,
                                 @RequestParam(required = false) String note,
                                 RedirectAttributes ra) {
        LocalDate docDate = null;
        if (date != null && !date.trim().isEmpty()) {
            docDate = LocalDate.parse(date.trim());
        }
        SrBoardService.TrainingDoc doc = srBoardService.addTrainingDoc(title, status, docDate, owner, note);
        ra.addFlashAttribute("message", "교육 문서 등록 완료: " + doc.getId());
        return "redirect:/sr/dashboard";
    }
}
