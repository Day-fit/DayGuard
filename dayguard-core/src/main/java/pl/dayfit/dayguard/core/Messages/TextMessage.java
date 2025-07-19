package pl.dayfit.dayguard.core.Messages;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.core.DTOs.TextMessageResponseDTO;

import java.time.Instant;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class TextMessage extends CommunicationAbstractMessage {
    private String message;

    /**
     * Method that handles the sending logic
     */
    @Override
    public void send() {
        messageSender.publishMessage(
                TextMessageResponseDTO.builder()
                        .sender(sender)
                        .message(message)
                        .uuid(UUID.randomUUID())
                        .timestamp(Instant.now())
                        .build(),
                receiver
        );
    }
}
