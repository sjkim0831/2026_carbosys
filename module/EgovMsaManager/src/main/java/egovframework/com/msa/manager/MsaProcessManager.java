package egovframework.com.msa.manager;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class MsaProcessManager {
    private static final String APP_ROOT = "/app";
    private final Map<String, ProcessEntry> processMap = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public static class ProcessEntry {
        public Process process;
        public List<String> logs = new CopyOnWriteArrayList<>();
        public String status = "stopped";
        public Long pid;

        public void addLog(String line) {
            logs.add(line);
            if (logs.size() > 500)
                logs.remove(0);
        }
    }

    public synchronized void startModule(MsaScanner.ModuleInfo mod) {
        if (processMap.containsKey(mod.getId()))
            return;

        ProcessEntry entry = new ProcessEntry();
        entry.status = "starting";
        processMap.put(mod.getId(), entry);

        new Thread(() -> {
            try {
                String jarArg = resolveRunnableJar(mod);
                List<String> cmd = new ArrayList<>(
                        Arrays.asList("java", "-Xms64m", "-Xmx192m", "-jar", jarArg));
                // Enforce central port if registered
                if (mod.getPort() != null && mod.getPort() != 0) {
                    cmd.add("--server.port=" + mod.getPort());
                }

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(new File(APP_ROOT));
                pb.redirectErrorStream(true);
                pb.environment().put("JAVA_OPTS", "-Djava.awt.headless=true");

                Process proc = pb.start();
                entry.process = proc;
                entry.pid = getPid(proc);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        entry.addLog(line);
                        appendToStartupLog(mod.getDir(), line);

                        if (line.contains("Started ") && line.contains("seconds")) {
                            entry.status = "running";
                        }
                    }
                }
                proc.waitFor();
            } catch (Exception e) {
                entry.addLog("Error: " + e.getMessage());
                entry.status = "error";
            } finally {
                entry.status = "stopped";
                processMap.remove(mod.getId());
            }
        }).start();
    }

    public synchronized void restartModule(MsaScanner.ModuleInfo mod) {
        stopModule(mod.getId(), mod.getPort());
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        startModule(mod);
    }

    public synchronized String deployAndRestartModule(MsaScanner.ModuleInfo mod) {
        if (!mod.isJavaRunnable()) {
            return "이 모듈은 Java 실행 대상이 아닙니다.";
        }
        Path sourceJar = Paths.get(mod.getDir(), "target", mod.getArtifactId() + ".jar");
        if (!Files.exists(sourceJar)) {
            return "배포 실패: 소스 JAR 없음 - " + sourceJar;
        }

        Path appJar = Paths.get(APP_ROOT, mod.getArtifactId() + ".jar");
        Path appTargetDir = Paths.get(APP_ROOT, mod.getArtifactId(), "target");
        Path appTargetJar = appTargetDir.resolve(mod.getArtifactId() + ".jar");

        try {
            Files.createDirectories(appTargetDir);
            Files.copy(sourceJar, appJar, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.deleteIfExists(appTargetJar);
                Files.createSymbolicLink(appTargetJar, appJar);
            } catch (Exception symlinkErr) {
                // If symlink is unavailable, copy a physical file as fallback.
                Files.copy(appJar, appTargetJar, StandardCopyOption.REPLACE_EXISTING);
            }
            restartModule(mod);
            return "ok";
        } catch (Exception e) {
            return "배포 실패: " + e.getMessage();
        }
    }

    public synchronized String buildDeployAndRestartModule(MsaScanner.ModuleInfo mod) {
        if (!mod.isJavaRunnable()) {
            return "이 모듈은 Java 실행 대상이 아닙니다.";
        }

        File moduleDir = new File(mod.getDir());
        if (!moduleDir.exists() || !moduleDir.isDirectory()) {
            return "빌드 실패: 모듈 폴더를 찾을 수 없습니다 - " + mod.getDir();
        }
        if (!new File(moduleDir, "pom.xml").exists()) {
            return "빌드 실패: pom.xml이 없습니다 - " + mod.getDir();
        }

        // Build from bind-mounted source folder
        List<String> buildCmd = Arrays.asList("sh", "-lc", "mvn -DskipTests package");
        try {
            ProcessBuilder pb = new ProcessBuilder(buildCmd);
            pb.directory(moduleDir);
            pb.redirectErrorStream(true);
            forceJdkForBuild(pb);
            Process p = pb.start();
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
            int rc = p.waitFor();
            // Persist build logs in module startup.log tail for quick troubleshooting
            for (String l : lines) {
                appendToStartupLog(mod.getDir(), "[BUILD] " + l);
            }
            if (rc != 0) {
                return "빌드 실패: mvn 종료코드 " + rc;
            }
        } catch (Exception e) {
            return "빌드 실패: " + e.getMessage();
        }

        return deployAndRestartModule(mod);
    }

    private void forceJdkForBuild(ProcessBuilder pb) {
        // Some base images run manager with JAVA_HOME pointing to JRE.
        // Build must run with JDK (javac/tools available).
        String[] candidates = {
                "/usr/lib/jvm/java-21-openjdk-amd64",
                "/usr/lib/jvm/java-17-openjdk-amd64",
                "/usr/lib/jvm/default-java"
        };
        for (String home : candidates) {
            File javac = new File(home + "/bin/javac");
            if (javac.exists()) {
                Map<String, String> env = pb.environment();
                env.put("JAVA_HOME", home);
                String path = env.getOrDefault("PATH", "");
                env.put("PATH", home + "/bin" + (path.isEmpty() ? "" : ":" + path));
                return;
            }
        }
    }

    private void appendToStartupLog(String dir, String line) {
        try (FileWriter fw = new FileWriter(new File(dir, "startup.log"), true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            // ignore
        }
    }

    public void stopModule(String id, Integer port) {
        ProcessEntry entry = processMap.get(id);
        if (entry != null && entry.process != null) {
            entry.process.destroy();
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if (entry.process.isAlive())
                        entry.process.destroyForcibly();
                } catch (Exception e) {
                }
            }).start();
        }

        // Fallback: Kill by port if the process wasn't started by this manager or is
        // hanging
        if (port != null && port != 0) {
            try {
                // Minimal environments often don't include fuser/lsof.
                // Kill by Spring port arg pattern first, then force kill if still alive.
                String killCmd = "pids=$(ps -ef | grep -- '--server.port=" + port
                        + "' | grep -v grep | awk '{print $2}' || true); "
                        + "if [ -n \"$pids\" ]; then kill -15 $pids || true; sleep 1; "
                        + "for p in $pids; do kill -0 $p 2>/dev/null && kill -9 $p || true; done; fi";
                new ProcessBuilder("sh", "-c", killCmd).start().waitFor();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public List<String> getLogs(String id, String dir, Integer port) {
        ProcessEntry entry = processMap.get(id);
        if (entry != null) {
            return new ArrayList<>(entry.logs);
        }

        List<String> extLogs = new ArrayList<>();
        File logFile = new File(dir, "startup.log");
        if (logFile.exists()) {
            extLogs.add("[System] Reading from existing startup.log...");
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))) {
                List<String> allLines = br.lines().collect(Collectors.toList());
                int start = Math.max(0, allLines.size() - 200);
                extLogs.addAll(allLines.subList(start, allLines.size()));
                return extLogs;
            } catch (Exception e) {
                extLogs.add("Error reading log file: " + e.getMessage());
            }
        }

        if (port != null && port != 0) {
            try {
                String url = "http://localhost:" + port + "/actuator/logfile";
                String actuatorLogs = restTemplate.getForObject(url, String.class);
                if (actuatorLogs != null) {
                    extLogs.add("[System] Fetched from Actuator (Port " + port + ")");
                    String[] lines = actuatorLogs.split("\n");
                    int start = Math.max(0, lines.length - 200);
                    for (int i = start; i < lines.length; i++)
                        extLogs.add(lines[i]);
                    return extLogs;
                }
            } catch (Exception e) {
            }
        }
        return extLogs;
    }

    public String getStatus(String id, Integer port) {
        if (processMap.containsKey(id)) {
            return processMap.get(id).status;
        }
        if (port != null && port != 0) {
            if (isPortInUse(port))
                return "running";
        }
        return "stopped";
    }

    public Long getPid(String id) {
        ProcessEntry entry = processMap.get(id);
        return entry != null ? entry.pid : null;
    }

    private boolean isPortInUse(int port) {
        try (Socket s = new Socket("localhost", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Long getPid(Process p) {
        try {
            java.lang.reflect.Field field = p.getClass().getDeclaredField("pid");
            field.setAccessible(true);
            return (Long) field.get(p);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveRunnableJar(MsaScanner.ModuleInfo mod) {
        Path appTargetJar = Paths.get(APP_ROOT, mod.getArtifactId(), "target", mod.getArtifactId() + ".jar");
        if (Files.exists(appTargetJar)) {
            return appTargetJar.toString();
        }
        Path appJar = Paths.get(APP_ROOT, mod.getArtifactId() + ".jar");
        if (Files.exists(appJar)) {
            return appJar.toString();
        }
        // Fallback to mounted source tree jar.
        return Paths.get(mod.getDir(), "target", mod.getArtifactId() + ".jar").toString();
    }
}
