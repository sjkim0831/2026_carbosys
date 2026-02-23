import { spawn } from 'child_process';
import treeKill from 'tree-kill';
import { EventEmitter } from 'events';
import net from 'net';

export const processMap = new Map(); // id -> { process, logs }
export const events = new EventEmitter();

export async function getActualPort(pid, configuredPort) {
    if (configuredPort && configuredPort !== 0) return configuredPort;
    return new Promise((resolve) => {
        setTimeout(async () => {
            try {
                const { execSync } = await import('child_process');
                const out = execSync(`ss -tlnp | grep pid=${pid}`, { timeout: 2000 }).toString();
                const match = out.match(/:(\d+)\s/);
                resolve(match ? parseInt(match[1]) : null);
            } catch {
                resolve(null);
            }
        }, 3000);
    });
}


export function startModule(mod) {
    if (processMap.has(mod.id)) return { ok: false, msg: '이미 실행 중입니다' };

    const logs = [];
    const proc = spawn('mvn', ['spring-boot:run'], {
        cwd: mod.dir,
        shell: true,
        env: { ...process.env, MAVEN_OPTS: '-Djava.awt.headless=true' },
    });

    processMap.set(mod.id, { process: proc, logs, startedAt: new Date().toISOString() });
    events.emit('status', { id: mod.id, status: 'starting', pid: proc.pid });

    const onData = (stream) => (data) => {
        const line = data.toString();
        logs.push(line);
        if (logs.length > 500) logs.shift();
        events.emit('log', { id: mod.id, line });

        // Detect started
        if (line.includes('Started ') && line.includes('seconds')) {
            events.emit('status', { id: mod.id, status: 'running', pid: proc.pid });
        }
        if (line.includes('APPLICATION FAILED TO START') || line.includes('Error starting')) {
            events.emit('status', { id: mod.id, status: 'error', pid: proc.pid });
        }
    };

    proc.stdout.on('data', onData('stdout'));
    proc.stderr.on('data', onData('stderr'));

    proc.on('close', (code) => {
        processMap.delete(mod.id);
        events.emit('status', { id: mod.id, status: 'stopped', pid: null });
    });

    return { ok: true, pid: proc.pid };
}

export function stopModule(id) {
    const entry = processMap.get(id);
    if (!entry) return { ok: false, msg: '실행 중이 아닙니다' };
    treeKill(entry.process.pid, 'SIGTERM');
    return { ok: true };
}

export function getModuleLogs(id, lines = 100) {
    const entry = processMap.get(id);
    if (!entry) return [];
    return entry.logs.slice(-lines);
}

export function getRunningIds() {
    return [...processMap.keys()];
}

export async function checkPort(port) {
    return new Promise((resolve) => {
        const server = net.createServer();
        server.listen(port, () => { server.close(); resolve(false); });
        server.on('error', () => resolve(true));
    });
}
