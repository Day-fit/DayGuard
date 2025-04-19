import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

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

    username = document.querySelector("input#username").value;

    if (!username || username.trim() === '') {
        alert("Please enter a username");
        return;
    }

    // Create a fresh SockJS connection each time
    const socket = new SockJS('/ws', null, {
        withCredentials: true
    });

    stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
            username: username
        },
        debug: function(str) {
            console.log('STOMP: ' + str);
        },
        onConnect: () => {
            console.log('STOMP connected as', username);
            document.querySelector(".popup-overlay").style.display = "none";

            // Subscribe to personal channel
            stompClient.subscribe(`/user/${username}/queue/messages`, message => {
                displayMessage(JSON.parse(message.body));
            });

            // Subscribe to public channel
            stompClient.subscribe('/topic/public', message => {
                displayMessage(JSON.parse(message.body));
            });

            // Subscribe to activity channel for active users
            stompClient.subscribe(`/user/${username}/queue/activities`, message => {
                updateActiveUsers(JSON.parse(message.body));
            });
        },
        onStompError: frame => console.error('STOMP error:', frame),
        onWebSocketError: (error) => {
            console.error('WebSocket error:', error);
        }
    });

    stompClient.activate();
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

    if (stompClient) {
        stompClient.deactivate();
        console.log("Disconnected from WebSocket");

        document.querySelector(".popup-overlay").style.display = "flex";
        clearActiveUsers();
    }
});

function sendMessage() {
    const messageInput = document.querySelector("input#message");
    const message = messageInput.value;

    if (!message || message.trim() === '') {
        return;
    }

    const chatMessage = {
        sender: username,
        receiver: selectedReceiver,
        message: message,
        type: "MESSAGE"
    };

    stompClient.publish({
        destination: '/app/publish',
        body: JSON.stringify(chatMessage)
    });

    if (selectedReceiver.trim() !== '') {
        displayMessage({...chatMessage, fromMe: true});
    }

    messageInput.value = '';
}

function displayMessage(message) {
    const messageArea = document.querySelector(".message-area");
    const messageElement = document.createElement("div");

    messageElement.classList.add("message");
    if (message.sender === username || message.fromMe) {
        messageElement.classList.add("own-message");
    } else {
        messageElement.classList.add("other-message");
    }

    const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});

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
    if (data.type === "ACTIVE_USERS") {
        activeUsers = new Set(data.users);
        renderUsersList();
    } else if (data.type === "USER_JOINED") {
        activeUsers.add(data.user);
        renderUsersList();
        displayStatusMessage(`${data.user} has joined the chat`);
    } else if (data.type === "USER_LEFT") {
        activeUsers.delete(data.user);
        renderUsersList();
        displayStatusMessage(`${data.user} has left the chat`);
    }
}

function renderUsersList() {
    const usersList = document.querySelector(".users-list");
    usersList.innerHTML = '';

    activeUsers.forEach(user => {
        if (user !== username) {
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
                document.querySelectorAll(".user-item").forEach(item => {
                    item.classList.remove("selected");
                });
                userItem.classList.add("selected");
                displayStatusMessage(`Now chatting with ${user}`);
                document.querySelector(".sidebar").classList.remove("active");
            });

            usersList.appendChild(userItem);
        }
    });
}

function clearActiveUsers() {
    activeUsers = new Set();
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