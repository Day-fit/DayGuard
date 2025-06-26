package io.dayfit.github.dayguard.Services;

import io.dayfit.github.dayguard.Messages.AbstractMessage;
import io.dayfit.github.dayguard.POJOs.MQ.UserMQ;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

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
}