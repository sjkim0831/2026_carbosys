package egovframework.com.msa.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/msa")
public class MsaController {

    @Autowired
    private MsaProcessManager processManager;

    @Autowired
    private ChangeMonitorService changeMonitorService;

    @Autowired
    private LogAnalyticsService logAnalyticsService;

    @Autowired
    private OpsInsightService opsInsightService;

    private final MsaScanner scanner = new MsaScanner();
    private static final String MAPPING_FILE = "/app/msa-mappings.yml";
    private static final DateTimeFormatter LOG_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("")
    public String index() {
        return "redirect:/admin/msa/manager";
    }

    @GetMapping("/manager")
    public String managerView(Model model) {
        return "msaManager";
    }

    @ResponseBody
    @GetMapping("/api/modules")
    public List<Map<String, Object>> getModules() {
        return scanner.scan().stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("dir", m.getDir());
            map.put("port", m.getPort());
            map.put("javaRunnable", m.isJavaRunnable());
            map.put("status", processManager.getStatus(m.getId(), m.getPort()));
            map.put("pid", processManager.getPid(m.getId()));
            return map;
        }).collect(Collectors.toList());
    }

    @ResponseBody
    @GetMapping("/api/mappings")
    public List<Map<String, Object>> getMappings() {
        List<Map<String, Object>> mappings = new ArrayList<>();
        try {
            File file = new File(MAPPING_FILE);
            if (file.exists()) {
                Yaml yaml = new Yaml();
                Map<String, Object> obj = yaml.load(new FileInputStream(file));
                if (obj.get("mappings") instanceof List) {
                    List<Map<String, Object>> rawMappings = (List<Map<String, Object>>) obj.get("mappings");

                    // Enrich each mapping with current module running status
                    List<MsaScanner.ModuleInfo> modules = scanner.scan();
                    for (Map<String, Object> mapping : rawMappings) {
                        String targetModule = (String) mapping.get("module");
                        MsaScanner.ModuleInfo mod = modules.stream()
                                .filter(m -> m.getId().equals(targetModule))
                                .findFirst().orElse(null);
                        if (mod != null) {
                            mapping.put("status", processManager.getStatus(mod.getId(), mod.getPort()));
                            mapping.put("port", mod.getPort());
                        } else {
                            mapping.put("status", "unknown");
                        }
                        mappings.add(new java.util.LinkedHashMap<>(mapping));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mappings;
    }

    @ResponseBody
    @PostMapping("/api/modules/{id}/start")
    public Map<String, Object> startModule(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        List<MsaScanner.ModuleInfo> modules = scanner.scan();
        MsaScanner.ModuleInfo mod = modules.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
        if (mod != null) {
            if (!mod.isJavaRunnable()) {
                result.put("status", "error");
                result.put("message", "등록된 모듈이지만 실행 가능한 JAR를 찾지 못했습니다.");
                return result;
            }
            processManager.startModule(mod);
            result.put("status", "ok");
        } else {
            result.put("status", "error");
            result.put("message", "Module not found");
        }
        return result;
    }

    @ResponseBody
    @PostMapping("/api/modules/{id}/stop")
    public Map<String, Object> stopModule(@PathVariable String id) {
        // Find module to get its port for robust killing
        MsaScanner.ModuleInfo mod = scanner.scan().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (mod != null) {
            processManager.stopAllInstances(mod);
        } else {
            processManager.stopModule(id, null);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

    @ResponseBody
    @PostMapping("/api/modules/{id}/restart")
    public Map<String, Object> restartModule(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        MsaScanner.ModuleInfo mod = scanner.scan().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (mod == null) {
            result.put("status", "error");
            result.put("message", "Module not found");
            return result;
        }
        if (!mod.isJavaRunnable()) {
            result.put("status", "error");
            result.put("message", "등록된 모듈이지만 실행 가능한 JAR를 찾지 못했습니다.");
            return result;
        }

        processManager.restartModule(mod);
        result.put("status", "ok");
        return result;
    }

    @ResponseBody
    @PostMapping("/api/modules/{id}/deploy-restart")
    public Map<String, Object> deployRestartModule(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        MsaScanner.ModuleInfo mod = scanner.scan().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (mod == null) {
            result.put("status", "error");
            result.put("message", "Module not found");
            return result;
        }
        String deployResult = processManager.deployAndRestartModule(mod);
        if ("ok".equals(deployResult)) {
            result.put("status", "ok");
            return result;
        }
        result.put("status", "error");
        result.put("message", deployResult);
        return result;
    }

    @ResponseBody
    @PostMapping("/api/modules/{id}/build-deploy-restart")
    public Map<String, Object> buildDeployRestartModule(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        MsaScanner.ModuleInfo mod = scanner.scan().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (mod == null) {
            result.put("status", "error");
            result.put("message", "Module not found");
            return result;
        }
        String opResult = processManager.buildDeployAndRestartModule(mod);
        if ("ok".equals(opResult)) {
            result.put("status", "ok");
            return result;
        }
        result.put("status", "error");
        result.put("message", opResult);
        return result;
    }

    @ResponseBody
    @PostMapping("/api/modules/{id}/build-deploy-zerodowntime")
    public Map<String, Object> buildDeployZeroDowntimeModule(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        MsaScanner.ModuleInfo mod = scanner.scan().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (mod == null) {
            result.put("status", "error");
            result.put("message", "Module not found");
            return result;
        }
        String opResult = processManager.buildDeployZeroDowntimeModule(mod);
        if ("ok".equals(opResult)) {
            result.put("status", "ok");
            return result;
        }
        result.put("status", "error");
        result.put("message", opResult);
        return result;
    }

    @ResponseBody
    @PostMapping("/api/modules/{id}/deploy-zerodowntime")
    public Map<String, Object> deployZeroDowntimeModule(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        MsaScanner.ModuleInfo mod = scanner.scan().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (mod == null) {
            result.put("status", "error");
            result.put("message", "Module not found");
            return result;
        }
        String opResult = processManager.deployZeroDowntimeModule(mod);
        if ("ok".equals(opResult)) {
            result.put("status", "ok");
            return result;
        }
        result.put("status", "error");
        result.put("message", opResult);
        return result;
    }

    @ResponseBody
    @GetMapping("/api/modules/{id}/logs")
    public Map<String, Object> getLogs(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        List<MsaScanner.ModuleInfo> modules = scanner.scan();
        MsaScanner.ModuleInfo mod = modules.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);

        if (mod != null) {
            result.put("logs", processManager.getLogs(id, mod.getDir(), mod.getPort()));
        } else {
            result.put("logs", java.util.Collections.singletonList("Module info not found"));
        }
        return result;
    }

    @ResponseBody
    @GetMapping("/api/changes")
    public List<Map<String, Object>> getChanges(@RequestParam(required = false) String from,
                                                 @RequestParam(required = false) String to) {
        return changeMonitorService.getHistory(parseTimeParam(from), parseTimeParam(to));
    }

    @ResponseBody
    @GetMapping("/api/autodeploy/status")
    public Map<String, Object> getAutoDeployStatus() {
        Map<String, Object> res = new HashMap<>();
        res.put("enabled", changeMonitorService.isAutoDeployEnabled());
        return res;
    }

    @ResponseBody
    @PostMapping("/api/autodeploy/toggle")
    public Map<String, Object> setAutoDeploy(@RequestBody Map<String, Object> req) {
        boolean enabled = Boolean.parseBoolean(String.valueOf(req.get("enabled")));
        changeMonitorService.setAutoDeployEnabled(enabled);
        Map<String, Object> res = new HashMap<>();
        res.put("status", "ok");
        res.put("enabled", enabled);
        return res;
    }

    @ResponseBody
    @GetMapping("/api/logs/modules")
    public Map<String, Object> getModuleLogs(@RequestParam(required = false) String from,
                                              @RequestParam(required = false) String to) {
        return logAnalyticsService.getModuleLogs(parseTimeParam(from), parseTimeParam(to));
    }

    @ResponseBody
    @GetMapping("/api/logs/critical")
    public List<Map<String, Object>> getCriticalLogs(@RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to) {
        return logAnalyticsService.getCriticalEvents(parseTimeParam(from), parseTimeParam(to));
    }

    @ResponseBody
    @GetMapping("/api/stats/controllers")
    public List<Map<String, Object>> getControllerStats(@RequestParam(required = false) String from,
                                                         @RequestParam(required = false) String to) {
        return logAnalyticsService.getTopControllers(parseTimeParam(from), parseTimeParam(to));
    }

    @ResponseBody
    @GetMapping("/api/stats/errors")
    public List<Map<String, Object>> getErrorStats(@RequestParam(required = false) String from,
                                                    @RequestParam(required = false) String to) {
        return logAnalyticsService.getTopErrors(parseTimeParam(from), parseTimeParam(to));
    }

    @ResponseBody
    @GetMapping("/api/security/violations")
    public Map<String, Object> getSecurityViolations() {
        return opsInsightService.getSecurityViolations();
    }

    @ResponseBody
    @GetMapping("/api/traffic/overview")
    public Map<String, Object> getTrafficOverview() {
        return opsInsightService.getTrafficOverview();
    }

    @ResponseBody
    @GetMapping("/api/accessibility/issues")
    public Map<String, Object> getAccessibilityIssues() {
        return opsInsightService.getAccessibilityIssues();
    }

    private LocalDateTime parseTimeParam(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String normalized = raw.trim().replace('T', ' ');
        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }
        try {
            return LocalDateTime.parse(normalized, LOG_TIME_FMT);
        } catch (Exception ignored) {
            return null;
        }
    }

    @ResponseBody
    @PostMapping("/api/killall")
    public Map<String, Object> killAll() {
        Map<String, Object> result = new HashMap<>();
        List<String> stopped = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        final String managerId = "EgovMsaManager";

        // 1) Stop modules tracked by this manager (graceful destroy)
        List<MsaScanner.ModuleInfo> modules = scanner.scan();
        MsaScanner.ModuleInfo managerModule = modules.stream()
                .filter(m -> managerId.equals(m.getId()))
                .findFirst()
                .orElse(null);
        for (MsaScanner.ModuleInfo mod : modules) {
            if (managerId.equals(mod.getId())) {
                continue;
            }
            String status = processManager.getStatus(mod.getId(), mod.getPort());
            if ("running".equals(status) || "starting".equals(status)) {
                processManager.stopModule(mod.getId(), mod.getPort());
                stopped.add(mod.getId());
            }
        }

        // 2) Kill remaining Spring Boot / Maven wrapper processes EXCEPT current
        // manager
        // Java 8 compatible PID retrieval
        String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        long myPid = Long.parseLong(jvmName.split("@")[0]);
        try {
            // Give some time for graceful shutdown
            Thread.sleep(1000);

            // Kill other java processes matching spring-boot:run excluding current PID
            String[] killCmds = {
                    "pgrep -f 'spring-boot:run' | grep -v " + myPid + " | xargs -r kill -9",
                    "pgrep -f 'Dspring-boot.run' | grep -v " + myPid + " | xargs -r kill -9"
            };

            for (String cmd : killCmds) {
                new ProcessBuilder("sh", "-c", cmd).start().waitFor();
            }
        } catch (Exception e) {
            errors.add("Cleanup error: " + e.getMessage());
        }

        // 3) Schedule self-reboot
        try {
            Integer managerPort = managerModule != null ? managerModule.getPort() : null;
            if (managerPort == null || managerPort == 0) {
                managerPort = 18030;
            }
            String rebootLog = "/opt/carbosys/logs/msa-manager-reboot.log";
            String rebootCmd = "sleep 3; "
                    + "if [ -f /app/EgovMsaManager.jar ]; then "
                    + "nohup java -Xms64m -Xmx192m -jar /app/EgovMsaManager.jar --server.port=" + managerPort
                    + " > " + rebootLog + " 2>&1 & "
                    + "elif [ -f /app/EgovMsaManager/target/EgovMsaManager.jar ]; then "
                    + "nohup java -Xms64m -Xmx192m -jar /app/EgovMsaManager/target/EgovMsaManager.jar --server.port="
                    + managerPort + " > " + rebootLog + " 2>&1 & "
                    + "elif [ -d /opt/carbosys/module/EgovMsaManager ]; then "
                    + "cd /opt/carbosys/module/EgovMsaManager && "
                    + "nohup mvn -DskipTests spring-boot:run "
                    + "-Dspring-boot.run.arguments=--server.port=" + managerPort
                    + " > " + rebootLog + " 2>&1 & "
                    + "fi";
            new ProcessBuilder("sh", "-c", rebootCmd).start();
        } catch (Exception e) {
            errors.add("Reboot trigger error: " + e.getMessage());
        }

        result.put("status", "ok");
        result.put("stopped", stopped);
        result.put("message", "모든 모듈을 종료했습니다. Manager 모듈은 약 20초 후 자동으로 재시작됩니다.");

        // 4) Exit this process after a short delay to allow the response to reach the
        // browser
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("[System] Self-Restart triggered. Exiting current process...");
                System.exit(0);
            } catch (Exception ignored) {
            }
        }).start();

        return result;
    }
}
