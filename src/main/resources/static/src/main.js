import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient;
let username;

document.querySelector("button.btn").addEventListener("click", event => {
    event.preventDefault();

    username = document.querySelector("input#username").value;

    if (!username || username.trim() === '') {
        alert("Please enter a username");
        return;
    }

    // Create a fresh SockJS connection each time
    const socket = new SockJS('http://localhost:8080/ws', null, {
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
    }
});

function sendMessage() {
    const messageInput = document.querySelector("input#message");
    const receiverInput = document.querySelector("input#receiver");

    const message = messageInput.value;
    const receiver = receiverInput.value;

    if (!message || message.trim() === '') {
        return;
    }

    const chatMessage = {
        sender: username,
        receiver: receiver,
        message: message,
        type: "MESSAGE"
    };

    stompClient.publish({
        destination: '/app/publish',
        body: JSON.stringify(chatMessage)
    });

    if (receiver.trim() !== '') {
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