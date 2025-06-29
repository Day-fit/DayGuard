package pl.dayfit.dayguard.EventListeners;

import pl.dayfit.dayguard.Services.MQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebsocketEventListener {

    private final MQService mqService;
    private final DynamicMessageListenerManager listenerManager;

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event)
    {
        String username = getUsername(event);

        if (username != null)
        {
            log.debug("User connecting with username of {}", username);
            mqService.addUser(username);
            listenerManager.registerListeners(username, username + ".queuePM", username + ".queue.activity");
            return;
        }

        log.debug("User tried to connect with empty username");
    }

    @MessageMapping("/connection-ready")
    public void handleConnectionReady(SimpMessageHeaderAccessor accessor)
    {
        String username = Objects.requireNonNull(accessor.getUser(), "Username was empty").getName();

        if (!username.trim().isEmpty()) {
            mqService.sendActivateUsersList(username);
            log.debug("User {} is ready for messages", username);

            return;
        }

        log.debug("Received empty username");
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event)
    {
        String username = getUsername(event);

        if (username != null)
        {
            log.debug("User {} has disconnected", username);
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
