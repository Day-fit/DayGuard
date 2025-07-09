import DOMPurify from 'dompurify';

class MessageManager {
    constructor() {
        this.username = '';
        this.userMessages = {};
        this.unreadCounts = {};
    }

    setUsername(username) {
        this.username = username;
    }

    displayMessage(message) {
        const messageArea = document.getElementById('message-area');
        const messageElement = document.createElement('div');
        
        const isOwnMessage = message.sender === this.username || message.fromMe;
        const time = message.date ? new Date(message.date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        messageElement.className = `flex items-start ${isOwnMessage ? 'justify-end' : 'justify-start'} mb-4 animate-slide-up`;

        // Avatar (always on the left)
        const avatar = document.createElement('div');
        avatar.className = 'user-avatar flex-shrink-0 mr-3';
        avatar.textContent = DOMPurify.sanitize(message.sender.charAt(0).toUpperCase());
        messageElement.appendChild(avatar);

        const messageContent = document.createElement('div');
        messageContent.className = `max-w-xs lg:max-w-md`;
        
        // Message bubble
        const bubble = document.createElement('div');
        bubble.className = `message-bubble ${isOwnMessage ? 'message-own' : 'message-other'}`;
        
        // Message header (sender name and time)
        const header = document.createElement('div');
        header.className = `flex items-center mb-1 ${isOwnMessage ? 'text-primary-100' : 'text-secondary-600'}`;
        header.innerHTML = `
            <span class="text-xs font-medium mr-2">${DOMPurify.sanitize(message.sender)}</span>
            <span class="text-xs">${time}</span>
        `;
        
        // Message text content
        const textContent = document.createElement('div');
        textContent.className = 'whitespace-pre-wrap';
        textContent.textContent = message.message ? DOMPurify.sanitize(message.message) : '';
        
        bubble.appendChild(header);
        bubble.appendChild(textContent);
        
        // Handle attachments
        if (message.attachments && message.attachments.length > 0) {
            const attachmentsContainer = document.createElement('div');
            attachmentsContainer.className = 'mt-3 space-y-2';
            
            message.attachments.forEach(attachment => {
                const attachmentElement = this.createAttachmentElement(attachment);
                attachmentsContainer.appendChild(attachmentElement);
            });
            
            bubble.appendChild(attachmentsContainer);
        }
        
        messageContent.appendChild(bubble);
        messageElement.appendChild(messageContent);
        
        messageArea.appendChild(messageElement);
        this.scrollToBottom(messageArea);
    }

    createAttachmentElement(attachment) {
        const attachmentDiv = document.createElement('div');
        attachmentDiv.className = 'attachment-item cursor-pointer hover:bg-secondary-50 transition-colors duration-200';
        attachmentDiv.setAttribute('data-filename', DOMPurify.sanitize(attachment.name));
        attachmentDiv.setAttribute('data-type', DOMPurify.sanitize(attachment.type));
        attachmentDiv.setAttribute('data-content', attachment.data);
        
        if (attachment.type && attachment.type.startsWith('image/')) {
            attachmentDiv.innerHTML = `
                <div class="relative group">
                    <img src="data:${DOMPurify.sanitize(attachment.type)};base64,${attachment.data}" 
                         alt="${DOMPurify.sanitize(attachment.name)}" 
                         class="w-full h-32 object-cover rounded-lg border border-secondary-200" />
                    <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition-all duration-200 rounded-lg flex items-center justify-center">
                        <div class="opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                            <i class="bx bx-download text-white text-2xl"></i>
                        </div>
                    </div>
                    <div class="mt-1 text-xs text-secondary-600 truncate">${DOMPurify.sanitize(attachment.name)}</div>
                </div>
            `;
        } else {
            attachmentDiv.innerHTML = `
                <div class="flex items-center gap-3 p-3 bg-secondary-50 rounded-lg border border-secondary-200">
                    <div class="w-10 h-10 bg-secondary-200 rounded-lg flex items-center justify-center">
                        <i class="bx bx-file text-xl text-secondary-600"></i>
                    </div>
                    <div class="flex-1 min-w-0">
                        <div class="text-sm font-medium text-secondary-900 truncate">${DOMPurify.sanitize(attachment.name)}</div>
                        <div class="text-xs text-secondary-500">${this.formatFileSize(attachment.size)}</div>
                    </div>
                    <button class="text-primary-600 hover:text-primary-700">
                        <i class="bx bx-download text-lg"></i>
                    </button>
                </div>
            `;
        }
        
        attachmentDiv.addEventListener('click', () => {
            this.downloadAttachment(
                attachmentDiv.getAttribute('data-filename'),
                attachmentDiv.getAttribute('data-type'),
                attachmentDiv.getAttribute('data-content')
            );
        });
        
        return attachmentDiv;
    }

    downloadAttachment(filename, mimetype, base64Data) {
        try {
            const byteCharacters = atob(base64Data);
            const byteArrays = [];

            for (let offset = 0; offset < byteCharacters.length; offset += 512) {
                const slice = byteCharacters.slice(offset, offset + 512);
                const byteNumbers = new Array(slice.length);
                
                for (let i = 0; i < slice.length; i++) {
                    byteNumbers[i] = slice.charCodeAt(i);
                }

                const byteArray = new Uint8Array(byteNumbers);
                byteArrays.push(byteArray);
            }

            const blob = new Blob(byteArrays, { type: mimetype });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            link.style.display = 'none';

            document.body.appendChild(link);
            link.click();

            setTimeout(() => {
                document.body.removeChild(link);
                URL.revokeObjectURL(url);
            }, 100);
        } catch (error) {
            console.error('Download failed:', error);
            this.showNotification('Failed to download the file. Please try again.', 'error');
        }
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    storeMessage(message) {
        const otherUser = message.fromMe || message.sender === this.username ? message.receiver : message.sender;

        if (!this.userMessages[otherUser]) {
            this.userMessages[otherUser] = [];
        }

        this.userMessages[otherUser].push(message);
    }

    incrementUnreadCount(sender) {
        if (!this.unreadCounts[sender]) {
            this.unreadCounts[sender] = 0;
        }
        this.unreadCounts[sender]++;
    }

    loadUserMessages(user) {
        const messageArea = document.getElementById('message-area');
        messageArea.innerHTML = '';

        if (this.userMessages[user]) {
            this.userMessages[user].forEach(message => {
                this.displayMessage(message);
            });
        }
    }

    clearMessages() {
        this.userMessages = {};
        this.unreadCounts = {};
        const messageArea = document.getElementById('message-area');
        messageArea.innerHTML = '';
        
        // Show no user selected state
        const noUserSelected = document.getElementById('no-user-selected');
        if (noUserSelected) {
            noUserSelected.classList.remove('hidden');
        }
    }

    displayStatusMessage(text) {
        const messageArea = document.getElementById('message-area');
        const statusElement = document.createElement('div');
        statusElement.className = 'flex justify-center my-4 animate-fade-in';
        
        const statusContent = document.createElement('div');
        statusContent.className = 'status-message bg-secondary-100 px-4 py-2 rounded-full text-secondary-600 text-sm';
        statusContent.textContent = text;
        
        statusElement.appendChild(statusContent);
        messageArea.appendChild(statusElement);
        this.scrollToBottom(messageArea);
    }

    getUserUnreadCount(user) {
        return this.unreadCounts[user] || 0;
    }

    clearUserUnreadCount(user) {
        this.unreadCounts[user] = 0;
    }

    scrollToBottom(element) {
        setTimeout(() => {
            element.scrollTop = element.scrollHeight;
        }, 100);
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg max-w-sm animate-slide-up`;
        
        const bgColor = type === 'error' ? 'bg-red-500' : type === 'success' ? 'bg-green-500' : 'bg-primary-500';
        notification.className += ` ${bgColor} text-white`;
        
        notification.innerHTML = `
            <div class="flex items-center gap-3">
                <i class="bx ${type === 'error' ? 'bx-error' : type === 'success' ? 'bx-check' : 'bx-info-circle'} text-xl"></i>
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
}

export default MessageManager;