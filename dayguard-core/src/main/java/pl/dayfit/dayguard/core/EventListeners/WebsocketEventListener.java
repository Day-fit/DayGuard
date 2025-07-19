package pl.dayfit.dayguard.core.EventListeners;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pl.dayfit.dayguard.core.Messages.ActivityMessage;
import pl.dayfit.dayguard.core.POJOs.Messages.ActivityType;

import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebsocketEventListener {

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event)
    {
        if (event.getUser() == null)
        {
            return;
        }

        String username = event.getUser().getName();

        if (username == null)
        {
            log.debug("User tried to connect with empty username");
            return;
        }

        ActivityMessage.builder()
                .type(ActivityType.JOIN)
                .targetUsername(username)
                .timestamp(Instant.now())
                .messageUuid(UUID.randomUUID())
                .build()
                .send();

        log.debug("User connecting with username of {}", username);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event)
    {
        if (event.getUser() == null)
        {
            return;
        }

        String username = event.getUser().getName();

        if (username == null)
        {
            log.debug("User disconnected with null username");
        }

        ActivityMessage.builder()
                .targetUsername(username)
                .type(ActivityType.LEAVE)
                .messageUuid(UUID.randomUUID())
                .timestamp(Instant.now())
                .build()
                .send();
    }
}
