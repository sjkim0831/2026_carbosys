package egovframework.com.msa.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
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

    private final MsaScanner scanner = new MsaScanner();
    private static final String MAPPING_FILE = "/app/msa-mappings.yml";

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
                result.put("message", "이 모듈은 Java 실행 대상이 아닙니다 (폴더만 감지됨)");
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
            result.put("message", "이 모듈은 Java 실행 대상이 아닙니다 (수동관리)");
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
    @PostMapping("/api/killall")
    public Map<String, Object> killAll() {
        Map<String, Object> result = new HashMap<>();
        List<String> stopped = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 1) Stop modules tracked by this manager (graceful destroy)
        List<MsaScanner.ModuleInfo> modules = scanner.scan();
        for (MsaScanner.ModuleInfo mod : modules) {
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
            // Background script: wait for port release, then restart manager
            String rebootCmd = "sleep 3; cd /opt/carbosys/EgovMsaManager; nohup mvn spring-boot:run > /opt/carbosys/EgovMsaManager/reboot_manager.log 2>&1 &";
            new ProcessBuilder("sh", "-c", rebootCmd).start();
        } catch (Exception e) {
            errors.add("Reboot trigger error: " + e.getMessage());
        }

        result.put("status", "ok");
        result.put("stopped", stopped);
        result.put("message", "모든 모듈을 종료했습니다. Manager(18030) 모듈은 약 20초 후 자동으로 재시작됩니다.");

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
