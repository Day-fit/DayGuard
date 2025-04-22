import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class ConnectionManager {
    constructor(messageManager, userListManager) {
        this.messageManager = messageManager;
        this.userListManager = userListManager;
        this.stompClient = null;
        this.username = '';
        this.attachments = [];
    }

    setUsername(username) {
        this.username = username;
    }

    initializeWebSocketConnection() {
        const socket = new SockJS('http://localhost:8080/ws', null, { withCredentials: true });

        this.stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: { username: this.username },
            onConnect: () => {
                document.querySelector(".popup-overlay").style.display = "none";

                this.stompClient.subscribe(`/user/${this.username}/queue/messages`, message => {
                    try {
                        const receivedMessage = JSON.parse(message.body);
                        if (!receivedMessage.fromMe) {
                            this.messageManager.storeMessage(receivedMessage);

                            if (receivedMessage.sender === this.userListManager.getSelectedReceiver()) {
                                this.messageManager.displayMessage(receivedMessage);
                            } else {
                                this.messageManager.incrementUnreadCount(receivedMessage.sender);
                                this.userListManager.renderUsersList();
                            }
                        }
                    } catch (error) {
                        console.error("Error processing message", error);
                    }
                });

                this.stompClient.subscribe('/topic/public', () => {});

                this.stompClient.subscribe(`/user/${this.username}/queue/activities`, message => {
                    try {
                        const activity = JSON.parse(message.body);
                        this.userListManager.updateActiveUsers(activity);
                    } catch (error) {
                        console.error("Error processing activity", error);
                    }
                });

                this.stompClient.publish({
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
            this.stompClient.activate();
        } catch (error) {
            alert('Failed to initialize WebSocket connection.');
        }
    }

    addAttachment(file) {
        const reader = new FileReader();

        reader.onload = () => {
            const base64Data = reader.result.split(',')[1];
            this.attachments.push({
                data: base64Data,
                type: file.type,
                name: file.name,
                size: file.size
            });
        };

        reader.readAsDataURL(file);
    }

    clearAttachments() {
        this.attachments = [];

        const preview = document.querySelector('.attachment-preview');
        if (preview) preview.remove();
    }

    sendMessage(message) {
        const selectedReceiver = this.userListManager.getSelectedReceiver();

        if ((!message && this.attachments.length === 0) || !selectedReceiver) return false;

        const chatMessage = {
            sender: this.username,
            receiver: selectedReceiver,
            message: message || "",
            type: this.attachments.length > 0 ? "MESSAGE_WITH_ATTACHMENT" : "TEXT_MESSAGE",
            attachments: this.attachments.length > 0 ? this.attachments : null,
        };

        this.stompClient.publish({
            destination: '/app/publish',
            body: JSON.stringify(chatMessage),
            headers: { 'content-type': 'application/json' }
        });

        const outgoingMessage = { ...chatMessage, fromMe: true };
        this.messageManager.storeMessage(outgoingMessage);
        this.messageManager.displayMessage(outgoingMessage);

        this.clearAttachments();

        return true;
    }

    disconnect() {
        if (!this.stompClient) return;

        this.stompClient.deactivate();
        document.querySelector(".popup-overlay").style.display = "flex";
        this.userListManager.clearActiveUsers();
    }
}

export default ConnectionManager;