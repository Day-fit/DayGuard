package pl.dayfit.dayguard.message;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.dto.TextMessageResponseDTO;

import java.time.Instant;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class TextMessage extends CommunicationAbstractMessage {
    private String ciphertext;
    private String ephemeralPub;

    /**
     * Method that handles the sending logic
     */
    @Override
    public void send() {
        messageSender.publishMessage(
                TextMessageResponseDTO.builder()
                        .sender(sender)
                        .message(ciphertext)
                        .ephemeralPub(ephemeralPub)
                        .uuid(UUID.randomUUID())
                        .timestamp(Instant.now())
                        .build(),
                receiver
        );
    }
}
