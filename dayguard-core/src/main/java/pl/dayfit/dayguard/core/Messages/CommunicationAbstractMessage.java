package pl.dayfit.dayguard.core.Messages;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class CommunicationAbstractMessage extends AbstractMessage{
    protected String sender;
    protected String receiver;
}
