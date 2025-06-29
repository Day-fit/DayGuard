package pl.dayfit.dayguard.EventListeners;

import pl.dayfit.dayguard.Services.MQService;
import pl.dayfit.dayguard.Events.ActivityEvent;
import pl.dayfit.dayguard.Messages.ActivityMessage;
import pl.dayfit.dayguard.POJOs.MQ.UserMQ;
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
            if(!user.getUsername().equals(message.getTargetUser()))
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
