package pl.dayfit.dayguard.DTOs;

import lombok.Builder;
import lombok.Getter;
import pl.dayfit.dayguard.POJOs.Messages.ActivityType;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class ActivityMessageDTO {
    private UUID uuid;
    private String targetUsername;
    private Instant timestamp;
    private ActivityType type;
}
