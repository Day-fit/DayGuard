package pl.dayfit.dayguard.Services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import pl.dayfit.dayguard.DTOs.ActivityMessageDTO;
import pl.dayfit.dayguard.DTOs.MessageResponseDTO;
import pl.dayfit.dayguard.Events.UserReadyForMessagesEvent;
import pl.dayfit.dayguard.Messages.AbstractMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.Messages.DedicatedActivityMessage;
import pl.dayfit.dayguard.Messages.MessageSender;
import pl.dayfit.dayguard.POJOs.Messages.ActivityType;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService implements MessageSender {
    private final MQService mqService;
    private final RabbitTemplate rabbitTemplate;

    @PostConstruct
    private void init()
    {
        AbstractMessage.messageSender = this;
    }

    @Override
    public void publishMessage(MessageResponseDTO message, String receiver) throws IllegalArgumentException
    {
        if (receiver == null)
        {
            throw new IllegalArgumentException("Receiver cannot be null");
        }

        String routingKey = message instanceof ActivityMessageDTO ? MQService.ACTIVITY_PREFIX + receiver : MQService.MESSAGING_PM_PREFIX + receiver;

        rabbitTemplate.convertAndSend(
                routingKey,
                message
        );
    }

    @Override
    public void publishMessageFanout(MessageResponseDTO message) {
        rabbitTemplate.convertAndSend(
                mqService.getBroadcastExchange().getName(),
                "", //fanout exchange, routing key is ignored
                message
        );
    }

    @EventListener
    public void sendActivateUsersList(UserReadyForMessagesEvent event)
    {
        log.debug("Sending activate users list to users activity exchange ...");

        mqService.getActiveUsers().keySet().forEach(user ->
                DedicatedActivityMessage.builder()
                        .receiver(event.username())
                        .targetUsername(user)
                        .messageUuid(UUID.randomUUID())
                        .timestamp(Instant.now())
                        .type(ActivityType.IS_CONNECTED)
                        .build()
                        .send()
        );
    }
}