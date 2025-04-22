package io.dayfit.github.dayguard.EventListeners;

import io.dayfit.github.dayguard.Services.MQService;
import io.dayfit.github.dayguard.Events.ActivityEvent;
import io.dayfit.github.dayguard.POJOs.Messages.ActivityMessage;
import io.dayfit.github.dayguard.POJOs.MQ.UserMQ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivitiesEventListener {
    private final RabbitTemplate rabbitTemplate;
    private final MQService mqService;

    @EventListener
    public void handleActivityEvent(ActivityEvent event) {
        final ActivityMessage message = event.activityMessage();

        for(UserMQ user : mqService.getUsersMQ().values())
        {
            if(!user.getUsername().equals(message.getTargetUsername()))
            {
                rabbitTemplate.convertAndSend(
                        mqService.getUsersActivityExchange().getName(),
                        user.getRoutingKeyActivity(),
                        message
                );

                log.info("Sending activity message to user {}, at routing key of {}", user.getUsername(), user.getRoutingKeyActivity());
            }
        }
    }
}
