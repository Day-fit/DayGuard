package pl.dayfit.dayguard.Messages;

import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.DTOs.ActivityMessageDTO;
import pl.dayfit.dayguard.POJOs.Messages.ActivityType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ActivityMessage implements Sendable {
    protected ActivityType type;
    protected String targetUsername;
    protected Instant timestamp;
    protected UUID messageUuid;

    protected ActivityMessageService sendingService;

    /**
     * Method that handles the sending logic
     */
    @Override
    public void send() {
        sendingService.handleActivityMessageSending(
                ActivityMessageDTO.builder()
                        .targetUsername(targetUsername)
                        .uuid(messageUuid)
                        .timestamp(timestamp)
                        .type(type)
                        .build()
        );
    }
}
