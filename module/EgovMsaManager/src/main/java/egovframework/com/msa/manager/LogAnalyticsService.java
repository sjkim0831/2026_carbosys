package egovframework.com.msa.manager;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LogAnalyticsService {
    private static final Path LOG_DIR = Paths.get("/opt/carbosys/logs");
    private static final Path OFFSET_FILE = LOG_DIR.resolve("log-offsets.properties");
    private static final Path LOG_ARCHIVE_FILE = LOG_DIR.resolve("module-log-events.jsonl");
    private static final Path CRITICAL_FILE = LOG_DIR.resolve("critical-events.jsonl");

    private static final int MAX_LOGS_PER_MODULE = 600;
    private static final int MAX_CRITICAL = 150;
    private static final long SCAN_INTERVAL_MS = 4000L;

    private final MsaScanner scanner = new MsaScanner();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ExecutorService loopExec = Executors.newSingleThreadExecutor();

    private final Map<String, Long> offsets = new ConcurrentHashMap<>();
    private final Map<String, Deque<Map<String, Object>>> logsByModule = new ConcurrentHashMap<>();
    private final Deque<Map<String, Object>> criticalEvents = new ArrayDeque<>();
    private final Map<String, Integer> controllerHits = new ConcurrentHashMap<>();
    private final Map<String, Integer> errorHotspots = new ConcurrentHashMap<>();

    private static final Pattern HTTP_PATH = Pattern.compile("\\b(GET|POST|PUT|DELETE|PATCH)\\s+\"([^\"]+)\"");
    private static final Pattern LOGGER_SOURCE = Pattern.compile("\\s([a-zA-Z0-9_.$]+)\\s*:\\s");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(LOG_DIR);
        } catch (IOException ignored) {
        }
        loadOffsets();
        loopExec.submit(() -> {
            while (running.get()) {
                try {
                    scanOnce();
                    Thread.sleep(SCAN_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {
                }
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        loopExec.shutdownNow();
        saveOffsets();
    }

    public Map<String, Object> getModuleLogs() {
        return getModuleLogs(null, null);
    }

    public Map<String, Object> getModuleLogs(LocalDateTime from, LocalDateTime to) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<Map<String, Object>> modules = new ArrayList<>();
        for (Map.Entry<String, Deque<Map<String, Object>>> e : logsByModule.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("module", e.getKey());
            List<Map<String, Object>> logs;
            synchronized (e.getValue()) {
                logs = filterByRange(new ArrayList<>(e.getValue()), from, to);
            }
            row.put("count", logs.size());
            row.put("logs", logs);
            row.put("last", logs.isEmpty() ? "" : logs.get(logs.size() - 1).get("message"));
            modules.add(row);
        }
        modules.sort(Comparator.comparing(o -> String.valueOf(o.get("module"))));
        out.put("modules", modules);
        out.put("criticalCount", getCriticalEvents(from, to).size());
        return out;
    }

    public List<Map<String, Object>> getCriticalEvents() {
        return getCriticalEvents(null, null);
    }

    public List<Map<String, Object>> getCriticalEvents(LocalDateTime from, LocalDateTime to) {
        synchronized (criticalEvents) {
            return filterByRange(new ArrayList<>(criticalEvents), from, to);
        }
    }

    public List<Map<String, Object>> getTopControllers() {
        return getTopControllers(null, null);
    }

    public List<Map<String, Object>> getTopControllers(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return toControllerRows(controllerHits);
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Deque<Map<String, Object>> q : logsByModule.values()) {
            synchronized (q) {
                for (Map<String, Object> event : q) {
                    if (!inRange(event, from, to)) {
                        continue;
                    }
                    mergeControllerHit(String.valueOf(event.get("message")), filtered);
                }
            }
        }
        return toControllerRows(filtered);
    }

    public List<Map<String, Object>> getTopErrors() {
        return getTopErrors(null, null);
    }

    public List<Map<String, Object>> getTopErrors(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return toErrorRows(errorHotspots);
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Deque<Map<String, Object>> q : logsByModule.values()) {
            synchronized (q) {
                for (Map<String, Object> event : q) {
                    if (!inRange(event, from, to)) {
                        continue;
                    }
                    mergeErrorHotspot(String.valueOf(event.get("message")), filtered);
                }
            }
        }
        return toErrorRows(filtered);
    }

    private List<Map<String, Object>> filterByRange(List<Map<String, Object>> src, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return src;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> event : src) {
            if (inRange(event, from, to)) {
                out.add(event);
            }
        }
        return out;
    }

    private boolean inRange(Map<String, Object> event, LocalDateTime from, LocalDateTime to) {
        LocalDateTime time = parseEventTime(String.valueOf(event.get("time")));
        if (time == null) {
            return true;
        }
        if (from != null && time.isBefore(from)) {
            return false;
        }
        if (to != null && time.isAfter(to)) {
            return false;
        }
        return true;
    }

    private LocalDateTime parseEventTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw.trim(), TS_FMT);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<Map<String, Object>> toControllerRows(Map<String, Integer> src) {
        return src.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(40)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("controller", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> toErrorRows(Map<String, Integer> src) {
        return src.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(40)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("source", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                }).collect(Collectors.toList());
    }

    private void scanOnce() {
        List<MsaScanner.ModuleInfo> modules = scanner.scan();
        for (MsaScanner.ModuleInfo mod : modules) {
            Path logPath = Paths.get(mod.getDir(), "startup.log");
            if (!Files.exists(logPath)) {
                continue;
            }
            readNewLines(mod.getId(), logPath);
        }
        saveOffsets();
    }

    private void readNewLines(String moduleId, Path logPath) {
        long offset = offsets.getOrDefault(moduleId, 0L);
        try (RandomAccessFile raf = new RandomAccessFile(logPath.toFile(), "r")) {
            long len = raf.length();
            if (len < offset) {
                offset = 0L; // log rotated/truncated
            }
            raf.seek(offset);
            String line;
            while ((line = raf.readLine()) != null) {
                String msg = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                ingestLine(moduleId, msg);
            }
            offsets.put(moduleId, raf.getFilePointer());
        } catch (Exception ignored) {
        }
    }

    private void ingestLine(String moduleId, String line) {
        String level = detectLevel(line);
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("time", LocalDateTime.now().format(TS_FMT));
        event.put("module", moduleId);
        event.put("level", level);
        event.put("message", line);

        Deque<Map<String, Object>> q = logsByModule.computeIfAbsent(moduleId, k -> new ArrayDeque<>());
        synchronized (q) {
            q.addLast(event);
            while (q.size() > MAX_LOGS_PER_MODULE) {
                q.removeFirst();
            }
        }

        appendJsonLine(LOG_ARCHIVE_FILE, event);
        analyzeController(line);
        analyzeErrors(line, event);
    }

    private void analyzeController(String line) {
        mergeControllerHit(line, controllerHits);
    }

    private void analyzeErrors(String line, Map<String, Object> event) {
        boolean critical = isCritical(line);
        if (critical) {
            synchronized (criticalEvents) {
                criticalEvents.addFirst(event);
                while (criticalEvents.size() > MAX_CRITICAL) {
                    criticalEvents.removeLast();
                }
            }
            appendJsonLine(CRITICAL_FILE, event);
        }

        mergeErrorHotspot(line, errorHotspots);
    }

    private void mergeControllerHit(String line, Map<String, Integer> target) {
        Matcher m = HTTP_PATH.matcher(line);
        if (!m.find()) {
            return;
        }
        String method = m.group(1);
        String path = m.group(2);
        String key = method + " " + normalizePath(path);
        target.merge(key, 1, Integer::sum);
    }

    private void mergeErrorHotspot(String line, Map<String, Integer> target) {
        if (line.contains("ERROR") || line.contains("Exception") || line.contains("FAIL")) {
            String source = extractSource(line);
            target.merge(source, 1, Integer::sum);
        }
    }

    private String normalizePath(String path) {
        String p = path;
        p = p.replaceAll("/\\d+", "/{id}");
        p = p.replaceAll("=[0-9]+", "={n}");
        return p;
    }

    private String extractSource(String line) {
        Matcher m = LOGGER_SOURCE.matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        if (line.length() > 140) {
            return line.substring(0, 140);
        }
        return line;
    }

    private boolean isCritical(String line) {
        return line.contains("FATAL")
                || line.contains("OutOfMemoryError")
                || line.contains("APPLICATION FAILED TO START")
                || line.contains("PortInUseException")
                || line.contains("Connection refused")
                || line.contains("SQLException")
                || (line.contains("ERROR") && line.contains("Exception"));
    }

    private String detectLevel(String line) {
        if (line.contains("FATAL")) {
            return "FATAL";
        }
        if (line.contains("ERROR")) {
            return "ERROR";
        }
        if (line.contains("WARN")) {
            return "WARN";
        }
        if (line.contains("DEBUG")) {
            return "DEBUG";
        }
        if (line.contains("INFO")) {
            return "INFO";
        }
        return "LOG";
    }

    private void appendJsonLine(Path file, Map<String, Object> row) {
        try {
            String line = toJsonLine(row) + System.lineSeparator();
            Files.write(file, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }

    private String toJsonLine(Map<String, Object> row) {
        return "{"
                + "\"time\":\"" + esc(String.valueOf(row.get("time"))) + "\","
                + "\"module\":\"" + esc(String.valueOf(row.get("module"))) + "\","
                + "\"level\":\"" + esc(String.valueOf(row.get("level"))) + "\","
                + "\"message\":\"" + esc(String.valueOf(row.get("message"))) + "\""
                + "}";
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void loadOffsets() {
        if (!Files.exists(OFFSET_FILE)) {
            return;
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(OFFSET_FILE.toFile())) {
            p.load(in);
            for (String k : p.stringPropertyNames()) {
                offsets.put(k, Long.parseLong(p.getProperty(k, "0")));
            }
        } catch (Exception ignored) {
        }
    }

    private void saveOffsets() {
        Properties p = new Properties();
        for (Map.Entry<String, Long> e : offsets.entrySet()) {
            p.setProperty(e.getKey(), String.valueOf(e.getValue()));
        }
        try {
            Files.createDirectories(LOG_DIR);
            p.store(Files.newOutputStream(OFFSET_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    "log offsets");
        } catch (Exception ignored) {
        }
    }
}
