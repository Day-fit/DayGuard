import ConnectionManager from './Classes/ConnectionManager.js';

const messageManager = new MessageManager();
const userListManager = new UserListManager();
const connectionManager = new ConnectionManager(messageManager, userListManager);

userListManager.setMessageDisplay(messageManager);
userListManager.setUpdateUICallback(updateUIForSelectedUser);

const authContainer = document.getElementById('auth-container');
const chatContainer = document.getElementById('chat-container');
const loginTab = document.getElementById('login-tab');
const registerTab = document.getElementById('register-tab');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const loginError = document.getElementById('login-error');
const registerError = document.getElementById('register-error');
const currentUserSpan = document.getElementById('current-user');
const logoutBtn = document.getElementById('logout-btn');
const usersList = document.getElementById('users-list');
const messageArea = document.getElementById('message-area');
const noUserSelected = document.getElementById('no-user-selected');
const messageFormContainer = document.getElementById('message-form-container');
const messageInput = document.getElementById('message-input');
const sendBtn = document.getElementById('send-btn');
const attachmentBtn = document.getElementById('attachment-btn');
const fileInput = document.getElementById('file-input');

let username = null;

loginTab.addEventListener('click', () => {
    loginTab.classList.add('bg-indigo-100', 'text-indigo-600');
    loginTab.classList.remove('text-gray-500');
    registerTab.classList.remove('bg-indigo-100', 'text-indigo-600');
    registerTab.classList.add('text-gray-500');
    loginForm.classList.remove('hidden');
    registerForm.classList.add('hidden');
    loginError.classList.add('hidden');
    registerError.classList.add('hidden');
});
registerTab.addEventListener('click', () => {
    registerTab.classList.add('bg-indigo-100', 'text-indigo-600');
    registerTab.classList.remove('text-gray-500');
    loginTab.classList.remove('bg-indigo-100', 'text-indigo-600');
    loginTab.classList.add('text-gray-500');
    registerForm.classList.remove('hidden');
    loginForm.classList.add('hidden');
    loginError.classList.add('hidden');
    registerError.classList.add('hidden');
});

// --- Auth: Register ---
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    registerError.classList.add('hidden');
    const regUsername = document.getElementById('register-username').value.trim();
    const regEmail = document.getElementById('register-email').value.trim();
    const regPassword = document.getElementById('register-password').value;
    try {
        const res = await fetch('/api/v1/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: regUsername, email: regEmail, password: regPassword }),
        });
        if (res.status === 200) {
            await loginUser(regUsername, regPassword);
        } else {
            const data = await res.json();
            registerError.textContent = data.message || 'Błąd rejestracji';
            registerError.classList.remove('hidden');
        }
    } catch {
        registerError.textContent = 'Błąd połączenia z serwerem';
        registerError.classList.remove('hidden');
    }
});

// --- Auth: Login ---
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginError.classList.add('hidden');
    const logUsername = document.getElementById('login-username').value.trim();
    const logPassword = document.getElementById('login-password').value;
    await loginUser(logUsername, logPassword);
});

async function loginUser(logUsername, logPassword) {
    try {
        const res = await fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ username: logUsername, password: logPassword }),
        });
        if (res.status === 200) {
            username = logUsername;
            showChat();
        } else {
            const data = await res.json();
            loginError.textContent = data.message || 'Błędny login lub hasło';
            loginError.classList.remove('hidden');
        }
    } catch {
        loginError.textContent = 'Błąd połączenia z serwerem';
        loginError.classList.remove('hidden');
    }
}

// --- Auth: Refresh Token (on load) ---
async function tryRefreshToken() {
    try {
        const res = await fetch('/api/v1/auth/refresh', {
            method: 'POST',
            credentials: 'include'
        });
        if (res.status === 200) {
            // Można pobrać username z backendu jeśli jest endpoint, tu zakładamy, że nie
            return true;
        }
    } catch {}
    return false;
}

// --- Logout ---
logoutBtn.addEventListener('click', () => {
    document.cookie = 'accessToken=; Max-Age=0; path=/;';
    document.cookie = 'refreshToken=; Max-Age=0; path=/;';
    location.reload();
});

// --- Show Chat UI ---
function showChat() {
    authContainer.classList.add('hidden');
    chatContainer.classList.remove('hidden');
    currentUserSpan.textContent = username;
    messageInput.disabled = true;
    sendBtn.disabled = true;
    // Inicjalizacja połączenia
    messageManager.setUsername(username);
    userListManager.setUsername(username);
    connectionManager.setUsername(username);
    connectionManager.initializeWebSocketConnection();
}

// --- User List & Messaging UI ---
userListManager.setUpdateUICallback((hasSelectedUser) => {
    if (hasSelectedUser) {
        noUserSelected.classList.add('hidden');
        messageInput.disabled = false;
        sendBtn.disabled = false;
    } else {
        noUserSelected.classList.remove('hidden');
        messageInput.disabled = true;
        sendBtn.disabled = true;
    }
});

// --- Obsługa wysyłania wiadomości ---
sendBtn.addEventListener('click', (e) => {
    e.preventDefault();
    const message = messageInput.value.trim();
    if (connectionManager.sendMessage(message)) {
        messageInput.value = '';
    }
});
messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        const message = messageInput.value.trim();
        if (connectionManager.sendMessage(message)) {
            messageInput.value = '';
        }
    }
});

// --- Obsługa załączników ---
attachmentBtn.addEventListener('click', () => fileInput.click());
fileInput.addEventListener('change', (event) => {
    if (event.target.files.length > 0) {
        Array.from(event.target.files).forEach(file => {
            connectionManager.addAttachment(file);
        });
        event.target.value = '';
    }
});

// --- Inicjalizacja na starcie ---
(async function init() {
    // Spróbuj odświeżyć token (jeśli user już zalogowany)
    if (await tryRefreshToken()) {
        // Można pobrać username z backendu jeśli jest endpoint, tu zakładamy, że nie
        // Wymuś wpisanie loginu jeśli nie ma username w cookie
        authContainer.classList.remove('hidden');
        chatContainer.classList.add('hidden');
    } else {
        authContainer.classList.remove('hidden');
        chatContainer.classList.add('hidden');
    }
})();

function updateUIForSelectedUser(hasSelectedUser) {
    if (hasSelectedUser) {
        noUserSelected.classList.add("hidden");
        messageInput.disabled = false;
        sendBtn.disabled = false;
    } else {
        noUserSelected.classList.remove("hidden");
        messageInput.disabled = true;
        sendBtn.disabled = true;
    }
}
