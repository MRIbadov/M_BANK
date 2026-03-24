export const $ = id => document.getElementById(id);
export const fmt = n => Number(n).toLocaleString('pl-PL', {minimumFractionDigits:2, maximumFractionDigits:2});

export function showToast(msg, type='') {
    const t = $('toast');
    if(!t) return;
    t.textContent = msg;
    t.className = 'show' + (type ? ' ' + type : '');
    clearTimeout(t._timer);
    t._timer = setTimeout(() => t.className = '', 3500);
}

export function showAlert(id, msg) {
    const el = $(id);
    if(!el) return;
    el.textContent = msg;
    el.classList.add('show');
    setTimeout(() => el.classList.remove('show'), 5000);
}

export function emptyRow(cols, msg='No data yet') {
    return `<tr><td colspan="${cols}"><div class="empty"><div class="empty-icon">📭</div><div class="empty-text">${msg}</div></div></td></tr>`;
}

// Attach to window so HTML can see them
window.showAlert = showAlert;