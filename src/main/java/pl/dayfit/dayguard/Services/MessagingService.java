package pl.dayfit.dayguard.Services;

import lombok.extern.slf4j.Slf4j;
import pl.dayfit.dayguard.DTOs.ActivityMessageDTO;
import pl.dayfit.dayguard.Messages.AbstractMessage;
import pl.dayfit.dayguard.Messages.DedicatedActivityMessage;
import pl.dayfit.dayguard.POJOs.MQ.UserMQ;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.POJOs.Messages.ActivityType;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {
    private final MQService mqService;
    private final RabbitTemplate rabbitTemplate;

    public void publishMessage(AbstractMessage message) throws IllegalArgumentException
    {
        if (message.getReceiver() != null)
        {

            UserMQ receiver = mqService.getUser(message.getReceiver());

            if (receiver != null)
            {
                rabbitTemplate.convertAndSend(
                        receiver.getExchangePM().getName(),
                        receiver.getRoutingKeyPM(),
                        message
                );

                return;
            }
        }

        throw new IllegalArgumentException("Receiver is null!");
    }

    public void sendActivateUsersList(String receiverUsername)
    {
        log.debug("Sending activate users list to users activity exchange");

        mqService.getUsersMQ().keySet().forEach(user ->
                DedicatedActivityMessage.builder()
                        .receiver(receiverUsername)
                        .messageUuid(UUID.randomUUID())
                        .timestamp(Instant.now())
                        .type(ActivityType.IS_CONNECTED)
                        .sendingService(this::sendActivityMessage)
                        .build()
        );
    }

    private void sendActivityMessage(ActivityMessageDTO dto)
    {
    }
}