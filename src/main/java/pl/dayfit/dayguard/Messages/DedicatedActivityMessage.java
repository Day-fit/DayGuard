package pl.dayfit.dayguard.Messages;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.DTOs.ActivityMessageDTO;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class DedicatedActivityMessage extends ActivityMessage {
    private String receiver;

    @Override
    public void send()
    {
        messageSender.publishMessage(
            ActivityMessageDTO.builder()
                    .uuid(UUID.randomUUID())
                    .timestamp(Instant.now())
                    .type(type)
                    .targetUsername(targetUsername)
                    .build(),
            receiver
        );
    }
}
