import ConnectionManager from './Classes/ConnectionManager.js';
import MessageManager from './Classes/MessageManager.js';
import UserListManager from './Classes/UserListManager.js';
import DOMPurify from 'dompurify';
import validator from 'validator';
import zxcvbn from 'zxcvbn';

// Initialize managers
const messageManager = new MessageManager();
const userListManager = new UserListManager();
const connectionManager = new ConnectionManager(messageManager, userListManager);

// Set up manager connections
userListManager.setMessageDisplay(messageManager);
userListManager.setUpdateUICallback(updateUIForSelectedUser);

// DOM Elements
const loadingScreen = document.getElementById('loading-screen');
const authContainer = document.getElementById('auth-container');
const chatContainer = document.getElementById('chat-container');
const loginTab = document.getElementById('login-tab');
const registerTab = document.getElementById('register-tab');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const loginError = document.getElementById('login-error');
const registerError = document.getElementById('register-error');
const currentUserSpan = document.getElementById('current-user');
const currentUserMobile = document.getElementById('current-user-mobile');
const logoutBtn = document.getElementById('logout-btn');
const logoutBtnMobile = document.getElementById('logout-btn-mobile');
const noUserSelected = document.getElementById('no-user-selected');
const messageInput = document.getElementById('message-input');
const sendBtn = document.getElementById('send-btn');
const attachmentBtn = document.getElementById('attachment-btn');
const fileInput = document.getElementById('file-input');
const attachmentPreviewContainer = document.getElementById('attachment-preview-container');
const attachmentPreview = document.getElementById('attachment-preview');

// Mobile elements
const mobileMenuBtn = document.getElementById('mobile-menu-btn');
const closeSidebarBtn = document.getElementById('close-sidebar');
const sidebar = document.getElementById('sidebar');
const mobileOverlay = document.getElementById('mobile-overlay');

let username = null;
let email = null;
let isTyping = false;
let typingTimeout = null;

// Initialize the application
(async function init() {
    showLoading();
    
    try {
        // First try to refresh the token
        const tokenRefreshed = await tryRefreshToken();
        
        // Then try to get user details (regardless of token refresh result)
        const res = await fetch('/api/v1/get-user-details', {
            method: 'GET',
            credentials: 'include'
        });

        if (res.ok) {
            const userData = await res.json();
            username = userData.username;
            email = userData.email;
            showChat();
        } else {
            showAuth();
        }
    } catch (error) {
        console.error('Initialization error:', error);
        showAuth();
    } finally {
        hideLoading();
    }
})();

// Loading screen management
function showLoading() {
    loadingScreen.classList.remove('hidden');
}

function hideLoading() {
    loadingScreen.classList.add('hidden');
}

// Show/hide containers
function showAuth() {
    authContainer.classList.remove('hidden');
    chatContainer.classList.add('hidden');
}

function showChat() {
    authContainer.classList.add('hidden');
    chatContainer.classList.remove('hidden');
    currentUserSpan.textContent = username;
    currentUserMobile.textContent = username;
    
    // Disable input initially
    messageInput.disabled = true;
    sendBtn.disabled = true;

    // Initialize managers
    messageManager.setUsername(username);
    userListManager.setUsername(username);
    connectionManager.setIdentifier(username);
    connectionManager.initializeWebSocketConnection();
}

// Tab switching
loginTab.addEventListener('click', () => {
    switchTab('login');
});

registerTab.addEventListener('click', () => {
    switchTab('register');
});

function switchTab(activeTab) {
    if (activeTab === 'login') {
        loginTab.classList.add('text-primary-600', 'bg-white', 'shadow-sm');
        loginTab.classList.remove('text-secondary-600');
        registerTab.classList.remove('text-primary-600', 'bg-white', 'shadow-sm');
        registerTab.classList.add('text-secondary-600');
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
        hideErrors();
    } else {
        registerTab.classList.add('text-primary-600', 'bg-white', 'shadow-sm');
        registerTab.classList.remove('text-secondary-600');
        loginTab.classList.remove('text-primary-600', 'bg-white', 'shadow-sm');
        loginTab.classList.add('text-secondary-600');
        registerForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
        hideErrors();
    }
}

function hideErrors() {
    loginError.classList.add('hidden');
    registerError.classList.add('hidden');
}

// Registration form
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideErrors();
    
    const formData = new FormData(registerForm);
    let regUsername = formData.get('register-username').trim();
    let regEmail = formData.get('register-email').trim();
    let regPassword = formData.get('register-password');

    // Sanitize inputs
    regUsername = DOMPurify.sanitize(regUsername);
    regEmail = DOMPurify.sanitize(regEmail);
    regPassword = DOMPurify.sanitize(regPassword);

    // Validate email
    if (!validateEmail(regEmail)) {
        showError(registerError, 'Invalid email address.');
        return;
    }
    // Validate username (alphanumeric, 3-20 chars)
    if (!validateUsername(regUsername)) {
        showError(registerError, 'Username must be 3-20 alphanumeric characters.');
        return;
    }
    // Validate password
    if (!validatePassword(regPassword)) {
        showError(registerError, 'Password is too weak. Use at least 8 characters and a mix of types.');
        return;
    }

    try {
        const res = await fetch('/api/v1/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: regUsername, email: regEmail, password: regPassword }),
        });
        
        if (res.ok) {
            await loginUser(regUsername, regPassword);
        } else {
            const data = await res.json();
            showError(registerError, data.message || 'Registration failed');
        }
    } catch (error) {
        showError(registerError, 'Connection error. Please try again.');
    }
});

// Password strength meter for registration
const registerPasswordInput = document.getElementById('register-password');
const passwordStrengthBar = document.getElementById('password-strength-bar');
const passwordStrengthLabel = document.getElementById('password-strength-label');

if (registerPasswordInput) {
    registerPasswordInput.addEventListener('input', (e) => {
        const value = e.target.value;
        if (!value) {
            passwordStrengthBar.style.width = '0%';
            passwordStrengthBar.className = 'h-2 rounded bg-red-400 transition-all duration-300';
            passwordStrengthLabel.textContent = '';
            return;
        }
        const result = zxcvbn(value);
        let width = '0%';
        let color = 'bg-red-400';
        let label = '';
        switch (result.score) {
            case 0:
                width = '20%';
                color = 'bg-red-400';
                label = 'Very Weak';
                break;
            case 1:
                width = '40%';
                color = 'bg-orange-400';
                label = 'Weak';
                break;
            case 2:
                width = '60%';
                color = 'bg-yellow-400';
                label = 'Fair';
                break;
            case 3:
                width = '80%';
                color = 'bg-blue-400';
                label = 'Good';
                break;
            case 4:
                width = '100%';
                color = 'bg-green-500';
                label = 'Strong';
                break;
        }
        passwordStrengthBar.style.width = width;
        passwordStrengthBar.className = `h-2 rounded transition-all duration-300 ${color}`;
        passwordStrengthLabel.textContent = label;
    });
}

// Login form
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideErrors();
    
    const formData = new FormData(loginForm);
    let logIdentifier = formData.get('login-identifier').trim();
    let logPassword = formData.get('login-password');

    // Sanitize inputs
    logIdentifier = DOMPurify.sanitize(logIdentifier);
    logPassword = DOMPurify.sanitize(logPassword);

    await loginUser(logIdentifier, logPassword);
});

async function loginUser(identifier, password) {
    try {
        const res = await fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ identifier: identifier, password: password }),
        });
        
        if (res.ok) {
            // Get user details after successful login
            const userRes = await fetch('/api/v1/get-user-details', {
                method: 'GET',
                credentials: 'include'
            });
            
            if (userRes.ok) {
                const userData = await userRes.json();
                username = userData.username;
                email = userData.email;
                showChat();
            } else {
                // Fallback to identifier if user details fail
                username = identifier;
                showChat();
            }
        } else {
            const data = await res.json();
            showError(loginError, data.message || 'Invalid credentials');
        }
    } catch (error) {
        showError(loginError, 'Connection error. Please try again.');
    }
}

function showError(errorElement, message) {
    errorElement.textContent = message;
    errorElement.classList.remove('hidden');
}

// Token refresh
async function tryRefreshToken() {
    try {
        const res = await fetch('/api/v1/auth/refresh', {
            method: 'POST',
            credentials: 'include'
        });
        return res.ok;
    } catch (error) {
        return false;
    }
}

// Logout
function logout() {
    fetch('/api/v1/auth/logout', {
        method: 'POST',
        credentials: 'include'
    }).finally(() => {
        location.reload();
    });
}

logoutBtn.addEventListener('click', logout);
logoutBtnMobile.addEventListener('click', logout);

// Mobile sidebar management
mobileMenuBtn.addEventListener('click', () => {
    sidebar.classList.remove('-translate-x-full');
    mobileOverlay.classList.remove('hidden');
});

closeSidebarBtn.addEventListener('click', closeMobileSidebar);
mobileOverlay.addEventListener('click', closeMobileSidebar);

function closeMobileSidebar() {
    sidebar.classList.add('-translate-x-full');
    mobileOverlay.classList.add('hidden');
}

// User selection UI updates
function updateUIForSelectedUser(hasSelectedUser) {
    if (hasSelectedUser) {
        noUserSelected.classList.add('hidden');
        messageInput.disabled = false;
        sendBtn.disabled = false;
        messageInput.focus();
    } else {
        noUserSelected.classList.remove('hidden');
        messageInput.disabled = true;
        sendBtn.disabled = true;
    }
}

// Message sending
sendBtn.addEventListener('click', sendMessage);
messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});

// Auto-resize textarea
messageInput.addEventListener('input', () => {
    messageInput.style.height = 'auto';
    messageInput.style.height = Math.min(messageInput.scrollHeight, 120) + 'px';
});

function sendMessage() {
    const message = DOMPurify.sanitize(messageInput.value.trim());
    if (connectionManager.sendMessage(message)) {
        messageInput.value = '';
        messageInput.style.height = 'auto';
        closeMobileSidebar(); // Close sidebar on mobile after sending
    }
}

// File attachments
attachmentBtn.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', (event) => {
    if (event.target.files.length > 0) {
        Array.from(event.target.files).forEach(file => {
            connectionManager.addAttachment(file, showAttachmentPreview);
        });
        event.target.value = '';
    }
});

function showAttachmentPreview() {
    if (connectionManager.attachments.length > 0) {
        attachmentPreviewContainer.classList.remove('hidden');
        attachmentPreview.innerHTML = '';
        
        connectionManager.attachments.forEach((attachment, index) => {
            const previewItem = document.createElement('div');
            previewItem.className = 'attachment-item';
            
            if (attachment.type && attachment.type.startsWith('image/')) {
                previewItem.innerHTML = `
                    <img src="data:${attachment.type};base64,${attachment.data}" alt="${attachment.name}" class="w-12 h-12 object-cover rounded" />
                    <div class="flex-1 min-w-0">
                        <div class="text-sm font-medium text-secondary-900 truncate">${attachment.name}</div>
                        <div class="text-xs text-secondary-500">${formatFileSize(attachment.size)}</div>
                    </div>
                    <button onclick="removeAttachment(${index})" class="text-red-500 hover:text-red-700">
                        <i class="bx bx-x text-lg"></i>
                    </button>
                `;
            } else {
                previewItem.innerHTML = `
                    <div class="w-12 h-12 bg-secondary-100 rounded flex items-center justify-center">
                        <i class="bx bx-file text-xl text-secondary-600"></i>
                    </div>
                    <div class="flex-1 min-w-0">
                        <div class="text-sm font-medium text-secondary-900 truncate">${attachment.name}</div>
                        <div class="text-xs text-secondary-500">${formatFileSize(attachment.size)}</div>
                    </div>
                    <button onclick="removeAttachment(${index})" class="text-red-500 hover:text-red-700">
                        <i class="bx bx-x text-lg"></i>
                    </button>
                `;
            }
            
            attachmentPreview.appendChild(previewItem);
        });
    } else {
        attachmentPreviewContainer.classList.add('hidden');
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Global function for removing attachments
window.removeAttachment = function(index) {
    connectionManager.attachments.splice(index, 1);
    showAttachmentPreview();
};

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    if (e.ctrlKey || e.metaKey) {
        switch (e.key) {
            case 'k':
                e.preventDefault();
                if (!chatContainer.classList.contains('hidden')) {
                    messageInput.focus();
                }
                break;
            case 'l':
                e.preventDefault();
                logout();
                break;
        }
    }
});

// Handle window resize
window.addEventListener('resize', () => {
    if (window.innerWidth >= 1024) { // lg breakpoint
        closeMobileSidebar();
    }
});

// Validation helpers for unit testing
export function validateEmail(email) {
    return validator.isEmail(email) && validator.isLength(email, { min: 6, max: 254 });
}

export function validateUsername(username) {
    return validator.isAlphanumeric(username) && validator.isLength(username, { min: 3, max: 20 });
}

export function validatePassword(password) {
    const pwStrength = zxcvbn(password);
    if (password.length < 8 || pwStrength.score < 2) return false;
    return password.length <= 64;
}
