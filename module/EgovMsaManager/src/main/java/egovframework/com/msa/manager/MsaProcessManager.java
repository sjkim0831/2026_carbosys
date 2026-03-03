package egovframework.com.msa.manager;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
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
    private static final String DEFAULT_XMS = "64m";
    private static final String DEFAULT_XMX = "256m";
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
                List<String> cmd = new ArrayList<>(Arrays.asList(
                        "java",
                        "-Xms" + resolveXms(mod),
                        "-Xmx" + resolveXmx(mod),
                        "-jar",
                        jarArg));
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
        String deployResult = deployModuleJar(mod);
        if (!"ok".equals(deployResult)) {
            return deployResult;
        }
        restartModule(mod);
        return "ok";
    }

    public synchronized String buildDeployAndRestartModule(MsaScanner.ModuleInfo mod) {
        if (!mod.isJavaRunnable()) {
            return "이 모듈은 Java 실행 대상이 아닙니다.";
        }

        String buildResult = buildModule(mod);
        if (!"ok".equals(buildResult)) {
            return buildResult;
        }

        return deployAndRestartModule(mod);
    }

    public synchronized String buildDeployZeroDowntimeModule(MsaScanner.ModuleInfo mod) {
        if (!mod.isJavaRunnable()) {
            return "이 모듈은 Java 실행 대상이 아닙니다.";
        }
        if (mod.getPort() == null || mod.getPort() == 0) {
            return "무중단 배포 실패: 기본 포트가 설정되어 있지 않습니다.";
        }

        String buildResult = buildModule(mod);
        if (!"ok".equals(buildResult)) {
            return buildResult;
        }
        return deployZeroDowntimeModule(mod);
    }

    public synchronized String deployZeroDowntimeModule(MsaScanner.ModuleInfo mod) {
        if (!mod.isJavaRunnable()) {
            return "이 모듈은 Java 실행 대상이 아닙니다.";
        }
        if (mod.getPort() == null || mod.getPort() == 0) {
            return "무중단 배포 실패: 기본 포트가 설정되어 있지 않습니다.";
        }

        String deployResult = deployModuleJar(mod);
        if (!"ok".equals(deployResult)) {
            return deployResult;
        }

        int basePort = mod.getPort();
        int shadowPort = pickShadowPort(basePort);
        if (shadowPort <= 0) {
            return "무중단 배포 실패: 임시 포트를 찾지 못했습니다.";
        }

        Process shadowProc = null;
        try {
            appendToStartupLog(mod.getDir(), "[ZD] shadow start: port=" + shadowPort);
            shadowProc = startUntrackedProcess(mod, shadowPort, "[ZD-SHADOW]");

            if (!waitForHealthy(shadowPort, 70000)) {
                if (shadowProc != null && shadowProc.isAlive()) {
                    shadowProc.destroyForcibly();
                }
                return "무중단 배포 실패: 임시 포트(" + shadowPort + ") 헬스체크 실패";
            }

            appendToStartupLog(mod.getDir(), "[ZD] shadow healthy: port=" + shadowPort);
            stopModule(mod.getId(), basePort);
            waitUntilPortClosed(basePort, 20000);

            // Avoid start race when old tracked entry is not yet removed from processMap.
            waitUntilEntryCleared(mod.getId(), 10000);
            startModule(mod);

            if (!waitForHealthy(basePort, 70000)) {
                if (shadowProc != null && shadowProc.isAlive()) {
                    appendToStartupLog(mod.getDir(), "[ZD] rollback: base unhealthy, shadow kept alive");
                    return "무중단 배포 실패: 기본 포트(" + basePort + ") 재기동 헬스체크 실패";
                }
                return "무중단 배포 실패: 기본 포트 헬스체크 실패";
            }

            // Promotion completed, stop shadow instance.
            stopByPort(shadowPort);
            appendToStartupLog(mod.getDir(), "[ZD] completed: base=" + basePort + ", shadowStopped=" + shadowPort);
            return "ok";
        } catch (Exception e) {
            if (shadowProc != null && shadowProc.isAlive()) {
                try {
                    shadowProc.destroyForcibly();
                } catch (Exception ignored) {
                }
            }
            return "무중단 배포 실패: " + e.getMessage();
        }
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

    private String buildModule(MsaScanner.ModuleInfo mod) {
        File moduleDir = new File(mod.getDir());
        if (!moduleDir.exists() || !moduleDir.isDirectory()) {
            return "빌드 실패: 모듈 폴더를 찾을 수 없습니다 - " + mod.getDir();
        }
        if (!new File(moduleDir, "pom.xml").exists()) {
            return "빌드 실패: pom.xml이 없습니다 - " + mod.getDir();
        }

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
            for (String l : lines) {
                appendToStartupLog(mod.getDir(), "[BUILD] " + l);
            }
            if (rc != 0) {
                return "빌드 실패: mvn 종료코드 " + rc;
            }
            return "ok";
        } catch (Exception e) {
            return "빌드 실패: " + e.getMessage();
        }
    }

    private String deployModuleJar(MsaScanner.ModuleInfo mod) {
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
                Files.copy(appJar, appTargetJar, StandardCopyOption.REPLACE_EXISTING);
            }
            return "ok";
        } catch (Exception e) {
            return "배포 실패: " + e.getMessage();
        }
    }

    private Process startUntrackedProcess(MsaScanner.ModuleInfo mod, int port, String logPrefix) throws IOException {
        String jarArg = resolveRunnableJar(mod);
        List<String> cmd = new ArrayList<>(Arrays.asList(
                "java",
                "-Xms" + resolveXms(mod),
                "-Xmx" + resolveXmx(mod),
                "-jar",
                jarArg,
                "--server.port=" + port));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(APP_ROOT));
        pb.redirectErrorStream(true);
        pb.environment().put("JAVA_OPTS", "-Djava.awt.headless=true");
        Process proc = pb.start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    appendToStartupLog(mod.getDir(), logPrefix + " " + line);
                }
            } catch (Exception e) {
                appendToStartupLog(mod.getDir(), logPrefix + " log-reader-error: " + e.getMessage());
            }
        }).start();

        return proc;
    }

    private int pickShadowPort(int basePort) {
        int[] preferred = new int[] { basePort + 1000, basePort + 2000, basePort + 3000 };
        for (int p : preferred) {
            if (p > 0 && !isPortInUse(p)) {
                return p;
            }
        }
        for (int p = 20000; p <= 20999; p++) {
            if (!isPortInUse(p)) {
                return p;
            }
        }
        return -1;
    }

    private void stopByPort(int port) {
        try {
            String killCmd = "pids=$(ps -ef | grep -- '--server.port=" + port
                    + "' | grep -v grep | awk '{print $2}' || true); "
                    + "if [ -n \"$pids\" ]; then kill -15 $pids || true; sleep 1; "
                    + "for p in $pids; do kill -0 $p 2>/dev/null && kill -9 $p || true; done; fi";
            new ProcessBuilder("sh", "-c", killCmd).start().waitFor();
        } catch (Exception ignored) {
        }
    }

    private void waitUntilPortClosed(int port, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (!isPortInUse(port)) {
                return;
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void waitUntilEntryCleared(String id, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (!processMap.containsKey(id)) {
                return;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean waitForHealthy(int port, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        int stable = 0;
        while (System.currentTimeMillis() < end) {
            // Security filters can block actuator URL checks in some services.
            // Use stable TCP listen checks to avoid false negatives/noisy 403 logs.
            if (isPortInUse(port)) {
                stable++;
                if (stable >= 3) {
                    return true;
                }
            } else {
                stable = 0;
            }
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
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

    public void stopAllInstances(MsaScanner.ModuleInfo mod) {
        // Stop tracked/base instance first.
        stopModule(mod.getId(), mod.getPort());

        // Kill any remaining same-module java instances (e.g., shadow port instance).
        try {
            String artifact = mod.getArtifactId().replaceAll("[^A-Za-z0-9._-]", "");
            String p1 = "/app/" + artifact + "/target/" + artifact + ".jar";
            String p2 = "/app/" + artifact + ".jar";
            String killCmd = "pids1=$(ps -eo pid,args | awk '$2==\"java\" && index($0,\"" + p1
                    + "\")>0 {print $1}'); "
                    + "pids2=$(ps -eo pid,args | awk '$2==\"java\" && index($0,\"" + p2 + "\")>0 {print $1}'); "
                    + "pids=\"$pids1 $pids2\"; "
                    + "if [ -n \"$pids\" ]; then kill -15 $pids 2>/dev/null || true; sleep 1; "
                    + "for p in $pids; do kill -0 $p 2>/dev/null && kill -9 $p 2>/dev/null || true; done; fi";
            new ProcessBuilder("sh", "-c", killCmd).start().waitFor();
        } catch (Exception ignored) {
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

    private String resolveXms(MsaScanner.ModuleInfo mod) {
        String moduleId = mod == null ? "" : mod.getId();
        String byModule = readHeapEnv("MSA_JVM_" + normalizeModuleKey(moduleId) + "_XMS");
        if (byModule != null) {
            return byModule;
        }
        String global = readHeapEnv("MSA_JVM_DEFAULT_XMS");
        if (global != null) {
            return global;
        }
        if ("EgovMsaManager".equals(moduleId)) {
            return "256m";
        }
        if ("EgovHome".equals(moduleId)) {
            return "128m";
        }
        return DEFAULT_XMS;
    }

    private String resolveXmx(MsaScanner.ModuleInfo mod) {
        String moduleId = mod == null ? "" : mod.getId();
        String byModule = readHeapEnv("MSA_JVM_" + normalizeModuleKey(moduleId) + "_XMX");
        if (byModule != null) {
            return byModule;
        }
        String global = readHeapEnv("MSA_JVM_DEFAULT_XMX");
        if (global != null) {
            return global;
        }
        if ("EgovMsaManager".equals(moduleId)) {
            return "512m";
        }
        if ("EgovHome".equals(moduleId)) {
            return "384m";
        }
        return DEFAULT_XMX;
    }

    private String normalizeModuleKey(String moduleId) {
        if (moduleId == null) {
            return "";
        }
        return moduleId.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String readHeapEnv(String key) {
        try {
            String v = System.getenv(key);
            if (v == null || v.trim().isEmpty()) {
                return null;
            }
            String s = v.trim().toLowerCase(Locale.ROOT);
            if (!s.matches("\\d+[mg]")) {
                return null;
            }
            return s;
        } catch (Exception ignored) {
            return null;
        }
    }
}
