package pl.dayfit.dayguard.Controllers;

import lombok.extern.slf4j.Slf4j;
import pl.dayfit.dayguard.DTOs.AttachmentMessageRequestDTO;
import pl.dayfit.dayguard.DTOs.MessageRequestDTO;
import pl.dayfit.dayguard.DTOs.TextMessageRequestDTO;
import pl.dayfit.dayguard.Messages.AbstractMessage;
import pl.dayfit.dayguard.Messages.AttachmentMessage;
import pl.dayfit.dayguard.Messages.TextMessage;
import pl.dayfit.dayguard.POJOs.Messages.Attachment;
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

@Slf4j
@RestController
@AllArgsConstructor
public class MessageController {

    @MessageMapping("/publish")
    public ResponseEntity<Map<String, String>> publishMessage(@RequestBody MessageRequestDTO messageDto, Message<?> rawMessage)
    {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(rawMessage);

        String username = Objects.requireNonNull(headerAccessor.getUser(), "Sender user is null")
                .getName();

        AbstractMessage message = null;

        if (messageDto instanceof TextMessageRequestDTO textMessageDTO)
        {
             message = TextMessage
                    .builder()
                    .message(textMessageDTO.getMessage())
                    .messageUuid(UUID.randomUUID())
                    .sender(username)
                    .receiver(textMessageDTO.getReceiver())
                    .build();
        }

        else if (messageDto instanceof AttachmentMessageRequestDTO attachmentMessageRequestDTO)
        {
            message = AttachmentMessage
                    .builder()
                    .attachments(
                            attachmentMessageRequestDTO.getAttachments().stream().map(file ->
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
                    .receiver(attachmentMessageRequestDTO.getReceiver())
                    .build();
        }

        if (message == null)
        {
            throw new IllegalArgumentException("Message body is incorrect");
        }

        message.send();
        return ResponseEntity.ok(Map.of("message", "message sent successfully"));
    }
}
