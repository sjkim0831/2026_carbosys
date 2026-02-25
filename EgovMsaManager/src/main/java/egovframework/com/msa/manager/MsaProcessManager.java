package egovframework.com.msa.manager;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class MsaProcessManager {
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
                List<String> cmd = new ArrayList<>(
                        Arrays.asList("java", "-jar", "target/" + mod.getArtifactId() + ".jar"));
                // Enforce central port if registered
                if (mod.getPort() != null && mod.getPort() != 0) {
                    cmd.add("--server.port=" + mod.getPort());
                }

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(new File(mod.getDir()));
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

    private void appendToStartupLog(String dir, String line) {
        try (FileWriter fw = new FileWriter(new File(dir, "startup.log"), true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            // ignore
        }
    }

    public void stopModule(String id) {
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
}
