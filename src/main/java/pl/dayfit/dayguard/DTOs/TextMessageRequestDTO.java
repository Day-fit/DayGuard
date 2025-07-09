package pl.dayfit.dayguard.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TextMessageRequestDTO extends MessageRequestDTO {
    @NotBlank(message = "Message cannot be blank")
    private String message;
}
