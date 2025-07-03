import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class ConnectionManager {
    constructor(messageManager, userListManager) {
        this.messageManager = messageManager;
        this.userListManager = userListManager;
        this.stompClient = null;
        this.attachments = [];
        this.identifier = '';
    }

    setIdentifier(identifier)
    {
        this.identifier = identifier;
    }

    initializeWebSocketConnection() {
        this.stompClient = new Client({
            webSocketFactory: () => new SockJS('/ws', undefined, {
                withCredentials: true,
                transports: ['websocket', 'xhr-polling']
            }),
            debug: frame => console.log('[STOMP DEBUG]', frame),
            onConnect: () => {
                document.querySelector('.popup-overlay').style.display = 'none';

                this.stompClient.subscribe(
                    `/user/${this.identifier}/queue/messages`,
                    message => {
                        const msg = JSON.parse(message.body);
                        if (!msg.fromMe) {
                            this.messageManager.storeMessage(msg);
                            msg.sender === this.userListManager.getSelectedReceiver()
                                ? this.messageManager.displayMessage(msg)
                                : (this.messageManager.incrementUnreadCount(msg.sender), this.userListManager.renderUsersList());
                        }
                    }
                );

                this.stompClient.subscribe('/topic/public', () => {});

                this.stompClient.subscribe(
                    `/user/${this.identifier}/queue/activities`,
                    message => this.userListManager.updateActiveUsers(JSON.parse(message.body))
                );

                this.stompClient.publish({
                    destination: '/app/connection-ready',
                    body: JSON.stringify({}),
                    headers: { 'content-type': 'application/json' }
                });
            },
            onStompError: frame => alert(`STOMP protocol error: ${frame.body}`),
            onWebSocketClose: event => alert('WebSocket connection closed'),
        });

        this.stompClient.activate();
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
            sender: this.identifier,
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
