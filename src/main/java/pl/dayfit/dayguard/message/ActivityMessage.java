package pl.dayfit.dayguard.message;

import lombok.experimental.SuperBuilder;
import lombok.*;
import pl.dayfit.dayguard.dto.ActivityMessageDTO;
import pl.dayfit.dayguard.type.ActivityType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ActivityMessage extends AbstractMessage {
    protected String targetUsername;
    protected UUID id;
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
                        .id(id)
                        .targetUsername(targetUsername)
                        .build()
        );
    }
}
