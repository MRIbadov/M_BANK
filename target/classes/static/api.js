export const state = {
    API: '',
    token: localStorage.getItem('nb_token') || null,
    currentUser: null,
    accounts: [],
    transactions: []
};

export async function api(path, method='GET', body=null) {
    const opts = {
        method,
        headers: {
            'Content-Type':'application/json',
            ...(state.token ? {'Authorization':'Bearer '+state.token} : {})
        }
    };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(state.API + path, opts);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || `HTTP ${res.status}`);
    return data;
}