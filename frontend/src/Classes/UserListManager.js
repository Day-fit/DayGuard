class UserListManager {
    constructor() {
        this.activeUsers = new Set();
        this.selectedReceiver = '';
        this.username = '';
        this.messageDisplay = null;
        this.updateUI = null;
    }

    setUsername(username) {
        this.username = username;
    }

    setMessageDisplay(messageDisplay) {
        this.messageDisplay = messageDisplay;
    }

    setUpdateUICallback(callback) {
        this.updateUI = callback;
    }

    updateActiveUsers(data) {
        if (!data) return;

        switch (data.type) {
            case "ACTIVE_USERS_LIST":
                if (Array.isArray(data.targetUsernames)) {
                    this.activeUsers = new Set(data.targetUsernames.filter(user => user && user.trim() !== ''));
                    this.renderUsersList();
                }
                break;
            case "JOIN":
                if (data.targetUsername && data.targetUsername.trim() !== '') {
                    this.activeUsers.add(data.targetUsername);
                    this.renderUsersList();
                    this.messageDisplay.displayStatusMessage(`${data.targetUsername} has joined the chat`);
                }
                break;
            case "LEAVE":
                if (data.targetUsername) {
                    this.activeUsers.delete(data.targetUsername);
                    this.renderUsersList();
                    this.messageDisplay.displayStatusMessage(`${data.targetUsername} has left the chat`);

                    if (data.targetUsername === this.selectedReceiver) {
                        this.selectedReceiver = '';
                        this.messageDisplay.displayStatusMessage(`${data.targetUsername} has left the chat. Please select another user to chat with.`);

                        if (this.updateUI) {
                            this.updateUI(false);
                        }
                    }
                }
                break;
        }
    }

    renderUsersList() {
        const usersList = document.querySelector(".users-list");
        usersList.innerHTML = '';

        const filteredUsers = Array.from(this.activeUsers)
            .filter(user => user && user.trim() !== '' && user !== this.username);

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

            if (user === this.selectedReceiver) {
                userItem.classList.add("selected");
            }

            const initials = user.charAt(0).toUpperCase();
            const hasUnread = this.messageDisplay.getUserUnreadCount(user) > 0;

            userItem.innerHTML = `
                <div class="user-avatar">${initials}</div>
                <span>${user}</span>
                ${hasUnread ? `<span class="unread-indicator"></span>` : ''}
            `;

            userItem.addEventListener("click", () => {
                this.selectedReceiver = user;
                document.querySelectorAll(".user-item").forEach(item => item.classList.remove("selected"));
                userItem.classList.add("selected");

                this.messageDisplay.clearUserUnreadCount(user);
                this.messageDisplay.loadUserMessages(user);
                this.messageDisplay.displayStatusMessage(`Now chatting with ${user}`);

                document.querySelector(".sidebar").classList.remove("active");
                this.renderUsersList();

                if (this.updateUI) {
                    this.updateUI(true);
                }
            });

            usersList.appendChild(userItem);
        });
    }

    clearActiveUsers() {
        this.activeUsers.clear();
        this.selectedReceiver = '';
        this.messageDisplay.clearMessages();
        document.querySelector(".users-list").innerHTML = '';

        if (this.updateUI) {
            this.updateUI(false);
        }
    }

    getSelectedReceiver() {
        return this.selectedReceiver;
    }
}