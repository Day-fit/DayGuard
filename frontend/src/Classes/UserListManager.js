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
            case "IS_CONNECTED":
                {
                    this.activeUsers.add(data.targetUsername);
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
        const usersList = document.getElementById('users-list');
        usersList.innerHTML = '';

        const filteredUsers = Array.from(this.activeUsers)
            .filter(user => user && user.trim() !== '' && user !== this.username)
            .sort();

        if (filteredUsers.length === 0) {
            const emptyMessage = document.createElement('div');
            emptyMessage.className = 'text-center py-8 text-secondary-500';
            emptyMessage.innerHTML = `
                <div class="w-16 h-16 bg-secondary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <i class="bx bx-user text-2xl text-secondary-400"></i>
                </div>
                <p class="text-sm">No active users</p>
                <p class="text-xs mt-1">Other users will appear here when they join</p>
            `;
            usersList.appendChild(emptyMessage);
            return;
        }

        // Add online indicator
        const onlineIndicator = document.createElement('div');
        onlineIndicator.className = 'px-4 py-2 text-xs font-medium text-secondary-500 uppercase tracking-wider';
        onlineIndicator.textContent = `Online (${filteredUsers.length})`;
        usersList.appendChild(onlineIndicator);

        filteredUsers.forEach(user => {
            const userItem = document.createElement('div');
            userItem.className = 'user-item';
            
            if (user === this.selectedReceiver) {
                userItem.classList.add('selected');
            }

            const initials = user.charAt(0).toUpperCase();
            const hasUnread = this.messageDisplay.getUserUnreadCount(user) > 0;
            const unreadCount = this.messageDisplay.getUserUnreadCount(user);

            userItem.innerHTML = `
                <div class="user-avatar relative">
                    ${initials}
                    <div class="absolute -bottom-1 -right-1 w-3 h-3 bg-green-500 border-2 border-white rounded-full"></div>
                </div>
                <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between">
                        <span class="font-medium text-secondary-900 truncate">${user}</span>
                        ${hasUnread ? `<span class="unread-badge">${unreadCount > 99 ? '99+' : unreadCount}</span>` : ''}
                    </div>
                    <div class="text-xs text-secondary-500">
                        <span>Online</span>
                    </div>
                </div>
            `;

            userItem.addEventListener('click', () => {
                this.selectUser(user, userItem);
            });

            usersList.appendChild(userItem);
        });
    }

    selectUser(user, userItem) {
        // Update selected state
        this.selectedReceiver = user;
        document.querySelectorAll('.user-item').forEach(item => item.classList.remove('selected'));
        userItem.classList.add('selected');

        // Clear unread count and load messages
        this.messageDisplay.clearUserUnreadCount(user);
        this.messageDisplay.loadUserMessages(user);
        this.messageDisplay.displayStatusMessage(`Now chatting with ${user}`);

        // Close mobile sidebar
        const sidebar = document.getElementById('sidebar');
        const mobileOverlay = document.getElementById('mobile-overlay');
        if (sidebar && mobileOverlay) {
            sidebar.classList.add('-translate-x-full');
            mobileOverlay.classList.add('hidden');
        }

        // Update UI
        if (this.updateUI) {
            this.updateUI(true);
        }

        // Re-render to update unread badges
        this.renderUsersList();
    }

    clearActiveUsers() {
        this.activeUsers.clear();
        this.selectedReceiver = '';
        this.messageDisplay.clearMessages();
        
        const usersList = document.getElementById('users-list');
        if (usersList) {
            usersList.innerHTML = '';
        }

        if (this.updateUI) {
            this.updateUI(false);
        }
    }

    getSelectedReceiver() {
        return this.selectedReceiver;
    }

    // Add user to the list (for testing or manual addition)
    addUser(username) {
        if (username && username.trim() !== '' && username !== this.username) {
            this.activeUsers.add(username);
            this.renderUsersList();
        }
    }

    // Remove user from the list
    removeUser(username) {
        if (this.activeUsers.has(username)) {
            this.activeUsers.delete(username);
            this.renderUsersList();
            
            if (username === this.selectedReceiver) {
                this.selectedReceiver = '';
                if (this.updateUI) {
                    this.updateUI(false);
                }
            }
        }
    }

    // Get all active users
    getActiveUsers() {
        return Array.from(this.activeUsers);
    }

    // Check if user is online
    isUserOnline(username) {
        return this.activeUsers.has(username);
    }
}

export default UserListManager;