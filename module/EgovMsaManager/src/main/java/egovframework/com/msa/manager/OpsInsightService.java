package egovframework.com.msa.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class OpsInsightService {
    private static final String MAPPING_FILE = "/app/msa-mappings.yml";
    private static final String MODULE_ROOT = "/opt/carbosys/module";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern IMG_NO_ALT = Pattern.compile("<img\\b(?![^>]*\\balt\\s*=)[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern INPUT_TAG = Pattern.compile("<(input|select|textarea)\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern BUTTON_EMPTY = Pattern.compile("<button\\b[^>]*>\\s*</button>", Pattern.CASE_INSENSITIVE);
    private static final Pattern A_EMPTY = Pattern.compile("<a\\b[^>]*>\\s*</a>", Pattern.CASE_INSENSITIVE);

    @Autowired
    private LogAnalyticsService logAnalyticsService;

    @Autowired
    private MsaProcessManager processManager;

    private final MsaScanner scanner = new MsaScanner();

    public Map<String, Object> getSecurityViolations() {
        List<Map<String, Object>> critical = logAnalyticsService.getCriticalEvents();
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Integer> gradeCounts = new LinkedHashMap<>();
        gradeCounts.put("critical", 0);
        gradeCounts.put("high", 0);
        gradeCounts.put("medium", 0);
        gradeCounts.put("low", 0);

        for (Map<String, Object> event : critical) {
            String msg = String.valueOf(event.get("message"));
            String grade = classifyGrade(msg);
            String category = classifyCategory(msg);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("time", event.get("time"));
            row.put("module", event.get("module"));
            row.put("level", event.get("level"));
            row.put("grade", grade);
            row.put("category", category);
            row.put("message", msg);
            rows.add(row);

            gradeCounts.put(grade, gradeCounts.get(grade) + 1);
        }

        rows.sort((a, b) -> {
            LocalDateTime at = parseTime(String.valueOf(a.get("time")));
            LocalDateTime bt = parseTime(String.valueOf(b.get("time")));
            if (at == null && bt == null) return 0;
            if (at == null) return 1;
            if (bt == null) return -1;
            return bt.compareTo(at);
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", rows.size());
        out.put("gradeCounts", gradeCounts);
        out.put("items", rows);
        return out;
    }

    public Map<String, Object> getTrafficOverview() {
        List<MsaScanner.ModuleInfo> modules = scanner.scan();
        List<Map<String, Object>> mappings = loadMappings();

        Map<String, List<Map<String, Object>>> pathByModule = new HashMap<>();
        for (Map<String, Object> m : mappings) {
            String module = String.valueOf(m.get("module"));
            pathByModule.computeIfAbsent(module, k -> new ArrayList<>()).add(m);
        }

        List<Map<String, Object>> moduleStats = new ArrayList<>();
        List<Map<String, Object>> controllers = new ArrayList<>();

        long totalMemMb = 0;
        double totalCpu = 0.0;
        int totalConn = 0;
        int runningCount = 0;
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int cpuCapacityPct = cpuCores * 100;

        for (MsaScanner.ModuleInfo mod : modules) {
            String status = processManager.getStatus(mod.getId(), mod.getPort());
            boolean running = "running".equals(status);

            Integer rssMb = running ? readRssMbByPort(mod.getPort()) : null;
            Double cpuPct = running ? readCpuPctByPort(mod.getPort()) : null;
            Integer activeUsers = running ? readEstablishedConnByPort(mod.getPort()) : 0;

            if (rssMb != null) totalMemMb += rssMb;
            if (cpuPct != null) totalCpu += cpuPct;
            totalConn += activeUsers != null ? activeUsers : 0;
            if (running) runningCount++;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("module", mod.getId());
            row.put("port", mod.getPort());
            row.put("status", status);
            row.put("memoryMb", rssMb);
            row.put("cpuPct", cpuPct == null ? null : round2(cpuPct));
            row.put("activeUsers", activeUsers);
            row.put("controllerCount", pathByModule.getOrDefault(mod.getId(), Collections.emptyList()).size());
            moduleStats.add(row);

            List<Map<String, Object>> modPaths = pathByModule.getOrDefault(mod.getId(), Collections.emptyList());
            for (Map<String, Object> p : modPaths) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("module", mod.getId());
                c.put("path", p.get("path"));
                c.put("method", p.get("method"));
                c.put("description", p.get("description"));
                c.put("status", status);
                controllers.add(c);
            }
        }

        moduleStats.sort(Comparator.comparing(o -> String.valueOf(o.get("module"))));
        controllers.sort(Comparator.comparing(o -> String.valueOf(o.get("path"))));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("runningModules", runningCount);
        summary.put("totalModules", modules.size());
        summary.put("totalMemoryMb", totalMemMb);
        summary.put("totalCpuPct", round2(totalCpu));
        summary.put("cpuCores", cpuCores);
        summary.put("cpuCapacityPct", cpuCapacityPct);
        summary.put("totalActiveUsers", totalConn);
        summary.put("controllerCount", controllers.size());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("time", LocalDateTime.now().format(TS_FMT));
        out.put("summary", summary);
        out.put("modules", moduleStats);
        out.put("controllers", controllers);
        return out;
    }

    public Map<String, Object> getAccessibilityIssues() {
        List<Map<String, Object>> issues = new ArrayList<>();
        Map<String, Integer> severity = new LinkedHashMap<>();
        severity.put("high", 0);
        severity.put("medium", 0);
        severity.put("low", 0);

        Path root = Paths.get(MODULE_ROOT);
        if (!Files.isDirectory(root)) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("total", 0);
            out.put("severity", severity);
            out.put("items", issues);
            return out;
        }

        AtomicInteger fileCount = new AtomicInteger();
        try (Stream<Path> stream = Files.walk(root, 12)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".html"))
                    .filter(p -> p.toString().contains("/src/main/resources/"))
                    .forEach(path -> {
                        fileCount.incrementAndGet();
                        scanHtml(path, root, issues, severity);
                    });
        } catch (Exception ignored) {
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scannedFiles", fileCount.get());
        out.put("total", issues.size());
        out.put("severity", severity);
        out.put("items", issues);
        return out;
    }

    private void scanHtml(Path path, Path root, List<Map<String, Object>> issues, Map<String, Integer> severity) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return;
        }

        String rel = root.relativize(path).toString().replace('\\', '/');
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String lower = line.toLowerCase(Locale.ROOT);
            int lineNo = i + 1;

            if (lower.contains("<html") && !lower.contains("lang=")) {
                addIssue(issues, severity, "low", "LANG_MISSING", rel, lineNo,
                        "html 태그에 lang 속성이 없습니다.", line);
            }
            if (IMG_NO_ALT.matcher(line).find()) {
                addIssue(issues, severity, "high", "IMG_ALT_MISSING", rel, lineNo,
                        "img 태그에 alt 속성이 없습니다.", line);
            }
            Matcher inputMat = INPUT_TAG.matcher(line);
            while (inputMat.find()) {
                String tag = inputMat.group();
                String t = tag.toLowerCase(Locale.ROOT);
                if (t.contains("type=\"hidden\"") || t.contains(" type='hidden'")) {
                    continue;
                }
                boolean hasId = t.contains(" id=");
                boolean hasAria = t.contains(" aria-label=") || t.contains(" aria-labelledby=");
                if (!hasId && !hasAria) {
                    addIssue(issues, severity, "medium", "FORM_LABEL_WEAK", rel, lineNo,
                            "폼 요소에 id/aria-label이 없어 접근성 라벨 연결이 약합니다.", line);
                }
            }
            if (BUTTON_EMPTY.matcher(line).find()) {
                String t = line.toLowerCase(Locale.ROOT);
                if (!(t.contains("aria-label=") || t.contains("title="))) {
                    addIssue(issues, severity, "medium", "BUTTON_TEXT_MISSING", rel, lineNo,
                            "버튼에 텍스트 또는 aria-label이 없습니다.", line);
                }
            }
            if (A_EMPTY.matcher(line).find()) {
                addIssue(issues, severity, "medium", "LINK_TEXT_MISSING", rel, lineNo,
                        "링크 텍스트가 비어 있습니다.", line);
            }
        }
    }

    private void addIssue(List<Map<String, Object>> issues,
                          Map<String, Integer> severity,
                          String level,
                          String code,
                          String file,
                          int line,
                          String message,
                          String snippet) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("severity", level);
        item.put("code", code);
        item.put("file", file);
        item.put("line", line);
        item.put("message", message);
        item.put("snippet", trimSnippet(snippet));
        issues.add(item);
        severity.put(level, severity.get(level) + 1);
    }

    private String trimSnippet(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.length() > 180 ? t.substring(0, 180) + "..." : t;
    }

    private String classifyGrade(String message) {
        String m = normalize(message);
        if (containsAny(m, "sql injection", "command injection", "rce", "privilege escalation", "authentication bypass")) {
            return "critical";
        }
        if (containsAny(m, "unauthorized", "forbidden", "csrf", "xss", "jwt", "token", "access denied", "bad credentials")) {
            return "high";
        }
        if (containsAny(m, "failed login", "invalid", "timeout", "warn", "suspicious")) {
            return "medium";
        }
        return "low";
    }

    private String classifyCategory(String message) {
        String m = normalize(message);
        if (containsAny(m, "sql", "query", "badsqlgrammar")) return "db/query";
        if (containsAny(m, "jwt", "token", "oauth", "auth", "login")) return "auth";
        if (containsAny(m, "xss", "csrf", "script")) return "web-threat";
        if (containsAny(m, "forbidden", "unauthorized", "access denied")) return "authorization";
        return "general";
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String src, String... needles) {
        if (src == null) return false;
        for (String n : needles) {
            if (src.contains(n)) return true;
        }
        return false;
    }

    private LocalDateTime parseTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(raw.trim(), TS_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> loadMappings() {
        File file = new File(MAPPING_FILE);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> obj = yaml.load(new FileInputStream(file));
            Object raw = obj.get("mappings");
            if (!(raw instanceof List)) {
                return Collections.emptyList();
            }
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object o : (List<?>) raw) {
                if (o instanceof Map) {
                    out.add(new LinkedHashMap<>((Map<String, Object>) o));
                }
            }
            return out;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Integer readRssMbByPort(Integer port) {
        if (port == null || port == 0) return null;
        String line = readFirstLine(Arrays.asList("sh", "-c",
                "ps -eo pid,rss,args --no-headers | awk '$3==\"java\" && index($0,\"--server.port=" + port + "\")>0 {print $0; exit}'"));
        if (line == null || line.trim().isEmpty()) return null;
        String[] arr = line.trim().split("\\s+", 3);
        if (arr.length < 2) return null;
        try {
            long kb = Long.parseLong(arr[1]);
            return (int) (kb / 1024L);
        } catch (Exception e) {
            return null;
        }
    }

    private Double readCpuPctByPort(Integer port) {
        if (port == null || port == 0) return null;
        String line = readFirstLine(Arrays.asList("sh", "-c",
                "ps -eo pcpu,args --no-headers | awk '$2==\"java\" && index($0,\"--server.port=" + port + "\")>0 {print $1; exit}'"));
        if (line == null || line.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(line.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer readEstablishedConnByPort(Integer port) {
        if (port == null || port == 0) return 0;
        String line = readFirstLine(Arrays.asList("sh", "-c",
                "ss -tan | awk '$1==\"ESTAB\" && $4 ~ /:" + port + "$/ {c++} END{print c+0}'"));
        if (line == null || line.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(line.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String readFirstLine(List<String> cmd) {
        try {
            Process p = new ProcessBuilder(cmd).start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line = br.readLine();
                p.waitFor();
                return line;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
