import express from 'express';
import { createServer } from 'http';
import { scanModules } from './scanner.js';
import {
    startModule, stopModule, getModuleLogs, getRunningIds, processMap, events, checkPort
} from './process-manager.js';

const app = express();
const httpServer = createServer(app);
const PORT = 9900;

app.use(express.json());
app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    next();
});

// In-memory module list (refreshed on demand)
let moduleList = [];
let statusMap = {}; // id -> { status, pid, startedAt }

events.on('status', ({ id, status, pid }) => {
    if (!statusMap[id]) statusMap[id] = {};
    statusMap[id].status = status;
    statusMap[id].pid = pid;
    if (status === 'running') statusMap[id].startedAt = new Date().toISOString();
});

// Initial scan
(async () => {
    moduleList = await scanModules();
    console.log(`[MSA Manager] ${moduleList.length}개 모듈 발견됨`);
    moduleList.forEach(m => {
        statusMap[m.id] = { status: 'stopped', pid: null };
    });
})();

// ================================================================
// REST API
// ================================================================

// GET /api/modules — list all modules with status
app.get('/api/modules', async (req, res) => {
    if (req.query.refresh === '1') {
        moduleList = await scanModules();
        moduleList.forEach(m => {
            if (!statusMap[m.id]) statusMap[m.id] = { status: 'stopped', pid: null };
        });
    }
    const runningIds = getRunningIds();
    const result = moduleList.map(m => {
        const st = statusMap[m.id] ?? { status: 'stopped', pid: null };
        // Sync with real process map
        if (runningIds.includes(m.id) && st.status === 'stopped') {
            st.status = 'running';
        } else if (!runningIds.includes(m.id) && st.status === 'running') {
            st.status = 'stopped';
        }
        return { ...m, ...st };
    });
    res.json(result);
});

// POST /api/modules/:id/start
app.post('/api/modules/:id/start', (req, res) => {
    const mod = moduleList.find(m => m.id === req.params.id);
    if (!mod) return res.status(404).json({ error: '모듈을 찾을 수 없습니다' });
    statusMap[mod.id] = { status: 'starting', pid: null };
    const result = startModule(mod);
    res.json(result);
});

// POST /api/modules/:id/stop
app.post('/api/modules/:id/stop', (req, res) => {
    const result = stopModule(req.params.id);
    if (result.ok) statusMap[req.params.id] = { status: 'stopping', pid: null };
    res.json(result);
});

// GET /api/modules/:id/logs
app.get('/api/modules/:id/logs', (req, res) => {
    const lines = parseInt(req.query.lines ?? 200);
    res.json({ logs: getModuleLogs(req.params.id, lines) });
});

// GET /api/modules/:id/status
app.get('/api/modules/:id/status', (req, res) => {
    const st = statusMap[req.params.id] ?? { status: 'stopped', pid: null };
    res.json(st);
});

// SSE endpoint for real-time updates
app.get('/api/events', (req, res) => {
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.flushHeaders();

    const onStatus = (data) => res.write(`event: status\ndata: ${JSON.stringify(data)}\n\n`);
    const onLog = (data) => res.write(`event: log\ndata: ${JSON.stringify(data)}\n\n`);

    events.on('status', onStatus);
    events.on('log', onLog);

    req.on('close', () => {
        events.off('status', onStatus);
        events.off('log', onLog);
    });
});

// ================================================================
// Web UI (single-page HTML embedded)
// ================================================================
app.get('/', (req, res) => {
    res.send(getHTML());
});

httpServer.listen(PORT, () => {
    console.log(`\n🚀 MSA Manager 웹 서버 실행 중: http://localhost:${PORT}\n`);
});

function getHTML() {
    return `<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>MSA Manager</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet"/>
<style>
  *{box-sizing:border-box;margin:0;padding:0}
  :root{
    --bg:#0f1117;--surface:#1a1d27;--surface2:#242837;--border:#2e3248;
    --text:#e2e8f0;--text2:#8892a4;--blue:#4f8ef7;--green:#34d399;
    --yellow:#fbbf24;--red:#f87171;--purple:#a78bfa;--cyan:#38bdf8;
  }
  body{font-family:'Inter',sans-serif;background:var(--bg);color:var(--text);min-height:100vh}
  header{background:var(--surface);border-bottom:1px solid var(--border);padding:0 24px;display:flex;align-items:center;justify-content:space-between;height:60px;position:sticky;top:0;z-index:100;backdrop-filter:blur(10px)}
  .logo{display:flex;align-items:center;gap:10px;font-size:18px;font-weight:700;color:var(--blue)}
  .logo svg{width:28px;height:28px}
  .header-actions{display:flex;gap:10px;align-items:center}
  .btn{display:inline-flex;align-items:center;gap:6px;padding:7px 14px;border-radius:8px;border:none;font-size:13px;font-weight:600;cursor:pointer;transition:all .15s}
  .btn-primary{background:var(--blue);color:#fff} .btn-primary:hover{background:#3a7ae4}
  .btn-ghost{background:transparent;color:var(--text2);border:1px solid var(--border)} .btn-ghost:hover{background:var(--surface2);color:var(--text)}
  .btn-sm{padding:4px 10px;font-size:12px;border-radius:6px}
  .btn-success{background:rgba(52,211,153,.15);color:var(--green);border:1px solid rgba(52,211,153,.3)} .btn-success:hover{background:rgba(52,211,153,.25)}
  .btn-danger{background:rgba(248,113,113,.12);color:var(--red);border:1px solid rgba(248,113,113,.3)} .btn-danger:hover{background:rgba(248,113,113,.22)}
  .badge{display:inline-flex;align-items:center;gap:4px;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:600}
  .badge-running{background:rgba(52,211,153,.15);color:var(--green)}
  .badge-stopped{background:rgba(100,116,139,.1);color:var(--text2)}
  .badge-starting{background:rgba(251,191,36,.12);color:var(--yellow)}
  .badge-error{background:rgba(248,113,113,.12);color:var(--red)}
  .dot{width:7px;height:7px;border-radius:50%;background:currentColor}
  .dot-pulse{animation:pulse 1.5s ease-in-out infinite}
  @keyframes pulse{0%,100%{opacity:1}50%{opacity:.4}}
  
  .container{max-width:1280px;margin:0 auto;padding:24px}
  .grid{display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px;margin-bottom:24px}
  .stat-card{background:var(--surface);border:1px solid var(--border);border-radius:12px;padding:16px 20px;display:flex;align-items:center;gap:14px}
  .stat-icon{width:40px;height:40px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:20px}
  .stat-val{font-size:26px;font-weight:700}
  .stat-label{font-size:12px;color:var(--text2);margin-top:2px}

  .table-wrap{background:var(--surface);border:1px solid var(--border);border-radius:14px;overflow:hidden}
  .table-header{display:flex;align-items:center;justify-content:space-between;padding:16px 20px;border-bottom:1px solid var(--border)}
  .table-title{font-size:15px;font-weight:700}
  table{width:100%;border-collapse:collapse}
  th{text-align:left;font-size:11px;font-weight:600;color:var(--text2);text-transform:uppercase;letter-spacing:.05em;padding:12px 20px;border-bottom:1px solid var(--border);background:var(--surface2)}
  td{padding:14px 20px;border-bottom:1px solid rgba(46,50,72,.5);font-size:14px;vertical-align:middle}
  tr:last-child td{border:none}
  tr:hover td{background:rgba(255,255,255,.02)}
  .module-name{font-weight:600;color:var(--text)}
  .module-dir{font-size:11px;color:var(--text2);margin-top:2px;font-family:monospace}
  .port-badge{background:var(--surface2);border:1px solid var(--border);color:var(--cyan);font-family:monospace;font-size:12px;padding:2px 8px;border-radius:6px}
  .actions{display:flex;gap:6px}

  /* Log drawer */
  .drawer{position:fixed;right:0;top:0;bottom:0;width:520px;background:var(--surface);border-left:1px solid var(--border);transform:translateX(100%);transition:transform .25s cubic-bezier(.4,0,.2,1);z-index:200;display:flex;flex-direction:column}
  .drawer.open{transform:translateX(0)}
  .drawer-header{display:flex;align-items:center;justify-content:space-between;padding:16px 20px;border-bottom:1px solid var(--border);flex-shrink:0}
  .drawer-title{font-size:15px;font-weight:700}
  .drawer-close{width:32px;height:32px;border:none;background:transparent;color:var(--text2);cursor:pointer;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:18px}
  .drawer-close:hover{background:var(--surface2)}
  .log-box{flex:1;overflow-y:auto;padding:16px;font-family:monospace;font-size:12px;line-height:1.7;color:var(--text2)}
  .log-box .err{color:var(--red)} .log-box .warn{color:var(--yellow)} .log-box .info{color:var(--green)} .log-box .line{white-space:pre-wrap;word-break:break-all}
  .overlay{position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:150;display:none}
  .overlay.open{display:block}

  .ms{font-family:"Material Symbols Rounded";font-size:18px;font-weight:300;line-height:1;vertical-align:middle}
  .refresh-spin{animation:spin .6s linear infinite} @keyframes spin{to{transform:rotate(360deg)}}
  .search{background:var(--surface2);border:1px solid var(--border);color:var(--text);padding:7px 12px;border-radius:8px;font-size:13px;width:200px;outline:none}
  .search:focus{border-color:var(--blue)}
  .filter-bar{display:flex;gap:8px;align-items:center;margin-bottom:16px}
  .chip{padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;cursor:pointer;border:1px solid var(--border);background:transparent;color:var(--text2);transition:all .15s}
  .chip.active{background:var(--blue);color:#fff;border-color:var(--blue)}
  #toast{position:fixed;bottom:20px;left:50%;transform:translateX(-50%) translateY(60px);background:#1e2435;border:1px solid var(--border);color:var(--text);padding:10px 20px;border-radius:10px;font-size:13px;transition:transform .3s;z-index:999}
  #toast.show{transform:translateX(-50%) translateY(0)}
</style>
</head>
<body>
<header>
  <div class="logo">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="13 2 13 9 20 9"/><path d="M2 22 12.6 3.4a1 1 0 0 1 1.7-.1L20 9"/><path d="M2 22h20"/><path d="M2 22 7 10"/></svg>
    MSA Manager
  </div>
  <div class="header-actions">
    <span id="lastUpdate" style="font-size:12px;color:var(--text2)"></span>
    <button class="btn btn-ghost btn-sm" onclick="refreshModules(true)"><span class="ms" id="refreshIcon">refresh</span> 새로고침</button>
    <button class="btn btn-primary btn-sm" onclick="startAll()"><span class="ms">play_arrow</span> 전체 시작</button>
    <button class="btn btn-ghost btn-sm" onclick="stopAll()"><span class="ms">stop</span> 전체 중지</button>
  </div>
</header>

<div class="container">
  <div class="grid">
    <div class="stat-card">
      <div class="stat-icon" style="background:rgba(79,142,247,.12)"><span class="ms" style="color:var(--blue);font-size:22px">apps</span></div>
      <div><div class="stat-val" id="totalCount">-</div><div class="stat-label">전체 모듈</div></div>
    </div>
    <div class="stat-card">
      <div class="stat-icon" style="background:rgba(52,211,153,.12)"><span class="ms" style="color:var(--green);font-size:22px">check_circle</span></div>
      <div><div class="stat-val" id="runningCount" style="color:var(--green)">-</div><div class="stat-label">실행 중</div></div>
    </div>
    <div class="stat-card">
      <div class="stat-icon" style="background:rgba(100,116,139,.1)"><span class="ms" style="color:var(--text2);font-size:22px">stop_circle</span></div>
      <div><div class="stat-val" id="stoppedCount" style="color:var(--text2)">-</div><div class="stat-label">중지됨</div></div>
    </div>
  </div>

  <div class="filter-bar">
    <input class="search" id="search" placeholder="모듈 검색..." oninput="renderTable()">
    <button class="chip active" data-filter="all" onclick="setFilter(this)">전체</button>
    <button class="chip" data-filter="running" onclick="setFilter(this)">실행 중</button>
    <button class="chip" data-filter="stopped" onclick="setFilter(this)">중지됨</button>
  </div>

  <div class="table-wrap">
    <div class="table-header">
      <span class="table-title">모듈 목록</span>
      <span style="font-size:12px;color:var(--text2)">프로젝트: /opt/carbosys</span>
    </div>
    <table>
      <thead><tr>
        <th>모듈명</th><th>App Name</th><th>포트</th><th>상태</th><th>PID</th><th style="text-align:right">작업</th>
      </tr></thead>
      <tbody id="moduleTable"><tr><td colspan="6" style="text-align:center;color:var(--text2);padding:40px">로딩 중...</td></tr></tbody>
    </table>
  </div>
</div>

<div class="overlay" id="overlay" onclick="closeDrawer()"></div>
<div class="drawer" id="drawer">
  <div class="drawer-header">
    <span class="drawer-title" id="drawerTitle">로그</span>
    <button class="drawer-close" onclick="closeDrawer()">✕</button>
  </div>
  <div class="log-box" id="logBox"></div>
</div>

<div id="toast"></div>

<script>
let modules = [];
let filter = 'all';
let currentLogId = null;
const sse = new EventSource('/api/events');

sse.addEventListener('status', e => {
  const d = JSON.parse(e.data);
  const m = modules.find(x => x.id === d.id);
  if (m) { m.status = d.status; m.pid = d.pid; }
  renderTable();
  updateStats();
});
sse.addEventListener('log', e => {
  const d = JSON.parse(e.data);
  if (d.id === currentLogId) appendLog(d.line);
});

async function refreshModules(force = false) {
  const icon = document.getElementById('refreshIcon');
  icon.classList.add('refresh-spin');
  const res = await fetch('/api/modules' + (force ? '?refresh=1' : ''));
  modules = await res.json();
  icon.classList.remove('refresh-spin');
  document.getElementById('lastUpdate').textContent = '업데이트: ' + new Date().toLocaleTimeString('ko-KR');
  renderTable();
  updateStats();
}

function updateStats() {
  document.getElementById('totalCount').textContent = modules.length;
  const running = modules.filter(m => m.status === 'running' || m.status === 'starting').length;
  document.getElementById('runningCount').textContent = running;
  document.getElementById('stoppedCount').textContent = modules.length - running;
}

function setFilter(el) {
  document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
  el.classList.add('active');
  filter = el.dataset.filter;
  renderTable();
}

function renderTable() {
  const q = document.getElementById('search').value.toLowerCase();
  let list = modules.filter(m => {
    if (filter === 'running') return m.status === 'running' || m.status === 'starting';
    if (filter === 'stopped') return m.status === 'stopped' || m.status === 'error';
    return true;
  }).filter(m => !q || m.id.toLowerCase().includes(q) || (m.name||'').toLowerCase().includes(q));

  const tbody = document.getElementById('moduleTable');
  if (!list.length) { tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--text2);padding:40px">모듈 없음</td></tr>'; return; }
  tbody.innerHTML = list.map(m => {
    const isRunning = m.status === 'running';
    const isStarting = m.status === 'starting';
    const isStopping = m.status === 'stopping';
    let badge = '';
    if (isRunning) badge = '<span class="badge badge-running"><span class="dot"></span>실행 중</span>';
    else if (isStarting) badge = '<span class="badge badge-starting"><span class="dot dot-pulse"></span>시작 중</span>';
    else if (isStopping) badge = '<span class="badge badge-starting"><span class="dot dot-pulse"></span>중지 중</span>';
    else if (m.status === 'error') badge = '<span class="badge badge-error"><span class="dot"></span>오류</span>';
    else badge = '<span class="badge badge-stopped"><span class="dot"></span>중지됨</span>';
    
    const portDisplay = m.port && m.port !== 0
      ? '<span class="port-badge">:' + m.port + '</span>'
      : '<span style="color:var(--text2);font-size:12px">랜덤</span>';

    const canStart = !isRunning && !isStarting && !isStopping;
    const canStop = isRunning || isStarting;

    return '<tr>'
      + '<td><div class="module-name">' + m.id + '</div><div class="module-dir">' + m.dir + '</div></td>'
      + '<td style="color:var(--cyan);font-size:13px">' + (m.name || '-') + '</td>'
      + '<td>' + portDisplay + '</td>'
      + '<td>' + badge + '</td>'
      + '<td style="font-family:monospace;font-size:12px;color:var(--text2)">' + (m.pid ? m.pid : '-') + '</td>'
      + '<td><div class="actions" style="justify-content:flex-end">'
      + (canStart ? '<button class="btn btn-success btn-sm" onclick="startMod(\'' + m.id + '\')"><span class="ms" style="font-size:15px">play_arrow</span> 시작</button>' : '')
      + (canStop ? '<button class="btn btn-danger btn-sm" onclick="stopMod(\'' + m.id + '\')"><span class="ms" style="font-size:15px">stop</span> 중지</button>' : '')
      + '<button class="btn btn-ghost btn-sm" onclick="showLogs(\'' + m.id + '\',\'' + m.name + '\')"><span class="ms" style="font-size:15px">article</span> 로그</button>'
      + '</div></td>'
      + '</tr>';
  }).join('');
}

async function startMod(id) {
  showToast('🟡 ' + id + ' 시작 중...');
  await fetch('/api/modules/' + id + '/start', { method:'POST' });
}
async function stopMod(id) {
  showToast('⏹️ ' + id + ' 중지 중...');
  await fetch('/api/modules/' + id + '/stop', { method:'POST' });
}
async function startAll() {
  for (const m of modules) {
    if (m.status === 'stopped') await fetch('/api/modules/' + m.id + '/start', { method:'POST' });
  }
  showToast('전체 모듈 시작 요청됨');
}
async function stopAll() {
  for (const m of modules) {
    if (m.status === 'running' || m.status === 'starting') await fetch('/api/modules/' + m.id + '/stop', { method:'POST' });
  }
  showToast('전체 모듈 중지 요청됨');
}

async function showLogs(id, name) {
  currentLogId = id;
  document.getElementById('drawerTitle').textContent = '📋 ' + name + ' 로그';
  document.getElementById('drawer').classList.add('open');
  document.getElementById('overlay').classList.add('open');
  const res = await fetch('/api/modules/' + id + '/logs?lines=200');
  const { logs } = await res.json();
  const box = document.getElementById('logBox');
  box.innerHTML = logs.map(l => '<div class="line ' + colorLog(l) + '">' + escHtml(l) + '</div>').join('');
  box.scrollTop = box.scrollHeight;
}

function appendLog(line) {
  const box = document.getElementById('logBox');
  const div = document.createElement('div');
  div.className = 'line ' + colorLog(line);
  div.textContent = line;
  box.appendChild(div);
  if (box.children.length > 500) box.removeChild(box.children[0]);
  box.scrollTop = box.scrollHeight;
}

function colorLog(l) {
  if (/ERROR|Exception|SEVERE/i.test(l)) return 'err';
  if (/WARN/i.test(l)) return 'warn';
  if (/Started|INFO/i.test(l)) return 'info';
  return '';
}
function closeDrawer() {
  document.getElementById('drawer').classList.remove('open');
  document.getElementById('overlay').classList.remove('open');
  currentLogId = null;
}
function escHtml(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }

let toastTimer;
function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg; t.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove('show'), 2500);
}

// Auto-refresh every 5s
setInterval(() => refreshModules(false), 5000);
refreshModules(true);
</script>
</body>
</html>`;
}
