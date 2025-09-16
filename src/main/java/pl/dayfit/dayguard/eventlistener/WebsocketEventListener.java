package pl.dayfit.dayguard.eventlistener;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pl.dayfit.dayguard.auth.JwtAuthenticationToken;
import pl.dayfit.dayguard.auth.JwtPrincipal;
import pl.dayfit.dayguard.message.ActivityMessage;
import pl.dayfit.dayguard.type.ActivityType;

import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebsocketEventListener {

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event)
    {
        JwtAuthenticationToken token = ((JwtAuthenticationToken) event.getUser());

        if(token == null)
        {
            return;
        }

        JwtPrincipal principal = (JwtPrincipal) token.getPrincipal();

        if (principal == null)
        {
            return;
        }

        String username = principal.getName();

        if (username == null)
        {
            log.debug("User tried to connect with empty username");
            return;
        }

        ActivityMessage.builder()
                .type(ActivityType.JOIN)
                .id(principal.getId())
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
        JwtAuthenticationToken token = ((JwtAuthenticationToken) event.getUser());

        if(token == null)
        {
            return;
        }

        JwtPrincipal principal = (JwtPrincipal) token.getPrincipal();

        if (principal == null)
        {
            return;
        }

        String username = principal.getName();

        if (username == null)
        {
            log.debug("User disconnected with null username");
        }

        ActivityMessage.builder()
                .targetUsername(username)
                .id(principal.getId())
                .type(ActivityType.LEAVE)
                .messageUuid(UUID.randomUUID())
                .timestamp(Instant.now())
                .build()
                .send();
    }
}
