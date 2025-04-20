package io.dayfit.github.dayguard.EventListeners;

import io.dayfit.github.dayguard.Services.MQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketEventListener {

    private final MQService mqService;
    private final DynamicMessageListenerManager listenerManager;

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event)
    {
        String username = getUsername(event);

        if (username != null)
        {
            log.info("User connected with username of {}", username);
            mqService.addUser(username);
            listenerManager.registerListeners(username, username + ".queuePM", username + ".queue.activity");
            return;
        }

        log.warn("User tried to connect with empty username");
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event)
    {
        String username = getUsername(event);

        if (username != null)
        {
            log.info("User {} has disconnected", username);
            mqService.removeUser(username);
            listenerManager.removeMessageListener(username);
        }
    }

    private String getUsername(AbstractSubProtocolEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() != null)
        {
            return accessor.getUser().getName();
        }

        log.debug("Username was null at sessionId: {}", accessor.getSessionId());
        return null;
    }
}
