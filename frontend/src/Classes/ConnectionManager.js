import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class ConnectionManager {
    constructor(messageManager, userListManager) {
        this.messageManager = messageManager;
        this.userListManager = userListManager;
        this.stompClient = null;
        this.attachments = [];
        this.identifier = '';
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
    }

    setIdentifier(identifier) {
        this.identifier = identifier;
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
            `/user/${this.identifier}/queue/messages`,
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
            `/user/${this.identifier}/queue/activities`,
            (message) => {
                try {
                    const data = JSON.parse(message.body);
                    this.userListManager.updateActiveUsers(data);
                } catch (error) {
                    console.error('Error processing activity:', error);
                }
            }
        );
    }

    async notifyServerReady() {
        try {
            // Send connection ready message via STOMP
            this.stompClient.publish({
                destination: '/app/connection-ready',
                body: JSON.stringify({}),
                headers: { 'content-type': 'application/json' }
            });

            // Also ping the REST endpoint
            const response = await fetch("/api/v1/connection-ready", {
                method: "GET",
                credentials: 'include'
            });
            
            if (response.ok) {
                console.log("Server connection confirmed");
            } else {
                console.warn("Server connection check failed");
            }
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

    addAttachment(file) {
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
            return false;
        }

        if (!this.isConnected) {
            this.showNotification('Not connected to server', 'error');
            return false;
        }

        let chatMessage;

        if (this.attachments.length > 0) {
            // Create AttachmentMessageRequestDTO structure
            chatMessage = {
                receiver: selectedReceiver,
                attachments: this.attachments.map(attachment => ({
                    name: attachment.name,
                    contentType: attachment.type,
                    bytes: attachment.data,
                    size: attachment.size
                }))
            };
        } else {
            // Create TextMessageRequestDTO structure
            chatMessage = {
                receiver: selectedReceiver,
                message: message || ""
            };
        }

        try {
            this.stompClient.publish({
                destination: '/app/publish',
                body: JSON.stringify(chatMessage),
                headers: { 'content-type': 'application/json' }
            });

            // Create local message for display
            const outgoingMessage = {
                sender: this.identifier,
                receiver: selectedReceiver,
                message: message || "",
                attachments: this.attachments.length > 0 ? this.attachments : null,
                fromMe: true
            };

            this.messageManager.storeMessage(outgoingMessage);
            this.messageManager.displayMessage(outgoingMessage);

            this.clearAttachments();
            return true;
        } catch (error) {
            console.error('Error sending message:', error);
            this.showNotification('Failed to send message', 'error');
            return false;
        }
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
