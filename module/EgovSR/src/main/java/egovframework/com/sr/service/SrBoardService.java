package egovframework.com.sr.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SrBoardService {
    private final AtomicInteger srSeq = new AtomicInteger(0);
    private final AtomicInteger trSeq = new AtomicInteger(0);
    private final List<SrItem> srItems = new CopyOnWriteArrayList<>();
    private final List<TrainingDoc> trainingDocs = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        createSr("기업회원 가입 절차 보완", "회원사 공통/확장 항목 분리 및 검증 규칙 보완", "HIGH", "kim", "kim", LocalDate.now().plusDays(3));
        createSr("문서 출력 PDF 개선", "교육 이력 출력물 템플릿 정리 및 레이아웃 개선", "MEDIUM", "lee", "lee", LocalDate.now().plusDays(7));
        createSr("접근성 라벨 수정", "로그인/신청 화면 폼 접근성 점검 및 수정", "LOW", "park", "park", LocalDate.now().plusDays(5));
        updateSrStatus("SR-2026-001", "DEVELOP", "모델/검증 코드 수정 진행");
        updateSrStatus("SR-2026-002", "ANALYSIS", "요구사항 정리 중");
        updateSrStatus("SR-2026-003", "VERIFY", "사전 점검 완료, QA 대기");

        addTrainingDoc("ISMS 기본교육", "COMPLETED", LocalDate.of(2026, 2, 14), "kim", "분기 필수 교육");
        addTrainingDoc("개인정보 처리 교육", "COMPLETED", LocalDate.of(2026, 2, 20), "lee", "신규 인원 포함");
        addTrainingDoc("운영 배포 절차 교육", "PLANNED", LocalDate.of(2026, 3, 5), "park", "무중단 배포 절차 공유");
    }

    public List<String> srStatusOptions() {
        return Arrays.asList("RECEIVED", "ANALYSIS", "DEVELOP", "VERIFY", "DONE", "HOLD", "REJECTED");
    }

    public List<String> docStatusOptions() {
        return Arrays.asList("PLANNED", "IN_PROGRESS", "COMPLETED", "ARCHIVED");
    }

    public Map<String, Integer> statusSummary() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String status : srStatusOptions()) {
            map.put(status, 0);
        }
        for (SrItem item : srItems) {
            if (map.containsKey(item.getStatus())) {
                map.put(item.getStatus(), map.get(item.getStatus()) + 1);
            }
        }
        return map;
    }

    public List<SrItem> listSrItems() {
        List<SrItem> copy = new ArrayList<>(srItems);
        copy.sort(Comparator.comparing(SrItem::getUpdatedAt).reversed());
        return copy;
    }

    public List<TrainingDoc> listTrainingDocs() {
        List<TrainingDoc> copy = new ArrayList<>(trainingDocs);
        copy.sort(Comparator.comparing(TrainingDoc::getDate).reversed());
        return copy;
    }

    public SrItem createSr(String title, String description, String priority, String requester, String assignee, LocalDate dueDate) {
        int seq = srSeq.incrementAndGet();
        String id = String.format("SR-2026-%03d", seq);
        LocalDateTime now = LocalDateTime.now();

        SrItem item = new SrItem();
        item.setId(id);
        item.setTitle(blankAs(title, "제목 없음"));
        item.setDescription(blankAs(description, ""));
        item.setPriority(blankAs(priority, "MEDIUM"));
        item.setStatus("RECEIVED");
        item.setRequester(blankAs(requester, "unknown"));
        item.setAssignee(blankAs(assignee, blankAs(requester, "unknown")));
        item.setDueDate(dueDate);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item.setLastNote("생성됨");
        srItems.add(item);
        return item;
    }

    public boolean updateSrStatus(String id, String status, String note) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        SrItem item = srItems.stream().filter(x -> id.equals(x.getId())).findFirst().orElse(null);
        if (item == null) {
            return false;
        }
        item.setStatus(blankAs(status, item.getStatus()));
        item.setLastNote(blankAs(note, item.getLastNote()));
        item.setUpdatedAt(LocalDateTime.now());
        return true;
    }

    public TrainingDoc addTrainingDoc(String title, String status, LocalDate date, String owner, String note) {
        int seq = trSeq.incrementAndGet();
        String id = String.format("TR-2026-%03d", seq);
        TrainingDoc doc = new TrainingDoc();
        doc.setId(id);
        doc.setTitle(blankAs(title, "교육 문서"));
        doc.setStatus(blankAs(status, "PLANNED"));
        doc.setDate(date != null ? date : LocalDate.now());
        doc.setOwner(blankAs(owner, "unknown"));
        doc.setNote(blankAs(note, ""));
        trainingDocs.add(doc);
        return doc;
    }

    private String blankAs(String v, String fallback) {
        if (v == null || v.trim().isEmpty()) {
            return fallback;
        }
        return v.trim();
    }

    public static class SrItem {
        private String id;
        private String title;
        private String description;
        private String priority;
        private String status;
        private String requester;
        private String assignee;
        private LocalDate dueDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String lastNote;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRequester() { return requester; }
        public void setRequester(String requester) { this.requester = requester; }
        public String getAssignee() { return assignee; }
        public void setAssignee(String assignee) { this.assignee = assignee; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public String getLastNote() { return lastNote; }
        public void setLastNote(String lastNote) { this.lastNote = lastNote; }
    }

    public static class TrainingDoc {
        private String id;
        private String title;
        private String status;
        private LocalDate date;
        private String owner;
        private String note;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}

