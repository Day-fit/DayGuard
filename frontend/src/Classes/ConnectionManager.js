import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import DOMPurify from 'dompurify';

class ConnectionManager {
    constructor(messageManager, userListManager) {
        this.messageManager = messageManager;
        this.userListManager = userListManager;
        this.stompClient = null;
        this.attachments = [];
        this.username = '';
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
    }

    setIdentifier(username) {
        this.username = username;
    }

    initializeWebSocketConnection() {
        this.stompClient = new Client({
            webSocketFactory: () => new SockJS('/ws', undefined, {
                withCredentials: true,
                transports: ['websocket', 'xhr-polling']
            }),
            debug: (frame) => {
                if (process.env.NODE_ENV === 'development') {
                    console.log('[STOMP DEBUG]', frame);
                }
            },
            onConnect: async () => {
                console.log('WebSocket connected successfully');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                this.showConnectionStatus('Connected', 'success');
                
                await this.setupSubscriptions();
                await this.notifyServerReady();
            },
            onStompError: (frame) => {
                console.error('STOMP protocol error:', frame);
                this.isConnected = false;
                this.showConnectionStatus('Connection error', 'error');
                this.handleReconnection();
            },
            onWebSocketClose: (event) => {
                console.log('WebSocket connection closed:', event);
                this.isConnected = false;
                this.showConnectionStatus('Connection lost', 'warning');
                this.handleReconnection();
            },
            onWebSocketError: (error) => {
                console.error('WebSocket error:', error);
                this.isConnected = false;
                this.showConnectionStatus('Connection failed', 'error');
                this.handleReconnection();
            }
        });

        this.stompClient.activate();
    }

    async setupSubscriptions() {
        // Subscribe to personal messages
        this.stompClient.subscribe(
            `/user/${this.username}/queue/messages`,
            (message) => {
                try {
                    const msg = JSON.parse(message.body);
                    if (!msg.fromMe) {
                        this.messageManager.storeMessage(msg);
                        
                        if (msg.sender === this.userListManager.getSelectedReceiver()) {
                            this.messageManager.displayMessage(msg);
                        } else {
                            this.messageManager.incrementUnreadCount(msg.sender);
                            this.userListManager.renderUsersList();
                            this.showNotification(`New message from ${msg.sender}`, 'info');
                        }
                    }
                } catch (error) {
                    console.error('Error processing message:', error);
                }
            }
        );

        // Subscribe to public topic (for system messages)
        this.stompClient.subscribe('/topic/public', (message) => {
            try {
                const data = JSON.parse(message.body);
                console.log('Public message received:', data);
            } catch (error) {
                console.error('Error processing public message:', error);
            }
        });

        // Subscribe to user activities
        this.stompClient.subscribe(
            `/user/${this.username}/queue/activities`,
            (message) => {
                try {
                    const data = JSON.parse(message.body);
                    this.userListManager.updateActiveUsers(data);
                } catch (error) {
                    console.error('Error processing activity:', error);
                }
            }
        );

        // Subscribe to error queue
        this.stompClient.subscribe(
            `/user/${this.username}/queue/errors`,
            (message) => {
                try {
                    const data = JSON.parse(message.body);
                    if (data && data.error) {
                        this.showNotification(DOMPurify.sanitize(data.error), 'error');
                    }
                } catch (error) {
                    console.error('Error processing error queue message:', error);
                }
            }
        );
    }

    async notifyServerReady() {
        try {
            // Ping Server
            this.stompClient.publish({
                destination: '/app/connection-ready',
                body: JSON.stringify(this.username),
                headers: { 'content-type': 'application/json' }
            });

        } catch (error) {
            console.error("Error notifying server:", error);
        }
    }

    handleReconnection() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
            
            this.showConnectionStatus(`Reconnecting... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`, 'warning');
            
            setTimeout(() => {
                if (!this.isConnected) {
                    console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
                    this.initializeWebSocketConnection();
                }
            }, delay);
        } else {
            this.showConnectionStatus('Connection failed. Please refresh the page.', 'error');
        }
    }

    addAttachment(file, callback) {
        // Validate file size (max 10MB)
        const maxSize = 10 * 1024 * 1024; // 10MB
        if (file.size > maxSize) {
            this.showNotification('File size must be less than 10MB', 'error');
            return false;
        }

        // Validate file type
        const allowedTypes = [
            'image/jpeg', 'image/png', 'image/gif', 'image/webp',
            'application/pdf', 'text/plain',
            'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        ];
        
        if (!allowedTypes.includes(file.type)) {
            this.showNotification('File type not supported', 'error');
            return false;
        }

        const reader = new FileReader();

        reader.onload = () => {
            const base64Data = reader.result.split(',')[1];
            this.attachments.push({
                data: base64Data,
                type: file.type,
                name: file.name,
                size: file.size
            });
            if (callback) callback();
        };

        reader.onerror = () => {
            this.showNotification('Error reading file', 'error');
        };

        reader.readAsDataURL(file);
        return true;
    }

    clearAttachments() {
        this.attachments = [];
        const preview = document.getElementById('attachment-preview-container');
        if (preview) {
            preview.classList.add('hidden');
        }
    }

    sendMessage(message) {
        const selectedReceiver = this.userListManager.getSelectedReceiver();

        if ((!message && this.attachments.length === 0) || !selectedReceiver) {
            if (!selectedReceiver) {
                this.showNotification('Please select a user to chat with first', 'warning');
            }
            return false;
        }

        if (!this.isConnected) {
            this.showNotification('Not connected to server', 'error');
            return false;
        }

        let messagesSent = 0;
        const totalMessages = (message ? 1 : 0) + (this.attachments.length > 0 ? 1 : 0);

        // Send text message first if it exists
        if (message && message.trim()) {
            const textMessage = {
                receiver: selectedReceiver,
                message: message.trim()
            };

            console.log('Sending text message:', textMessage);

            try {
                this.stompClient.publish({
                    destination: "/app/publish/text",
                    body: JSON.stringify(textMessage),
                    headers: { 'content-type': 'application/json' }
                });

                // Create a local message for display
                const outgoingTextMessage = {
                    sender: this.username,
                    receiver: selectedReceiver,
                    message: message.trim(),
                    attachments: null,
                    fromMe: true
                };

                this.messageManager.storeMessage(outgoingTextMessage);
                this.messageManager.displayMessage(outgoingTextMessage);
                messagesSent++;
            } catch (error) {
                console.error('Error sending text message:', error);
                this.showNotification('Failed to send text message', 'error');
                return false;
            }
        }

        // Send attachments if they exist
        if (this.attachments.length > 0) {
            const attachmentMessage = {
                receiver: selectedReceiver,
                attachments: this.attachments.map(attachment => ({
                    name: attachment.name,
                    data: attachment.data,
                    type: attachment.type,
                    size: attachment.size
                }))
            };

            console.log('Sending attachment message:', attachmentMessage);

            try {
                this.stompClient.publish({
                    destination: "/app/publish/attachment",
                    body: JSON.stringify(attachmentMessage),
                    headers: { 'content-type': 'application/json' }
                });

                // Create a local message for display
                const outgoingAttachmentMessage = {
                    sender: this.username,
                    receiver: selectedReceiver,
                    message: "",
                    attachments: this.attachments,
                    fromMe: true
                };

                this.messageManager.storeMessage(outgoingAttachmentMessage);
                this.messageManager.displayMessage(outgoingAttachmentMessage);
                messagesSent++;
            } catch (error) {
                console.error('Error sending attachment message:', error);
                this.showNotification('Failed to send attachment message', 'error');
                return false;
            }
        }

        // Clear attachments after successful sending
        this.clearAttachments();
        
        return messagesSent === totalMessages;
    }

    disconnect() {
        if (this.stompClient) {
            this.isConnected = false;
            this.stompClient.deactivate();
            this.userListManager.clearActiveUsers();
        }
    }

    showConnectionStatus(message, type = 'info') {
        // Update connection status in UI if needed
        const statusElement = document.getElementById('connection-status');
        if (statusElement) {
            statusElement.textContent = message;
            statusElement.className = `text-sm ${type === 'success' ? 'text-green-600' : type === 'error' ? 'text-red-600' : 'text-yellow-600'}`;
        }
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg max-w-sm animate-slide-up`;
        
        const bgColor = type === 'error' ? 'bg-red-500' : type === 'success' ? 'bg-green-500' : type === 'warning' ? 'bg-yellow-500' : 'bg-primary-500';
        notification.className += ` ${bgColor} text-white`;
        
        notification.innerHTML = `
            <div class="flex items-center gap-3">
                <i class="bx ${type === 'error' ? 'bx-error' : type === 'success' ? 'bx-check' : type === 'warning' ? 'bx-warning' : 'bx-info-circle'} text-xl"></i>
                <span>${message}</span>
                <button onclick="this.parentElement.parentElement.remove()" class="ml-auto text-white hover:text-gray-200">
                    <i class="bx bx-x text-lg"></i>
                </button>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 5000);
    }

    // Get connection status
    getConnectionStatus() {
        return this.isConnected;
    }

    // Get current attachments
    getAttachments() {
        return this.attachments;
    }
}

export default ConnectionManager;
