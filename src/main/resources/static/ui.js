import { $, fmt, emptyRow, showToast, showAlert } from './utils.js';
import { api, state } from './api.js';

export function renderDashboard() {
    const total = state.accounts.reduce((s,a) => s + Number(a.balance), 0);
    $('dash-total').textContent = 'zł ' + fmt(total);
    $('dash-accs').textContent = state.accounts.length;
    $('dash-txcount').textContent = state.transactions.length;

    const tbody = $('dash-tx-body');
    const recent = state.transactions.slice(0, 8);
    tbody.innerHTML = recent.length ? recent.map(txRowFull).join('') : emptyRow(5);
}

export function txRowFull(t) {
    const amtCls = t.transactionType==='CREDIT' ? 'amount-in' : t.transactionType==='DEBIT' ? 'amount-out' : 'amount-tx';
    const sign   = t.transactionType==='CREDIT' ? '+' : t.transactionType==='DEBIT' ? '-' : '↔';
    const typeColors = {CREDIT:'badge-green', DEBIT:'badge-red', TRANSFER:'badge-blue'};
    return `<tr>
    <td style="color:var(--text-md); font-size:12px; white-space:nowrap">${t.createdAt}</td>
    <td style="font-weight:500">${t.description}</td>
    <td style="color:var(--text-md); font-size:12px">${t.fromAccount}</td>
    <td style="color:var(--text-md); font-size:12px">${t.toAccount}</td>
    <td><span class="badge ${typeColors[t.transactionType]||'badge-blue'}">${t.transactionType}</span></td>
    <td class="${amtCls}">${sign} zł ${fmt(t.amount)}</td>
  </tr>`;
}

export function renderAccounts() {
    const grid = $('accounts-grid');
    if (!state.accounts.length) {
        grid.innerHTML = '<p style="color:var(--text-sm);padding:24px">No accounts found.</p>';
        return;
    }
    const typeClass = {CHECKING:'checking', SAVINGS:'savings', INVESTMENT:'investment'};
    grid.innerHTML = state.accounts.map(a => `
    <div class="account-card ${typeClass[a.accountType]||'checking'}" onclick="showAccDetail(${a.id}, '${a.accountName}')">
      <div class="chip"></div>
      <div>
        <div class="acc-type">${a.accountType}</div>
        <div class="acc-name">${a.accountName}</div>
        <div class="acc-number">${a.accountNumber.substring(0,22)}...</div>
      </div>
      <div>
        <div class="acc-currency">${a.currency}</div>
        <div class="acc-balance">zł ${fmt(a.balance)}</div>
      </div>
    </div>
  `).join('');
}

// Make globally available for onclick
window.showAccDetail = async (accId, accName) => {
    const txs = await api(`/api/bank/accounts/${accId}/transactions`);
    $('acc-detail-title').textContent = accName + ' — Transactions';
    const tbody = $('acc-tx-body');
    tbody.innerHTML = txs.length ? txs.map(t => {
        const amtCls = t.transactionType==='CREDIT' ? 'amount-in' : t.transactionType==='DEBIT' ? 'amount-out' : 'amount-tx';
        const sign = t.transactionType==='CREDIT' ? '+' : t.transactionType==='DEBIT' ? '-' : '↔';
        return `<tr><td>${t.createdAt}</td><td>${t.description}</td><td>${t.transactionType}</td><td>${t.fromAccount}</td><td class="${amtCls}">${sign} zł ${fmt(t.amount)}</td></tr>`;
    }).join('') : emptyRow(5);
    $('acc-detail-card').style.display = 'block';
};

window.renderAllTx = () => {
    // ... logic for filtering transactions ...
    // (Ensure you use state.transactions and state.accounts)
};