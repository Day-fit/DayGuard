package pl.dayfit.dayguard.Messages;

import pl.dayfit.dayguard.DTOs.AttachmentMessageResponseDTO;
import pl.dayfit.dayguard.POJOs.Messages.Attachment;
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
}
