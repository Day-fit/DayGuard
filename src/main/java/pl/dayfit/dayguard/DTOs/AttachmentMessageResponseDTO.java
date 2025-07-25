package pl.dayfit.dayguard.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.POJOs.Messages.Attachment;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentMessageResponseDTO extends MessageResponseDTO {
    private List<Attachment> attachments;
    private String sender;
}
