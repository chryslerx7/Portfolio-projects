<?php
session_start();
$cfg = [
  'host'=>'localhost',
  'dbname'=>'ipon_db',
  'dbuser'=>'root',
  'dbpass'=>'',            
  'charset'=>'utf8mb4',
  'admin_user'=>'admin',   
  'admin_pass'=>'admin123' 
];
// ---------------- DB connect & bootstrap ----------------
try {
  $pdo = new PDO("mysql:host={$cfg['host']};charset={$cfg['charset']}", $cfg['dbuser'],$cfg['dbpass'], [
    PDO::ATTR_ERRMODE=>PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE=>PDO::FETCH_ASSOC
  ]);
  $pdo->exec("CREATE DATABASE IF NOT EXISTS `{$cfg['dbname']}` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
  $pdo->exec("USE `{$cfg['dbname']}`");
} catch (PDOException $e) {
  die("DB connection failed: ".htmlspecialchars($e->getMessage()));
}

// create tables (users kept but auth removed)
$pdo->exec("
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
");
$pdo->exec("
CREATE TABLE IF NOT EXISTS hulog (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  date DATE NOT NULL,
  amount DECIMAL(10,2) NOT NULL DEFAULT 67.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY user_date (user_id, date),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
");

// Ensure there is at least one user; use it as the single default user.
$stmt = $pdo->query("SELECT id FROM users ORDER BY id LIMIT 1");
$firstUser = $stmt->fetchColumn();
if (!$firstUser) {
  $hash = password_hash($cfg['admin_pass'], PASSWORD_DEFAULT);
  $ins = $pdo->prepare("INSERT INTO users (username,password_hash) VALUES (?,?)");
  $ins->execute([$cfg['admin_user'],$hash]);
  $firstUser = $pdo->lastInsertId();
}
$DEFAULT_USER_ID = (int)$firstUser;

// helpers
function jsonRes($arr){ header('Content-Type: application/json; charset=utf-8'); echo json_encode($arr); exit; }

$MAX_DATE = new DateTime('2036-12-31');

// ---------------- POST / API ----------------
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
  $action = $_POST['action'];

  // get statuses for a given year (we will request 12 months at once)
  if ($action === 'get_months_range') {
    $year = intval($_POST['year'] ?? 0);
    if ($year <= 0) jsonRes(['ok'=>false,'msg'=>'invalid']);
    $start = "{$year}-01-01";
    $end = "{$year}-12-31";
    $stmt = $pdo->prepare("SELECT date,amount FROM hulog WHERE user_id = ? AND date BETWEEN ? AND ?");
    $stmt->execute([$DEFAULT_USER_ID,$start,$end]);
    $rows = $stmt->fetchAll();
    $out = [];
    foreach ($rows as $r) $out[$r['date']] = number_format((float)$r['amount'],2,'.','');
    jsonRes(['ok'=>true,'statuses'=>$out]);
  }

  // set date (insert/update or delete)
  if ($action === 'set_date') {
    $date = $_POST['date'] ?? '';
    $hulog = intval($_POST['hulog'] ?? 0);
    $amount = isset($_POST['amount']) ? (float)$_POST['amount'] : 67.00;
    $d = DateTime::createFromFormat('Y-m-d',$date);
    if (!$d) jsonRes(['ok'=>false,'msg'=>'invalid']);
    if ($d > $MAX_DATE) jsonRes(['ok'=>false,'msg'=>'out of range']);
    if ($hulog) {
      $stmt = $pdo->prepare("INSERT INTO hulog (user_id,date,amount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE amount = VALUES(amount), created_at = CURRENT_TIMESTAMP");
      $stmt->execute([$DEFAULT_USER_ID,$date,$amount]);
      jsonRes(['ok'=>true,'date'=>$date,'hulog'=>1]);
    } else {
      $stmt = $pdo->prepare("DELETE FROM hulog WHERE user_id = ? AND date = ?");
      $stmt->execute([$DEFAULT_USER_ID,$date]);
      jsonRes(['ok'=>true,'date'=>$date,'hulog'=>0]);
    }
  }

  // get total saved for the single user
  if ($action === 'get_total') {
    $stmt = $pdo->prepare("SELECT IFNULL(SUM(amount),0) AS total FROM hulog WHERE user_id = ?");
    $stmt->execute([$DEFAULT_USER_ID]); $row = $stmt->fetch();
    jsonRes(['ok'=>true,'total'=>number_format((float)$row['total'],2,'.','')]);
  }

  // export CSV (single user)
  if ($action === 'export_csv') {
    $stmt = $pdo->prepare("SELECT date,amount,created_at FROM hulog WHERE user_id = ? ORDER BY date ASC");
    $stmt->execute([$DEFAULT_USER_ID]); $rows = $stmt->fetchAll();
    header('Content-Type: text/csv; charset=utf-8');
    header('Content-Disposition: attachment; filename="ipon_export_'.date('Ymd_His').'.csv"');
    $out = fopen('php://output','w');
    fputcsv($out, ['date','amount','created_at']);
    foreach ($rows as $r) fputcsv($out, [$r['date'],$r['amount'],$r['created_at']]);
    fclose($out); exit;
  }

  // import CSV (single user)
  if ($action === 'import_csv') {
    if (!isset($_FILES['csv'])) jsonRes(['ok'=>false,'msg'=>'no file']);
    $fn = $_FILES['csv']['tmp_name']; $inserted=0;
    if (($h = fopen($fn,'r')) !== false) {
      $first=true;
      while (($row = fgetcsv($h,2000,',')) !== false) {
        if ($first) { $first=false; continue; }
        if (count($row) < 2) continue;
        $date = trim($row[0]); $amount = floatval($row[1]);
        $d = DateTime::createFromFormat('Y-m-d',$date); if (!$d) continue;
        if ($d > $MAX_DATE) continue;
        try {
          $ins = $pdo->prepare("INSERT INTO hulog (user_id,date,amount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE amount = VALUES(amount), created_at = CURRENT_TIMESTAMP");
          $ins->execute([$DEFAULT_USER_ID,$date,$amount]); $inserted++;
        } catch (Exception $e) { continue; }
      }
      fclose($h);
    }
    jsonRes(['ok'=>true,'inserted'=>$inserted]);
  }

  jsonRes(['ok'=>false,'msg'=>'unknown']);
}

// ---------------- PAGE RENDER (single app page) ----------------
?>
<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Ipon — Year view</title>
<style>
:root{--bg:#f7fbff;--card:#fff;--accent:#e53935;--muted:#6b7280;--accent2:#3498db;--check:#27ae60}
*{box-sizing:border-box}
body{font-family:Inter,Segoe UI,Arial,sans-serif;background:var(--bg);margin:0;padding:18px}
.header{display:flex;align-items:center;justify-content:space-between;gap:8px;max-width:1200px;margin:0 auto 12px}
.brand h1{margin:0;color:var(--accent)}
.controls{display:flex;align-items:center;gap:8px}
.btn{border:none;padding:8px 12px;border-radius:9px;cursor:pointer;background:var(--accent2);color:#fff;font-weight:700}
.btn.ghost{background:#fff;border:1px solid #e6e9ef;color:#333}
.summary{background:var(--card);padding:10px;border-radius:10px;display:flex;gap:12px;align-items:center}
.container{max-width:1200px;margin:8px auto}

/* top nav */
.navbar { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:16px }
.year-title-big { font-size:28px; font-weight:800; color:#2c3e50 }

/* 12 months grid - perfect squares */
.months-wrap { display:grid; grid-template-columns: repeat(4, 1fr); gap:16px; align-items:start; }
@media(max-width:1100px){ .months-wrap{ grid-template-columns:repeat(3,1fr);} }
@media(max-width:800px){ .months-wrap{ grid-template-columns:repeat(2,1fr);} }
@media(max-width:480px){ .months-wrap{ grid-template-columns:repeat(1,1fr);} }

/* make each month a perfect square */
.month {
  background:var(--card);
  border-radius:12px;
  box-shadow:0 10px 30px rgba(2,6,23,0.06);
  overflow:hidden;
  display:flex; flex-direction:column;
  aspect-ratio: 1 / 1;
  min-height: 240px;
}

/* header and grid inside */
.month-header { background:linear-gradient(90deg,#e74c3c,#e53935); color:#fff; padding:10px 12px; display:flex; justify-content:space-between; align-items:center; }
.month-body { padding:10px; display:flex; flex-direction:column; flex:1; }

/* weekday labels */
.weekdays { display:grid; grid-template-columns:repeat(7,1fr); gap:6px; margin-bottom:6px; }
.weekday { font-size:11px; color:var(--muted); text-align:center; font-weight:700 }

/* days grid - EXACT 6 rows so every month is consistent (7 cols x 6 rows = 42 cells) */
.days { display:grid; grid-template-columns:repeat(7,1fr); grid-template-rows:repeat(6,1fr); gap:6px; align-content:start; flex:1; }

/* compact day box: keep them square-ish inside the square month */
.day { background:#fff; border-radius:8px; padding:6px; display:flex; flex-direction:column; justify-content:space-between; align-items:flex-start; cursor:pointer; border:1px solid transparent; transition:transform .12s, box-shadow .12s, border-color .12s; position:relative; min-width:0; aspect-ratio: 1 / 1; overflow:hidden; }
.day:active{ transform:scale(.995) }
.day.other{ opacity:.28; cursor:default; background:transparent; border:none; }
.date-num{ font-weight:800; font-size:12px; color:#222; align-self:flex-end }
.amount-badge{ background:var(--accent2); color:#fff; font-weight:700; padding:4px 6px; border-radius:6px; font-size:11px; align-self:flex-start; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }

/* checked indicator */
.status{ position:absolute; top:8px; right:8px; font-size:14px; color:var(--check); display:none; transform:scale(0); transition:transform .28s cubic-bezier(.2,.9,.3,1) }
.day.done .status { display:block; transform:scale(1) }

/* modal / toast */
.modal-backdrop{ position:fixed; inset:0; background:rgba(2,6,23,0.45); display:none; align-items:center; justify-content:center; z-index:1000 }
.modal{ background:#fff; border-radius:12px; padding:18px; width:360px; box-shadow:0 18px 50px rgba(2,6,23,0.25); transform:translateY(12px) scale(.98); opacity:0; transition:transform .28s .05s, opacity .28s .05s }
.modal.show{ transform:none; opacity:1 }
.toast{ position:fixed; right:18px; bottom:18px; background:var(--check); color:#fff; padding:10px 14px; border-radius:10px; display:none; z-index:1100 }
.toast.show{ display:block; animation:toastBounce .9s both }
@keyframes toastBounce{0%{transform:translateY(30px) scale(.9);opacity:0}60%{transform:translateY(-8px) scale(1.03);opacity:1}100%{transform:none}}

/* year change animation container */
.year-frame { position:relative; min-height: 420px; overflow:hidden; }
.year-inner { position:relative; transition: transform .42s cubic-bezier(.2,.9,.3,1), opacity .28s; }

.footer{ max-width:1200px; margin:18px auto; text-align:center; color:var(--muted); font-size:13px; padding-bottom:40px }

/* === NEW GREEN HIGHLIGHT WHEN DAY IS CHECKED === */
.day.done {
  background-color: #27ae60; /* green background */
  color: white; /* text color white for contrast */
  border-color: #1e8449; /* darker green border */
  box-shadow: 0 0 8px 2px rgba(39, 174, 96, 0.6);
}
.day.done .amount-badge {
  background-color: #145a32; /* darker green for badge */
  color: #d1f2eb;
}
.day.done .date-num {
  color: #e9f7ef;
}

</style>
</head>
<body>
<div class="header" style="max-width:1200px;margin:0 auto">
  <div class="brand"><h1>IPON Calendar</h1><div style="color:var(--muted);font-size:13px">Single-user — edit amounts & mark hulog</div></div>
  <div class="controls">
    <div class="summary"><div style="font-size:12px;color:var(--muted)">Total Saved</div><div id="totalSaved" style="font-weight:900;font-size:18px;margin-left:8px;color:var(--check)">₱0.00</div></div>
    <button class="btn ghost" id="exportBtn">Export CSV</button>
    <label class="btn ghost" style="cursor:pointer">Import CSV <input id="csvFile" name="csv" type="file" accept=".csv" style="display:none"></label>
  </div>
</div>

<div class="container" style="max-width:1200px;margin:12px auto">
  <div class="navbar">
    <div style="display:flex;gap:10px;align-items:center">
      <button id="prevYear" class="btn ghost">◀ Prev Year</button>
      <div class="year-title-big" id="yearTitle">2025</div>
      <button id="nextYear" class="btn ghost">Next Year ▶</button>
    </div>
    <div style="color:var(--muted);font-size:13px">Weeks start on Sunday</div>
  </div>

  <div class="year-frame">
    <div id="yearInner" class="year-inner"></div>
  </div>
</div>

<!-- modal -->
<div class="modal-backdrop" id="modalBackdrop">
  <div class="modal" id="modalBox" role="dialog" aria-modal="true">
    <h3 id="modalTitle">Nakahulog ka na?</h3>
    <p id="modalDate" style="color:#666;margin-top:6px"></p>
    <div style="margin-top:12px;display:flex;align-items:center;gap:12px">
      <label style="display:flex;align-items:center;gap:8px;font-weight:700"><input id="modalCheckbox" type="checkbox"> Nakahulog</label>
      <div style="margin-left:auto"><input id="modalAmount" type="number" step="0.01" value="67.00" style="padding:8px;border-radius:8px;border:1px solid #e6e9ef;width:140px"></div>
    </div>
    <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:14px"><button class="btn ghost" id="modalCancel">Cancel</button><button class="btn" id="modalSave">Save</button></div>
  </div>
</div>

<div class="toast" id="toast">Yehey! Nakahulog na.</div>
<div class="footer">Tip: Use Prev / Next Year to navigate. Click a day to add/edit amount. Export/Import CSV for backup.</div>

<script>
// Client JS: single-year view, Sunday start, slide+fade animations for year change
const START = new Date(); // today
const MAX = new Date('2036-12-31');
const yearInner = document.getElementById('yearInner');
const yearTitle = document.getElementById('yearTitle');
const totalSavedEl = document.getElementById('totalSaved');
const prevBtn = document.getElementById('prevYear');
const nextBtn = document.getElementById('nextYear');
const modalBackdrop = document.getElementById('modalBackdrop');
const modalBox = document.getElementById('modalBox');
const modalDateEl = document.getElementById('modalDate');
const modalCheckbox = document.getElementById('modalCheckbox');
const modalAmount = document.getElementById('modalAmount');
const modalSave = document.getElementById('modalSave');
const modalCancel = document.getElementById('modalCancel');
const toast = document.getElementById('toast');
const csvFile = document.getElementById('csvFile');
const exportBtn = document.getElementById('exportBtn');

let viewYear = (new Date()).getFullYear();
let statuses = {}; // date -> amount (strings)
let modalDate = null;

function ymd(d){ return d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0') + '-' + String(d.getDate()).padStart(2,'0'); }
async function post(data){ const fd=new FormData(); for(const k in data) fd.append(k,data[k]); const res = await fetch('',{method:'POST',body:fd}); return res.json(); }

async function loadYear(year){
  // fetch statuses for whole year (single request)
  const res = await post({action:'get_months_range', year: String(year)});
  if (res.ok) statuses = res.statuses || {}; else statuses = {};
  await updateTotal();
}

async function updateTotal(){ const r = await post({action:'get_total'}); if (r.ok) totalSavedEl.textContent = '₱' + Number(r.total).toFixed(2); }

function makeDay(dayNum, dateStr, amount, isOther){
  const el = document.createElement('div'); el.className = 'day' + (isOther ? ' other' : '');
  el.style.position = 'relative';
  const num = document.createElement('div'); num.className='date-num'; num.textContent = dayNum; el.appendChild(num);
  const amt = document.createElement('div'); amt.className='amount-badge'; amt.textContent = amount ? ('₱' + Number(amount).toFixed(2)) : '₱67.00'; el.appendChild(amt);
  const st = document.createElement('div'); st.className='status'; st.innerHTML = '<span class="checkmark"><svg viewBox="0 0 24 24"><path d="M4 12l5 5L20 6" stroke-linecap="round" stroke-linejoin="round"/></svg></span>'; el.appendChild(st);
  if (amount) el.classList.add('done');
  el.dataset.date = dateStr;
  if (!isOther) {
    el.addEventListener('click', ()=> {
      if (new Date(dateStr) > MAX) return;
      openModal(dateStr, amount);
    });
  }
  return el;
}

function buildYearDOM(year){
  const frame = document.createElement('div');
  frame.style.opacity = '0';
  frame.style.transform = 'translateY(6px)';
  const wrap = document.createElement('div'); wrap.className='months-wrap';
  // build 12 months
  for (let m=0;m<12;m++){
    const monthStart = new Date(year,m,1);
    const monthDiv = document.createElement('div'); monthDiv.className='month';
    const monthLabel = monthStart.toLocaleString(undefined,{month:'long', year:'numeric'});
    monthDiv.innerHTML = `<div class="month-header"><div class="title">${monthLabel}</div><div style="color:#fff;font-size:12px">₱/day</div></div>`;
    const body = document.createElement('div'); body.className='month-body';
    const weekdays = document.createElement('div'); weekdays.className='weekdays';
    ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].forEach(w => { const e = document.createElement('div'); e.className='weekday'; e.textContent = w; weekdays.appendChild(e); });
    body.appendChild(weekdays);
    const daysWrap = document.createElement('div'); daysWrap.className='days';

    // compute first day offset (Sunday=0)
    const firstDow = monthStart.getDay();
    const daysInMonth = new Date(year, m+1, 0).getDate();

    // --- Render 42 cells (7 cols x 6 rows) ---
    // Leading blanks (previous month placeholders)
    for (let i=0;i<firstDow;i++) daysWrap.appendChild(makeDay('','',null,true));

    // Current month days
    for (let d=1; d<=daysInMonth; d++){
      const dt = new Date(year,m,d);
      const ds = ymd(dt);
      const amt = (statuses[ds] !== undefined) ? statuses[ds] : null;
      daysWrap.appendChild(makeDay(d,ds,amt,false));
    }

    // Fill remaining to reach 42 cells total
    const totalSoFar = daysWrap.children.length;
    const remaining = 42 - totalSoFar;
    for (let i=0;i<remaining;i++) daysWrap.appendChild(makeDay('','',null,true));
    // --- end 42 cells ---

    body.appendChild(daysWrap);
    monthDiv.appendChild(body);
    wrap.appendChild(monthDiv);
  }
  frame.appendChild(wrap);
  return frame;
}

function applyAnimation(oldEl, newEl, direction='left'){
  // oldEl may be null on first load
  const container = yearInner;
  newEl.style.transition = 'transform .42s cubic-bezier(.2,.9,.3,1), opacity .28s';
  newEl.style.opacity = '0'; newEl.style.transform = direction === 'left' ? 'translateX(20px)' : 'translateX(-20px)';
  container.appendChild(newEl);
  requestAnimationFrame(()=> {
    newEl.style.opacity = '1'; newEl.style.transform = 'translateX(0)';
    if (oldEl) {
      oldEl.style.opacity = '0'; oldEl.style.transform = direction === 'left' ? 'translateX(-20px)' : 'translateX(20px)';
      setTimeout(()=> { if (oldEl.parentNode) oldEl.parentNode.removeChild(oldEl); }, 420);
    }
  });
}

async function showYear(year, direction='left'){
  yearTitle.textContent = year;
  await loadYear(year);
  const newDOM = buildYearDOM(year);
  // animate
  const old = yearInner.firstElementChild;
  applyAnimation(old, newDOM, direction);
  // small reveal tweak
  setTimeout(()=> { newDOM.style.opacity = '1'; newDOM.style.transform = 'none'; }, 50);
}

function openModal(dateStr, amount){
  modalDate = dateStr;
  modalDateEl.textContent = new Date(dateStr).toLocaleDateString(undefined,{weekday:'long',year:'numeric',month:'long',day:'numeric'});
  modalCheckbox.checked = !!amount;
  modalAmount.value = amount ? Number(amount).toFixed(2) : '67.00';
  modalBackdrop.style.display = 'flex';
  setTimeout(()=> modalBox.classList.add('show'), 10);
}
function closeModal(){ modalBox.classList.remove('show'); setTimeout(()=> modalBackdrop.style.display='none',260); modalDate=null; }

modalCancel.addEventListener('click', closeModal);
modalBackdrop.addEventListener('click', (e)=> { if (e.target === modalBackdrop) closeModal(); });

modalSave.addEventListener('click', async ()=>{
  if (!modalDate) return;
  const hulog = modalCheckbox.checked ? 1 : 0;
  const amt = parseFloat(modalAmount.value) || 0;
  const res = await post({action:'set_date', date: modalDate, hulog: hulog? '1':'0', amount: String(amt)});
  if (res.ok) {
    const el = document.querySelector(`[data-date="${modalDate}"]`);
    if (el) {
      el.querySelector('.amount-badge').textContent = '₱' + Number(amt).toFixed(2);
      if (hulog) el.classList.add('done'); else el.classList.remove('done');
    }
    if (hulog) { toast.textContent='Yehey! Nakahulog na.'; toast.classList.add('show'); setTimeout(()=> toast.classList.remove('show'),900); }
    await updateTotal();
    closeModal();
  } else alert('Save failed: ' + (res.msg || 'error'));
});

// prev/next year
prevBtn.addEventListener('click', ()=> {
  const newYear = viewYear - 1;
  // don't go earlier than START year
  const minYear = (new Date()).getFullYear();
  if (newYear < minYear) return;
  showYear(newYear, 'right'); viewYear = newYear;
});
nextBtn.addEventListener('click', ()=> {
  const newYear = viewYear + 1;
  const maxYear = 2036;
  if (newYear > maxYear) return;
  showYear(newYear, 'left'); viewYear = newYear;
});

// export/import
exportBtn.addEventListener('click', ()=> {
  const f = document.createElement('form'); f.method='POST'; f.style.display='none';
  const a = document.createElement('input'); a.name='action'; a.value='export_csv'; f.appendChild(a); document.body.appendChild(f); f.submit(); f.remove();
});
csvFile.addEventListener('change', async (e)=> {
  const file = e.target.files[0]; if (!file) return;
  const fd = new FormData(); fd.append('action','import_csv'); fd.append('csv', file);
  const r = await fetch('', {method:'POST', body: fd}); const j = await r.json();
  if (j.ok) { alert('Imported: ' + j.inserted + ' rows'); showYear(viewYear); updateTotal(); } else alert('Import failed');
});

// initial load
document.addEventListener('DOMContentLoaded', ()=> {
  const now = new Date();
  viewYear = now.getFullYear();
  showYear(viewYear, 'left');
});
</script>
</body></html>
