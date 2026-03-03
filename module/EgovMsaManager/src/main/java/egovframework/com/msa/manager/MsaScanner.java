package egovframework.com.msa.manager;

import lombok.Builder;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MsaScanner {
    private static final String ROOT_PATH = "/app";
    private static final String MODULE_ROOT = "/opt/carbosys/module";
    private static final String PORT_REGISTRY = "/app/msa-ports.yml";
    private static final List<String> INFRA_MODULES = Arrays.asList(
            "EurekaServer", "ConfigServer", "GatewayServer");

    @Data
    @Builder
    public static class ModuleInfo {
        private String id;
        private String name;
        private String dir;
        private Integer port;
        private String artifactId;
        private boolean registered; // Indicates if port comes from the central registry
        private boolean javaRunnable;
    }

    public List<ModuleInfo> scan() {
        Map<String, Integer> registry = loadPortRegistry();
        Map<String, ModuleInfo> modules = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : registry.entrySet()) {
            String id = entry.getKey();
            if (INFRA_MODULES.contains(id))
                continue;

            File moduleDir = new File(MODULE_ROOT, id);
            String dirPath = moduleDir.exists()
                    ? moduleDir.getAbsolutePath()
                    : new File(ROOT_PATH, id).getAbsolutePath();
            boolean hasJarInModule = new File(moduleDir, "target/" + id + ".jar").exists();
            boolean hasJarInApp = new File(ROOT_PATH, id + ".jar").exists()
                    || new File(ROOT_PATH, id + "/target/" + id + ".jar").exists();
            boolean javaRunnable = hasJarInModule || hasJarInApp;

            modules.put(id, ModuleInfo.builder()
                    .id(id)
                    .name(id)
                    .dir(dirPath)
                    .port(entry.getValue())
                    .artifactId(id)
                    .registered(true)
                    .javaRunnable(javaRunnable)
                    .build());
        }

        return new ArrayList<>(modules.values());
    }

    private Map<String, Integer> loadPortRegistry() {
        Map<String, Integer> ports = new HashMap<>();
        try {
            File file = new File(PORT_REGISTRY);
            if (file.exists()) {
                Yaml yaml = new Yaml();
                Map<String, Object> obj = yaml.load(new FileInputStream(file));
                if (obj.get("ports") instanceof Map) {
                    Map<String, Object> pMap = (Map<String, Object>) obj.get("ports");
                    for (Map.Entry<String, Object> entry : pMap.entrySet()) {
                        ports.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load port registry: " + e.getMessage());
        }
        return ports;
    }

}
