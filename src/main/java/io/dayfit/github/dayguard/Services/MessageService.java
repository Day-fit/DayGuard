package io.dayfit.github.dayguard.Services;

import io.dayfit.github.dayguard.Components.MQManager;
import io.dayfit.github.dayguard.POJOs.ActivityMessage;
import io.dayfit.github.dayguard.POJOs.RabbitMessage;
import io.dayfit.github.dayguard.POJOs.UserMQ;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MQManager mqManager;
    private final RabbitTemplate rabbitTemplate;

    public void publishMessage(RabbitMessage message) throws IllegalArgumentException
    {
        if (message.getReceiver() != null)
        {

            UserMQ receiver = mqManager.getUser(message.getReceiver());

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


    public void publishActivity(ActivityMessage message)
    {
        rabbitTemplate.convertAndSend(
                mqManager.getUsersActivityExchange().getName(),
                mqManager.getROUTING_KEY(),
                message
        );
    }
}