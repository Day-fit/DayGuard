package pl.dayfit.dayguard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.dayfit.dayguard.message.AttachmentMessage;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentMessageResponseDTO extends MessageResponseDTO {
    private List<AttachmentMessage.Attachment> attachments;
    private String sender;
}
