package pl.dayfit.dayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.type.ActivityType;

import java.time.Instant;
import java.util.UUID;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMessageDTO extends MessageResponseDTO {
    private UUID id;
    private String targetUsername;
    private Instant timestamp;
    private ActivityType type;
}
