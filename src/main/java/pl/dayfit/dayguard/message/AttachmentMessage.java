package pl.dayfit.dayguard.message;

import lombok.*;
import pl.dayfit.dayguard.dto.AttachmentMessageResponseDTO;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SuperBuilder
public class AttachmentMessage extends CommunicationAbstractMessage{
    private List<Attachment> attachments;

    /**
     * Method that handles the sending logic
     */
    @Override
    public void send() {
        messageSender.publishMessage(
                AttachmentMessageResponseDTO.builder()
                        .sender(sender)
                        .attachments(attachments)
                        .uuid(UUID.randomUUID())
                        .timestamp(Instant.now())
                        .build(),
                receiver
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attachment {
        private String name;
        private String data;
        private String type;
        private Long size;
    }
}
