#!/usr/bin/env node
/**
 * MSA Manager - CLI TUI
 * 방향키로 모듈 선택, Enter로 상세, s=시작, x=중지, q=종료, l=로그
 */
import { scanModules } from './scanner.js';
import { startModule, stopModule, getModuleLogs, processMap, events } from './process-manager.js';
import readline from 'readline';

// ─── ANSI helpers ────────────────────────────────────────────────────────────
const ESC = '\x1b[';
const reset = '\x1b[0m';
const bold = '\x1b[1m';
const dim = '\x1b[2m';
const cls = () => process.stdout.write('\x1b[2J\x1b[H');
const mv = (r, c) => process.stdout.write(`${ESC}${r};${c}H`);
const clrLine = () => process.stdout.write(ESC + 'K');
const color = {
    blue: (s) => `\x1b[38;5;75m${s}${reset}`,
    green: (s) => `\x1b[38;5;84m${s}${reset}`,
    yellow: (s) => `\x1b[38;5;220m${s}${reset}`,
    red: (s) => `\x1b[38;5;203m${s}${reset}`,
    cyan: (s) => `\x1b[38;5;80m${s}${reset}`,
    gray: (s) => `\x1b[38;5;244m${s}${reset}`,
    white: (s) => `\x1b[97m${s}${reset}`,
    bg: (s) => `\x1b[48;5;236m${s}${reset}`,
    bgBlue: (s) => `\x1b[48;5;27m${s}${reset}`,
    bgDark: (s) => `\x1b[48;5;234m\x1b[97m${s}${reset}`,
};

// ─── State ───────────────────────────────────────────────────────────────────
let modules = [];
let cursor = 0;
let mode = 'list'; // 'list' | 'log'
let logLines = [];
let logScroll = 0;
let statusMap = {};
let message = '';
let msgTimer = null;

// ─── Terminal setup ───────────────────────────────────────────────────────────
const { columns: COLS = 120, rows: ROWS = 40 } = process.stdout;
readline.emitKeypressEvents(process.stdin);
process.stdin.setRawMode(true);
process.stdin.resume();
process.stdin.setEncoding('utf8');

// ─── Helpers ─────────────────────────────────────────────────────────────────
function padEnd(s, n) {
    const plain = stripAnsi(s);
    const pad = Math.max(0, n - plain.length);
    return s + ' '.repeat(pad);
}
function stripAnsi(s) { return s.replace(/\x1b\[[0-9;]*m/g, ''); }
function truncate(s, n) { return s.length > n ? s.slice(0, n - 1) + '…' : s; }

function statusBar(s) {
    if (s === 'running') return color.green('● RUNNING ');
    if (s === 'starting') return color.yellow('◌ STARTING');
    if (s === 'error') return color.red('✖ ERROR   ');
    if (s === 'stopping') return color.yellow('◌ STOPPING');
    return color.gray('○ STOPPED ');
}

function showMessage(msg, ms = 2000) {
    message = msg;
    clearTimeout(msgTimer);
    msgTimer = setTimeout(() => { message = ''; render(); }, ms);
}

// ─── Render ───────────────────────────────────────────────────────────────────
function render() {
    if (mode === 'list') renderList();
    else if (mode === 'log') renderLog();
}

function renderList() {
    cls();
    const w = process.stdout.columns || COLS;
    const h = process.stdout.rows || ROWS;

    // Header
    const title = ' 🚀 MSA Manager';
    const subtitle = `  ${modules.length}개 모듈  |  실행 중: ${Object.values(statusMap).filter(s => s?.status === 'running').length}개`;
    process.stdout.write(color.bgDark(title.padEnd(w)) + '\n');
    process.stdout.write(color.gray(subtitle) + '\n');
    process.stdout.write(color.gray('─'.repeat(w)) + '\n');

    // Column headers
    const col = [4, 22, 22, 12, 8];
    const headers = ['#', 'ID', 'App Name', 'Port', 'Status'];
    process.stdout.write(
        dim + bold +
        color.gray(
            headers.map((h, i) => h.padEnd(col[i])).join(' ')
        ) + reset + '\n'
    );
    process.stdout.write(color.gray('─'.repeat(w)) + '\n');

    const visible = h - 8;
    const start = Math.max(0, cursor - Math.floor(visible / 2));
    const slice = modules.slice(start, start + visible);

    slice.forEach((m, i) => {
        const realIdx = start + i;
        const st = statusMap[m.id] ?? { status: 'stopped' };
        const isSelected = realIdx === cursor;
        const num = String(realIdx + 1).padEnd(col[0]);
        const id = truncate(m.id, col[1]).padEnd(col[1]);
        const name = truncate(m.name || '-', col[2]).padEnd(col[2]);
        const port = (m.port && m.port !== 0 ? ':' + m.port : 'random').padEnd(col[3]);
        const status = statusBar(st.status);

        let line = `${num} ${id} ${name} ${port} ${status}`;
        if (isSelected) {
            process.stdout.write('\x1b[48;5;236m\x1b[97m' + ('▶ ' + stripAnsi(line)).padEnd(w) + reset + '\n');
        } else {
            process.stdout.write('  ' + line + '\n');
        }
    });

    // Footer
    mv(h - 2, 1);
    process.stdout.write(color.gray('─'.repeat(w)) + '\n');
    const help = ' ↑↓:이동  s:시작  x:중지  l:로그  r:새로고침  q:종료';
    if (message) {
        process.stdout.write(color.yellow(' ' + message));
    } else {
        process.stdout.write(color.gray(help));
    }
}

function renderLog() {
    cls();
    const w = process.stdout.columns || COLS;
    const h = process.stdout.rows || ROWS;
    const m = modules[cursor];
    const title = ` 📋 ${m?.id} 로그`;
    process.stdout.write(color.bgDark(title.padEnd(w)) + '\n');
    process.stdout.write(color.gray('─'.repeat(w)) + '\n');

    const visible = h - 5;
    const start = Math.max(0, logLines.length - visible - logScroll);
    const slice = logLines.slice(start, start + visible);

    slice.forEach(line => {
        let colored = line;
        if (/ERROR|Exception|SEVERE/i.test(line)) colored = color.red(line);
        else if (/WARN/i.test(line)) colored = color.yellow(line);
        else if (/Started|INFO/i.test(line)) colored = color.green(line);
        else colored = color.gray(line);
        process.stdout.write(truncate(colored, w) + '\n');
    });

    mv(h - 2, 1);
    process.stdout.write(color.gray('─'.repeat(w)) + '\n');
    process.stdout.write(color.gray(' ESC/b:뒤로  ↑↓:스크롤  s:시작  x:중지'));
}

// ─── Actions ─────────────────────────────────────────────────────────────────
async function doRefresh() {
    showMessage('🔄 새로고침 중...');
    modules = await scanModules();
    modules.forEach(m => { if (!statusMap[m.id]) statusMap[m.id] = { status: 'stopped' }; });
    cursor = Math.min(cursor, Math.max(0, modules.length - 1));
    showMessage(`✅ ${modules.length}개 모듈 로드됨`);
    render();
}

function doStart() {
    const m = modules[cursor];
    if (!m) return;
    const st = statusMap[m.id]?.status;
    if (st === 'running' || st === 'starting') { showMessage('이미 실행 중입니다'); render(); return; }
    showMessage(`▶ ${m.id} 시작 중...`);
    statusMap[m.id] = { status: 'starting' };
    startModule(m);
    render();
}

function doStop() {
    const m = modules[cursor];
    if (!m) return;
    const st = statusMap[m.id]?.status;
    if (st === 'stopped') { showMessage('이미 중지됨'); render(); return; }
    showMessage(`⏹ ${m.id} 중지 중...`);
    statusMap[m.id] = { status: 'stopping' };
    stopModule(m.id);
    render();
}

async function doShowLog() {
    const m = modules[cursor];
    if (!m) return;
    mode = 'log';
    logScroll = 0;
    logLines = getModuleLogs(m.id, 300);
    if (!logLines.length) logLines = ['(로그 없음 — 모듈을 먼저 실행하세요)'];
    render();
}

// ─── SSE-like: listen to events ──────────────────────────────────────────────
events.on('status', ({ id, status, pid }) => {
    statusMap[id] = { status, pid };
    if (mode === 'list') render();
});
events.on('log', ({ id, line }) => {
    if (mode === 'log' && modules[cursor]?.id === id) {
        logLines.push(line.replace(/\n$/, ''));
        if (logLines.length > 500) logLines.shift();
        if (logScroll === 0) render();
    }
});

// ─── Key handling ─────────────────────────────────────────────────────────────
process.stdin.on('keypress', (ch, key) => {
    if (!key) return;

    if (mode === 'list') {
        if (key.name === 'up') { cursor = Math.max(0, cursor - 1); render(); }
        else if (key.name === 'down') { cursor = Math.min(modules.length - 1, cursor + 1); render(); }
        else if (key.name === 'return' || key.name === 'l') doShowLog();
        else if (ch === 's') doStart();
        else if (ch === 'x') doStop();
        else if (ch === 'r') doRefresh();
        else if (ch === 'q' || (key.ctrl && key.name === 'c')) {
            process.stdout.write('\x1b[?25h'); // show cursor
            process.exit(0);
        }
    } else if (mode === 'log') {
        if (key.name === 'escape' || ch === 'b' || ch === 'q') {
            mode = 'list'; logScroll = 0; render();
        }
        else if (key.name === 'up') { logScroll = Math.min(logLines.length, logScroll + 3); render(); }
        else if (key.name === 'down') { logScroll = Math.max(0, logScroll - 3); render(); }
        else if (ch === 's') { mode = 'list'; doStart(); }
        else if (ch === 'x') { mode = 'list'; doStop(); }
        else if (key.ctrl && key.name === 'c') { process.stdout.write('\x1b[?25h'); process.exit(0); }
    }
});

// ─── Entry ───────────────────────────────────────────────────────────────────
process.stdout.write('\x1b[?25l'); // hide cursor
process.on('exit', () => process.stdout.write('\x1b[?25h'));
process.on('SIGINT', () => { process.stdout.write('\x1b[?25h'); process.exit(0); });

await doRefresh();
