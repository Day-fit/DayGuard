package pl.dayfit.dayguard.Messages;

import pl.dayfit.dayguard.POJOs.Messages.Attachment;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
public class AttachmentMessage extends CommunicationAbstractMessage{
    private List<Attachment> attachments;
}
