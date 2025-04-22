import ConnectionManager from './Classes/ConnectionManager.js';

const messageManager = new MessageManager();
const userListManager = new UserListManager();
const connectionManager = new ConnectionManager(messageManager, userListManager);

userListManager.setMessageDisplay(messageManager);
userListManager.setUpdateUICallback(updateUIForSelectedUser);

document.querySelector(".menu-btn")?.addEventListener("click", () => {
    document.querySelector(".sidebar").classList.add("active");
});

document.querySelector(".close-sidebar")?.addEventListener("click", () => {
    document.querySelector(".sidebar").classList.remove("active");
});

document.querySelector(".show-users-btn")?.addEventListener("click", () => {
    document.querySelector(".sidebar").classList.add("active");
});

document.querySelector(".attachment-btn").addEventListener("click", () => {
    document.getElementById("file-input").click();
});

document.querySelector("button.btn").addEventListener("click", event => {
    event.preventDefault();

    const username = document.querySelector("input#username").value.trim();
    if (!username) {
        alert("Please enter a username");
        return;
    }

    messageManager.setUsername(username);
    userListManager.setUsername(username);
    connectionManager.setUsername(username);
    connectionManager.initializeWebSocketConnection();
});

document.querySelector("button.send-btn").addEventListener("click", event => {
    event.preventDefault();
    const messageInput = document.querySelector("input#message");
    const message = messageInput.value.trim();

    if (connectionManager.sendMessage(message)) {
        messageInput.value = '';
    }
});

document.querySelector("input#message").addEventListener("keypress", event => {
    if (event.key === "Enter") {
        event.preventDefault();
        const messageInput = document.querySelector("input#message");
        const message = messageInput.value.trim();

        if (connectionManager.sendMessage(message)) {
            messageInput.value = '';
        }
    }
});

document.querySelector("button.close-btn").addEventListener("click", event => {
    event.preventDefault();
    connectionManager.disconnect();
});

document.getElementById("file-input").addEventListener("change", (event) => {
    if (event.target.files.length > 0) {
        const attachmentPreview = createAttachmentPreview();

        Array.from(event.target.files).forEach(file => {
            connectionManager.addAttachment(file);

            const fileElement = document.createElement('div');
            fileElement.classList.add('file-item');

            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    fileElement.innerHTML = `
                        <div class="file-preview">
                            <img src="${e.target.result}" alt="${file.name}">
                        </div>
                        <div class="file-name">${file.name}</div>
                    `;
                };
                reader.readAsDataURL(file);
            } else {
                fileElement.innerHTML = `
                    <div class="file-icon"><i class="bx bx-file"></i></div>
                    <div class="file-name">${file.name}</div>
                `;
            }

            attachmentPreview.appendChild(fileElement);
        });

        event.target.value = '';
    }
});

function updateUIForSelectedUser(hasSelectedUser) {
    const noUserSelectedEl = document.querySelector(".no-user-selected");
    const messageAreaEl = document.querySelector(".message-area");
    const messageFormEl = document.querySelector(".message-form");
    const messageInput = document.querySelector("input#message");
    const sendBtn = document.querySelector(".send-btn");

    if (hasSelectedUser) {
        noUserSelectedEl.classList.add("hidden");
        messageAreaEl.classList.remove("hidden");
        messageFormEl.classList.remove("hidden");
        messageInput.disabled = false;
        sendBtn.disabled = false;
    } else {
        noUserSelectedEl.classList.remove("hidden");
        messageAreaEl.classList.add("hidden");
        messageFormEl.classList.add("hidden");
        messageInput.disabled = true;
        sendBtn.disabled = true;
    }
}

function createAttachmentPreview() {
    const messageForm = document.querySelector('.message-form');
    const oldPreview = document.querySelector('.attachment-preview');

    if (oldPreview) {
        oldPreview.remove();
    }

    const attachmentPreview = document.createElement('div');
    attachmentPreview.classList.add('attachment-preview');

    const clearButton = document.createElement('button');
    clearButton.classList.add('clear-attachments');
    clearButton.innerHTML = '<i class="bx bx-x"></i>';
    clearButton.addEventListener('click', () => {
        connectionManager.clearAttachments();
    });

    attachmentPreview.appendChild(clearButton);
    messageForm.insertBefore(attachmentPreview, document.getElementById('message'));

    return attachmentPreview;
}