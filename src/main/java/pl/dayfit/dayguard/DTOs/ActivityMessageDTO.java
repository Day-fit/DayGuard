package pl.dayfit.dayguard.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.POJOs.Messages.ActivityType;

import java.time.Instant;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMessageDTO extends MessageResponseDTO {
    private String targetUsername;
    private Instant timestamp;
    private ActivityType type;
}
