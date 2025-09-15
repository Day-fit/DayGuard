package pl.dayfit.dayguard.message;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.dto.ActivityMessageDTO;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class DedicatedActivityMessage extends ActivityMessage {
    private String receiver;
    private UUID id;

    @Override
    public void send()
    {
        messageSender.publishMessage(
            ActivityMessageDTO.builder()
                    .uuid(UUID.randomUUID())
                    .id(id)
                    .timestamp(Instant.now())
                    .type(type)
                    .targetUsername(targetUsername)
                    .build(),
            receiver
        );
    }
}
