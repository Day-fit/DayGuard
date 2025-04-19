package io.dayfit.github.dayguard.Controllers;

import io.dayfit.github.dayguard.POJOs.MessageType;
import io.dayfit.github.dayguard.POJOs.RabbitMessage;
import io.dayfit.github.dayguard.Services.MessagingService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class MessageController {

    private final MessagingService messagingService;

    @MessageMapping("/publish")
    public String publishMessage(@RequestBody RabbitMessage message, Message<?> rawMessage)
    {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(rawMessage);

        String username = Objects.requireNonNull(headerAccessor.getUser(), "Sender user is null")
                .getName();

        message.setMessageId(UUID.randomUUID().toString());
        message.setSender(username);
        message.setDate(new Date());
        message.setType(MessageType.MESSAGE);

        try {
            messagingService.publishMessage(message);
        } catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }

        return "Message published";
    }
}
