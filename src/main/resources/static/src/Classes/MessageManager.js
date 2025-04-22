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
        const messageArea = document.querySelector(".message-area");
        const messageElement = document.createElement("div");

        messageElement.classList.add("message", message.sender === this.username || message.fromMe ? "own-message" : "other-message");

        const time = message.date ? new Date(message.date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        let attachmentsHTML = '';
        if (message.attachments && message.attachments.length > 0) {
            attachmentsHTML = '<div class="message-attachments">';

            message.attachments.forEach(attachment => {
                if (attachment.type && attachment.type.startsWith('image/')) {
                    attachmentsHTML += `
                <div class="attachment-item image" data-filename="${attachment.name}" data-type="${attachment.type}" data-content="${attachment.data}">
                    <img src="data:${attachment.type};base64,${attachment.data}" alt="${attachment.name}" />
                    <div class="attachment-name">${attachment.name}</div>
                    <div class="download-icon"><i class="bx bx-download"></i></div>
                </div>
                `;
                } else {
                    attachmentsHTML += `
                <div class="attachment-item file" data-filename="${attachment.name}" data-type="${attachment.type}" data-content="${attachment.data}">
                    <div class="file-icon"><i class="bx bx-file"></i></div>
                    <div class="attachment-name">${attachment.name}</div>
                    <div class="download-icon"><i class="bx bx-download"></i></div>
                </div>
                `;
                }
            });

            attachmentsHTML += '</div>';
        }

        messageElement.innerHTML = `
        <div class="message-header">
            <span class="sender">${message.sender}</span>
            <span class="time">${time}</span>
        </div>
        <div class="message-content">${message.message}</div>
        ${attachmentsHTML}
        `;

        messageElement.querySelectorAll('.attachment-item').forEach(item => {
            item.addEventListener('click', () => {
                this.downloadAttachment(
                    item.getAttribute('data-filename'),
                    item.getAttribute('data-type'),
                    item.getAttribute('data-content')
                );
            });
        });

        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;
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

            document.body.appendChild(link);
            link.click();

            setTimeout(() => {
                document.body.removeChild(link);
                URL.revokeObjectURL(url);
            }, 100);
        } catch (error) {
            console.error('Download failed:', error);
            alert('Failed to download the file. Please try again.');
        }
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
        const messageArea = document.querySelector(".message-area");
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
        document.querySelector(".message-area").innerHTML = '';
    }

    displayStatusMessage(text) {
        const messageArea = document.querySelector(".message-area");
        const statusElement = document.createElement("div");
        statusElement.classList.add("status-message");
        statusElement.textContent = text;

        messageArea.appendChild(statusElement);
        messageArea.scrollTop = messageArea.scrollHeight;
    }

    getUserUnreadCount(user) {
        return this.unreadCounts[user] || 0;
    }

    clearUserUnreadCount(user) {
        this.unreadCounts[user] = 0;
    }
}