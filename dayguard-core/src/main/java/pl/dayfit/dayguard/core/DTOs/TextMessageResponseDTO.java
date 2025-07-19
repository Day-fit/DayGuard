package pl.dayfit.dayguard.core.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextMessageResponseDTO extends MessageResponseDTO {
    private String message;
    private String sender;
}
