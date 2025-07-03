package pl.dayfit.dayguard.DTOs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TextMessageDTO extends MessageDTO {
    private final String message;
}
