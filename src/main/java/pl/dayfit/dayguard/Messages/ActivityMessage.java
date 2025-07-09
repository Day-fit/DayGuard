package pl.dayfit.dayguard.Messages;

import lombok.experimental.SuperBuilder;
import lombok.*;
import pl.dayfit.dayguard.DTOs.ActivityMessageDTO;
import pl.dayfit.dayguard.POJOs.Messages.ActivityType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ActivityMessage extends AbstractMessage {
    protected String targetUsername;
    protected ActivityType type;

    /**
     * Method that handles the sending logic
     */
    @Override
    public void send() {
        messageSender.publishMessageFanout(
                ActivityMessageDTO.builder()
                        .uuid(UUID.randomUUID())
                        .timestamp(Instant.now())
                        .type(type)
                        .targetUsername(targetUsername)
                        .build()
        );
    }
}
