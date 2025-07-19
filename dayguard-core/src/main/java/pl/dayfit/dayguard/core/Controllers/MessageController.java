package pl.dayfit.dayguard.core.Controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.springframework.context.ApplicationEventPublisher;
import pl.dayfit.dayguard.core.DTOs.AttachmentMessageRequestDTO;
import pl.dayfit.dayguard.core.DTOs.TextMessageRequestDTO;
import pl.dayfit.dayguard.core.Events.UserReadyForMessagesEvent;
import pl.dayfit.dayguard.core.Messages.AbstractMessage;
import pl.dayfit.dayguard.core.Messages.AttachmentMessage;
import pl.dayfit.dayguard.core.Messages.TextMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
public class MessageController {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PolicyFactory policyFactory;

    @MessageMapping("/connection-ready")
    public ResponseEntity<Map<String, String>> handlePing(@NotNull Message<?> message)
    {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        Principal principal = accessor.getUser();

        if (principal == null)
        {
            throw new IllegalArgumentException("User is not logged in");
        }

        applicationEventPublisher.publishEvent(new UserReadyForMessagesEvent(principal.getName()));
        return ResponseEntity.ok(Map.of("message", "ack"));
    }

    @MessageMapping("/publish/text")
    public ResponseEntity<Map<String, String>> publishMessage(@Valid TextMessageRequestDTO textMessageDto, Message<?> rawMessage)
    {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(rawMessage);

        String username = Objects.requireNonNull(headerAccessor.getUser(), "Sender user is null")
                .getName();

        AbstractMessage message = TextMessage
                    .builder()
                    .message(policyFactory.sanitize(textMessageDto.getMessage()))
                    .messageUuid(UUID.randomUUID())
                    .sender(username)
                    .receiver(textMessageDto.getReceiver())
                    .build();

        message.send();
        return ResponseEntity.ok(Map.of("message", "message sent successfully"));
    }

    @MessageMapping("/publish/attachment")
    public ResponseEntity<Map<String, String>> publishMessage(@Valid AttachmentMessageRequestDTO attachmentMessageDto, Message<?> rawMessage)
    {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(rawMessage);

        String username = Objects.requireNonNull(headerAccessor.getUser(), "Sender user is null")
                .getName();

        AttachmentMessage message = AttachmentMessage
                .builder()
                .attachments(
                        attachmentMessageDto.getAttachments()
                )
                .messageUuid(UUID.randomUUID())
                .sender(username)
                .receiver(attachmentMessageDto.getReceiver())
                .build();

        message.send();
        return ResponseEntity.ok(Map.of("message", "message sent successfully"));
    }
}
