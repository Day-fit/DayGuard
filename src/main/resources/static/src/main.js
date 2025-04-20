import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient;
let username;
let selectedReceiver = '';
let activeUsers = new Set();

// Initialize sidebar functionality
document.querySelector(".menu-btn")?.addEventListener("click", () => {
    document.querySelector(".sidebar").classList.add("active");
});

document.querySelector(".close-sidebar")?.addEventListener("click", () => {
    document.querySelector(".sidebar").classList.remove("active");
});

document.querySelector("button.btn").addEventListener("click", event => {
    event.preventDefault();

    username = document.querySelector("input#username").value.trim();
    if (!username) {
        alert("Please enter a username");
        return;
    }

    initializeWebSocketConnection(username);
});

document.querySelector("button.send-btn").addEventListener("click", event => {
    event.preventDefault();
    sendMessage();
});

document.querySelector("input#message").addEventListener("keypress", event => {
    if (event.key === "Enter") {
        event.preventDefault();
        sendMessage();
    }
});

document.querySelector("button.close-btn").addEventListener("click", event => {
    event.preventDefault();
    disconnectWebSocket();
});

function initializeWebSocketConnection(username) {
    const socket = new SockJS('/ws', null, { withCredentials: true });

    stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: { username },
        onConnect: () => {
            document.querySelector(".popup-overlay").style.display = "none";

            stompClient.subscribe(`/user/${username}/queue/messages`, message => {
                try {
                    displayMessage(JSON.parse(message.body));
                } catch (e) {
                    // Silent error handling
                }
            });

            stompClient.subscribe('/topic/public', () => {});

            stompClient.subscribe(`/user/${username}/queue/activities`, message => {
                try {
                    const data = JSON.parse(message.body);
                    updateActiveUsers(data);
                } catch (e) {
                    // Silent error handling
                }
            });

            // Send connection-ready with headers
            stompClient.publish({
                destination: '/app/connection-ready',
                body: JSON.stringify({}),
                headers: { 'content-type': 'application/json' }
            });
        },
        onWebSocketError: () => {
            alert('Failed to connect to WebSocket server. Please try again.');
        },
    });

    try {
        stompClient.activate();
    } catch (error) {
        alert('Failed to initialize WebSocket connection.');
    }
}

function sendMessage() {
    const messageInput = document.querySelector("input#message");
    const message = messageInput.value.trim();

    if (!message || !selectedReceiver) return;

    const chatMessage = {
        sender: username,
        receiver: selectedReceiver,
        message,
        type: "MESSAGE",
    };

    stompClient.publish({
        destination: '/app/publish',
        body: JSON.stringify(chatMessage),
        headers: { 'content-type': 'application/json' }
    });

    displayMessage({ ...chatMessage, fromMe: true });
    messageInput.value = '';
}

function displayMessage(message) {
    const messageArea = document.querySelector(".message-area");
    const messageElement = document.createElement("div");

    messageElement.classList.add("message", message.sender === username || message.fromMe ? "own-message" : "other-message");

    const time = message.date ? new Date(message.date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    messageElement.innerHTML = `
        <div class="message-header">
            <span class="sender">${message.sender}</span>
            <span class="time">${time}</span>
        </div>
        <div class="message-content">${message.message}</div>
    `;

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function updateActiveUsers(data) {
    if (!data) return;

    switch (data.type) {
        case "ACTIVE_USERS_LIST":
            if (Array.isArray(data.targetUsernames)) {
                activeUsers = new Set(data.targetUsernames.filter(user => user && user.trim() !== ''));
                renderUsersList();
            }
            break;
        case "JOIN":
            if (data.targetUsername && data.targetUsername.trim() !== '') {
                activeUsers.add(data.targetUsername);
                renderUsersList();
                displayStatusMessage(`${data.targetUsername} has joined the chat`);
            }
            break;
        case "LEAVE":
            if (data.targetUsername) {
                activeUsers.delete(data.targetUsername);
                renderUsersList();
                displayStatusMessage(`${data.targetUsername} has left the chat`);

                // If the user we were chatting with left, clear the selected receiver
                if (data.targetUsername === selectedReceiver) {
                    selectedReceiver = '';
                    displayStatusMessage(`${data.targetUsername} has left the chat. Please select another user to chat with.`);
                }
            }
            break;
    }
}

function renderUsersList() {
    const usersList = document.querySelector(".users-list");
    usersList.innerHTML = '';

    const filteredUsers = Array.from(activeUsers)
        .filter(user => user && user.trim() !== '' && user !== username);

    if (filteredUsers.length === 0) {
        const emptyMessage = document.createElement("div");
        emptyMessage.classList.add("empty-users-message");
        emptyMessage.textContent = "No active users";
        usersList.appendChild(emptyMessage);
        return;
    }

    filteredUsers.forEach(user => {
        const userItem = document.createElement("div");
        userItem.classList.add("user-item");

        if (user === selectedReceiver) {
            userItem.classList.add("selected");
        }

        const initials = user.charAt(0).toUpperCase();

        userItem.innerHTML = `
            <div class="user-avatar">${initials}</div>
            <span>${user}</span>
        `;

        userItem.addEventListener("click", () => {
            selectedReceiver = user;
            document.querySelectorAll(".user-item").forEach(item => item.classList.remove("selected"));
            userItem.classList.add("selected");
            displayStatusMessage(`Now chatting with ${user}`);
            document.querySelector(".sidebar").classList.remove("active");
        });

        usersList.appendChild(userItem);
    });
}

function disconnectWebSocket() {
    if (!stompClient) return;

    stompClient.deactivate();

    document.querySelector(".popup-overlay").style.display = "flex";
    clearActiveUsers();
}

function clearActiveUsers() {
    activeUsers.clear();
    selectedReceiver = '';
    document.querySelector(".users-list").innerHTML = '';
}

function displayStatusMessage(text) {
    const messageArea = document.querySelector(".message-area");
    const statusElement = document.createElement("div");
    statusElement.classList.add("status-message");
    statusElement.textContent = text;

    messageArea.appendChild(statusElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}