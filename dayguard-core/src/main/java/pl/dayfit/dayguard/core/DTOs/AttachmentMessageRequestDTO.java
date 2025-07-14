package pl.dayfit.dayguard.core.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.core.POJOs.Messages.Attachment;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentMessageRequestDTO extends MessageRequestDTO {
    @NotNull(message = "Attachments cannot be null")
    private List<Attachment> attachments;
}
