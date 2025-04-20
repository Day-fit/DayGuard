package io.dayfit.github.dayguard.Controllers;

import io.dayfit.github.dayguard.Services.MQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Controller
public class SubscriptionController {
    private final MQService mqService;

    /**
     * This method is called when the client sends a message to the "/connection-ready" endpoint.
     * It sends the activate users list to the specified user.
     *
     * @param accessor the SimpMessageHeaderAccessor object that contains the message headers
     */
    @MessageMapping("/connection-ready")
    public void handleActivitiesWhenReady(SimpMessageHeaderAccessor accessor)
    {
        String username = Objects.requireNonNull(accessor.getUser()).getName();
        mqService.sendActivateUsersList(username);
        log.info("Sent activate users list to {}", username);
    }
}
