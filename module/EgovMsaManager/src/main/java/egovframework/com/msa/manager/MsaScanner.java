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
            "EurekaServer", "ConfigServer", "GatewayServer", "EgovMsaManager");

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
        Map<String, ModuleInfo> modules = new LinkedHashMap<>();
        Map<String, Integer> registry = loadPortRegistry();

        // 1) Prefer mounted module folders so list reflects actual mapped directories.
        File moduleRoot = new File(MODULE_ROOT);
        if (moduleRoot.exists() && moduleRoot.isDirectory()) {
            File[] dirs = moduleRoot.listFiles(File::isDirectory);
            if (dirs != null) {
                for (File dir : dirs) {
                    String id = dir.getName();
                    if (id.startsWith(".") || INFRA_MODULES.contains(id))
                        continue;

                    boolean hasPom = new File(dir, "pom.xml").exists();
                    boolean hasNodePackage = new File(dir, "package.json").exists();
                    boolean hasJarInModule = new File(dir, "target/" + id + ".jar").exists();
                    boolean hasJarInApp = new File(ROOT_PATH, id + ".jar").exists()
                            || new File(ROOT_PATH, id + "/target/" + id + ".jar").exists();
                    boolean javaRunnable = hasJarInModule || hasJarInApp;
                    Integer port = registry.get(id);

                    // Include if it looks like a module folder or is registered centrally.
                    if (hasPom || hasNodePackage || port != null || javaRunnable) {
                        modules.put(id, ModuleInfo.builder()
                                .id(id)
                                .name(id)
                                .dir(dir.getAbsolutePath())
                                .port(port)
                                .artifactId(id)
                                .registered(port != null)
                                .javaRunnable(javaRunnable)
                                .build());
                    }
                }
            }
        }

        // 2) Add registry-only modules not found in mapped folder.
        for (Map.Entry<String, Integer> entry : registry.entrySet()) {
            String id = entry.getKey();
            if (INFRA_MODULES.contains(id))
                continue;
            if (modules.containsKey(id))
                continue;

            boolean hasJarInApp = new File(ROOT_PATH, id + ".jar").exists()
                    || new File(ROOT_PATH, id + "/target/" + id + ".jar").exists();
            modules.put(id, ModuleInfo.builder()
                    .id(id)
                    .name(id)
                    .dir(new File(ROOT_PATH, id).getAbsolutePath())
                    .port(entry.getValue())
                    .artifactId(id)
                    .registered(true)
                    .javaRunnable(hasJarInApp)
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
