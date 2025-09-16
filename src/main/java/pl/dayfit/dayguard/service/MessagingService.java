package pl.dayfit.dayguard.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import pl.dayfit.dayguard.dto.ActivityMessageDTO;
import pl.dayfit.dayguard.dto.MessageResponseDTO;
import pl.dayfit.dayguard.message.AbstractMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.message.MessageSender;

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
}