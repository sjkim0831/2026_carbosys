package egovframework.com.msa.manager;

import lombok.Builder;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MsaScanner {
    private static final String ROOT_PATH = "/opt/carbosys";
    private static final String PORT_REGISTRY = "/opt/carbosys/msa-ports.yml";

    @Data
    @Builder
    public static class ModuleInfo {
        private String id;
        private String name;
        private String dir;
        private Integer port;
        private String artifactId;
        private boolean registered; // Indicates if port comes from the central registry
    }

    public List<ModuleInfo> scan() {
        List<ModuleInfo> modules = new ArrayList<>();
        Map<String, Integer> registry = loadPortRegistry();

        File root = new File(ROOT_PATH);
        if (!root.exists() || !root.isDirectory())
            return modules;

        File[] entries = root.listFiles();
        if (entries == null)
            return modules;

        for (File entry : entries) {
            if (entry.isDirectory()) {
                File pom = new File(entry, "pom.xml");
                File src = new File(entry, "src/main");
                if (pom.exists() && src.exists()) {
                    modules.add(parseModule(entry, registry));
                }
            }
        }
        return modules;
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

    private ModuleInfo parseModule(File dir, Map<String, Integer> registry) {
        String id = dir.getName();
        String artifactId = id;
        String appName = id;
        Integer port = registry.get(id);
        boolean registered = port != null;

        // If not in registry, try parsing application.yml as fallback
        if (port == null) {
            try {
                File yml = new File(dir, "src/main/resources/application.yml");
                if (!yml.exists())
                    yml = new File(dir, "src/main/resources/application.yaml");

                if (yml.exists()) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> obj = yaml.load(new FileInputStream(yml));

                    if (obj.get("server") instanceof Map) {
                        Map<String, Object> server = (Map<String, Object>) obj.get("server");
                        if (server.get("port") != null) {
                            port = Integer.parseInt(server.get("port").toString());
                        }
                    }

                    if (obj.get("spring") instanceof Map) {
                        Map<String, Object> spring = (Map<String, Object>) obj.get("spring");
                        if (spring.get("application") instanceof Map) {
                            Map<String, Object> app = (Map<String, Object>) spring.get("application");
                            if (app.get("name") != null) {
                                appName = app.get("name").toString();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return ModuleInfo.builder()
                .id(id)
                .name(appName)
                .dir(dir.getAbsolutePath())
                .port(port)
                .artifactId(artifactId)
                .registered(registered)
                .build();
    }
}
