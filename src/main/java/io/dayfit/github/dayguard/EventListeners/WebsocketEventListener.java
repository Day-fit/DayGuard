package io.dayfit.github.dayguard.EventListeners;

import io.dayfit.github.dayguard.POJOs.ChatMessage;
import io.dayfit.github.dayguard.POJOs.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebsocketDisconnect(SessionDisconnectEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = Objects.requireNonNull(accessor.getSessionAttributes()).get("username").toString();

        if (username != null)
        {
            log.info("Disconnected from {}", username);

            ChatMessage chatMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .build();

            messagingTemplate.convertAndSend("/topic/chat", chatMessage);
        }
    }
}
