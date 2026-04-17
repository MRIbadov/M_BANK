import { $, showAlert, showToast } from './utils.js';
import { api, state } from './api.js';
import * as ui from './ui.js';

function hydrateProfile() {
    const user = state.currentUser;
    if (!user) return;

    const fullName = `${user.firstName} ${user.lastName}`;
    $('top-name').textContent = fullName;
    $('dash-greeting').textContent = `Welcome back, ${user.firstName}!`;
    $('top-avatar').textContent = user.firstName?.[0]?.toUpperCase() || '?';

    $('pf-account-number').innerHTML = state.accounts.length
        ? state.accounts.map(a => a.accountNumber).join('<br>')
        : 'No accounts yet';
    $('profile-name').textContent = fullName;
    $('profile-uname').textContent = `@${user.username}`;
    $('profile-avatar').textContent = user.firstName?.[0]?.toUpperCase() || '?';
    $('pf-first').textContent = user.firstName;
    $('pf-last').textContent = user.lastName;
    $('pf-email').textContent = user.email;
    $('pf-username').textContent = user.username;
    $('pf-phone').textContent = user.phoneNumber || '—'
}

function hydrateAccountSelectors() {
    const accountOptions = state.accounts.map(a =>
        `<option value="${a.id}">${a.accountName} (${a.accountNumber.slice(0, 14)}...) — ${a.currency} ${a.balance}</option>`
    ).join('');

    $('tf-from').innerHTML = accountOptions;
    $('tf-to').innerHTML = accountOptions;
    window.toggleTransferDestination();

    const filterOptions = state.accounts.map(a =>
        `<option value="${a.id}">${a.accountName}</option>`
    ).join('');
    $('tx-acc-filter').innerHTML = `<option value="">All Accounts</option>${filterOptions}`;
}

async function refreshAuthenticatedState() {
    state.currentUser = await api('/api/bank/profile');
    [state.accounts, state.transactions] = await Promise.all([
        api('/api/bank/accounts'),
        api('/api/bank/transactions')
    ]);

    hydrateProfile();
    hydrateAccountSelectors();
    ui.renderDashboard();
    ui.renderAccounts();
    ui.renderAllTx();
}

async function refreshOnFocus() {
    if (!state.token || document.hidden) return;

    try {
        await refreshAuthenticatedState();
    } catch (err) {
        localStorage.removeItem('nb_token');
        location.reload();
    }
}

// --- AUTH LOGIC ---
window.switchAuthTab = (tab) => {
    document.querySelectorAll('.auth-tab').forEach((t, i) =>
        t.classList.toggle('active', (i === 0 && tab === 'login') || (i === 1 && tab === 'register')));
    $('login-form').classList.toggle('active', tab === 'login');
    $('register-form').classList.toggle('active', tab === 'register');
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
        location.reload();
    } catch (err) {
        showAlert('login-error', err.message);
        btn.disabled = false;
    }
};

window.doRegister = async (e) => {
    e.preventDefault();
    const btn = $('reg-btn');
    btn.disabled = true;
    try {
        await api('/api/auth/register', 'POST', {
            firstName: $('reg-first').value,
            lastName: $('reg-last').value,
            email: $('reg-email').value,
            username: $('reg-username').value,
            password: $('reg-password').value,
            phoneNumber: $('reg-phone').value
        });

        showAlert('reg-success', 'Account created successfully. You can sign in now.');
        $('register-form').reset();
        window.switchAuthTab('login');
    } catch (err) {
        showAlert('reg-error', err.message);
    } finally {
        btn.disabled = false;
    }
};

window.logout = () => {
    localStorage.removeItem('nb_token');
    location.reload();
};

window.toggleTransferDestination = () => {
    const destinationType = $('tf-destination-type')?.value || 'DOMESTIC';
    $('tf-domestic-group').style.display = destinationType === 'DOMESTIC' ? 'block' : 'none';
    $('tf-external-group').style.display = destinationType === 'EXTERNAL' ? 'block' : 'none';
};



window.doTransfer = async () => {
    try {
        const destinationType = $('tf-destination-type').value;
        const payload = {
            fromAccountId: Number($('tf-from').value),
            destinationType,
            amount: Number($('tf-amount').value),
            description: $('tf-desc').value
        };

        if (destinationType === 'DOMESTIC') {
            payload.toAccountId = Number($('tf-to').value);
        } else {
            payload.toAccountNumber = $('tf-iban').value.trim();
        }

        await api('/api/bank/transfer', 'POST', payload);

        showAlert('transfer-alert-success', 'Transfer completed successfully.');
        window.clearTransfer();
        await refreshAuthenticatedState();
    } catch (err) {
        showAlert('transfer-alert-error', err.message);
    }
};

window.clearTransfer = () => {
    $('tf-amount').value = '';
    $('tf-desc').value = '';
    $('tf-iban').value = '';
    $('tf-destination-type').value = 'DOMESTIC';
    window.toggleTransferDestination();
};

// --- NAVIGATION ---
window.navigate = (page) => {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    $('page-' + page).classList.add('active');
    if (page === 'transactions') ui.renderAllTx();
};

window.createAccount = async () =>{
    try{
        const type = $('new-type').value;
        const name = $('new-name').value.trim() || 'New Account';


        await api('/api/bank/accounts', 'POST',{
            type, name
        });

        showAlert('new-acc-success', 'Account created successfully.');
        $('new-name').value = '';   

        await refreshAuthenticatedState();
        navigate('accounts');
    }
    catch (err) {
        showAlert('new-acc-error', err.message);
    }
}

// --- INIT ---
(async () => {
    if (!state.token) return;

    try {
        await refreshAuthenticatedState();
        $('auth-screen').style.display = 'none';
        $('app').style.display = 'block';
    } catch (e) {
        localStorage.removeItem('nb_token');
        showToast('Session expired. Please sign in again.');
    }
})();

document.addEventListener('visibilitychange', refreshOnFocus);
window.addEventListener('focus', refreshOnFocus);
