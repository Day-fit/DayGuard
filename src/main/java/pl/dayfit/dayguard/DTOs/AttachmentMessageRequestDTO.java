package pl.dayfit.dayguard.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.POJOs.Messages.Attachment;

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
