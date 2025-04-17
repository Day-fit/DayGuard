package io.dayfit.github.dayguard.EventListeners;

import io.dayfit.github.dayguard.Components.MQManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketEventListener {

    private final MQManager mqManager;
    private final DynamicMessageListenerManager listenerManager;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event)
    {
        String username = getUsername(event);

        if (username != null)
        {
            log.info("User connected with username of {}", username);
            mqManager.addUser(username);
            listenerManager.registerMessageListener(username, username + ".queue");
            return;
        }

        log.warn("User tired connect with empty username");
    }

    @EventListener
    public void handleWebsocketDisconnect(SessionDisconnectEvent event)
    {
        String username = getUsername(event);

        if (username != null)
        {
            log.info("User {} has disconnected", username);
            mqManager.removeUser(username);
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
