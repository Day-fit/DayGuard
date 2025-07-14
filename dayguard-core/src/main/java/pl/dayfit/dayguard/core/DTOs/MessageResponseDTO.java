package pl.dayfit.dayguard.core.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    protected UUID uuid;
    protected String sender;
    protected Instant timestamp;
}