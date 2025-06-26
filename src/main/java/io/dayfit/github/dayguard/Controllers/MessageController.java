package io.dayfit.github.dayguard.Controllers;

import io.dayfit.github.dayguard.DTOs.AttachmentMessageDTO;
import io.dayfit.github.dayguard.DTOs.MessageDTO;
import io.dayfit.github.dayguard.DTOs.TextMessageDTO;
import io.dayfit.github.dayguard.Messages.AbstractMessage;
import io.dayfit.github.dayguard.Messages.AttachmentMessage;
import io.dayfit.github.dayguard.Messages.TextMessage;
import io.dayfit.github.dayguard.POJOs.Messages.Attachment;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class MessageController {

    @MessageMapping("/publish")
    public ResponseEntity<Map<String, String>> publishMessage(@RequestBody MessageDTO messageDto, Message<?> rawMessage)
    {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(rawMessage);

        String username = Objects.requireNonNull(headerAccessor.getUser(), "Sender user is null")
                .getName();

        AbstractMessage message = null;

        if (messageDto instanceof TextMessageDTO textMessageDTO)
        {
             message = TextMessage
                    .builder()
                    .message(textMessageDTO.getMessage())
                    .messageUuid(UUID.randomUUID())
                    .sender(username)
                    .receiver(textMessageDTO.getReceiver())
                    .build();

            message.send();
            return ResponseEntity.ok(Map.of("message", "message sent successfully"));
        }

        else if (messageDto instanceof AttachmentMessageDTO attachmentMessageDTO)
        {
            message = AttachmentMessage
                    .builder()
                    .attachments(
                            attachmentMessageDTO.getAttachments().stream().map(file ->
                                    {
                                        Attachment attachment;

                                        try {
                                            attachment = new Attachment(
                                                    file.getName(),
                                                    file.getContentType(),
                                                    Base64.getEncoder().encodeToString(file.getBytes()),
                                                    file.getSize()
                                            );
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                        return attachment;
                                    }
                            ).toList()
                    )
                    .messageUuid(UUID.randomUUID())
                    .sender(username)
                    .receiver(attachmentMessageDTO.getReceiver())
                    .build();
        }

        if (message == null)
        {
            throw new IllegalArgumentException("Message is incorrect");
        }

        message.send();

        return ResponseEntity.ok(Map.of("message", "message sent successfully"));
    }
}
