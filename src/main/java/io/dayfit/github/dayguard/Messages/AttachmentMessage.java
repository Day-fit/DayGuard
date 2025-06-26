package io.dayfit.github.dayguard.Messages;

import io.dayfit.github.dayguard.POJOs.Messages.Attachment;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
public class AttachmentMessage extends CommunicationAbstractMessage{
    private List<Attachment> attachments;
}
