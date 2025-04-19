package io.dayfit.github.dayguard.EventListeners;

import io.dayfit.github.dayguard.Components.MQManager;
import io.dayfit.github.dayguard.Events.ActivityEvent;
import io.dayfit.github.dayguard.POJOs.ActivityMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivitiesEventListener {
    private final RabbitTemplate rabbitTemplate;
    private final MQManager mqManager;

    @EventListener
    public void handleActivityEvent(ActivityEvent event) {
        ActivityMessage message = event.activityMessage();
        rabbitTemplate.convertAndSend(
                mqManager.getUsersActivityExchange().getName(),
                mqManager.getROUTING_KEY(),
                message
        );
    }
}
