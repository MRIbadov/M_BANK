import { $, showAlert, showToast } from './utils.js';
import { api, state } from './api.js';
import * as ui from './ui.js';

// --- AUTH LOGIC ---
window.switchAuthTab = (tab) => {
    document.querySelectorAll('.auth-tab').forEach((t,i) => t.classList.toggle('active', (i===0&&tab==='login')||(i===1&&tab==='register')));
    $('login-form').classList.toggle('active', tab==='login');
    $('register-form').classList.toggle('active', tab==='register');
};

window.doLogin = async (e) => {
    e.preventDefault();
    const btn = $('login-btn');
    btn.disabled = true;
    try {
        const data = await api('/api/auth/login', 'POST', {
            username: $('login-username').value,
            password: $('login-password').value
        });
        state.token = data.token;
        localStorage.setItem('nb_token', data.token);
        location.reload(); // Refresh to boot the app state
    } catch(err) {
        showAlert('login-error', err.message);
        btn.disabled = false;
    }
};

window.logout = () => {
    localStorage.removeItem('nb_token');
    location.reload();
};

// --- NAVIGATION ---
window.navigate = (page) => {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    $('page-' + page).classList.add('active');
    if (page === 'transactions') ui.renderAllTx();
};

// --- INIT ---
(async () => {
    if (state.token) {
        try {
            state.currentUser = await api('/api/bank/profile');
            $('auth-screen').style.display = 'none';
            $('app').style.display = 'block';

            [state.accounts, state.transactions] = await Promise.all([
                api('/api/bank/accounts'),
                api('/api/bank/transactions')
            ]);

            ui.renderDashboard();
            ui.renderAccounts();
        } catch (e) {
            localStorage.removeItem('nb_token');
        }
    }
})();