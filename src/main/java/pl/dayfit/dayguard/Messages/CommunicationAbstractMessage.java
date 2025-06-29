package pl.dayfit.dayguard.Messages;

import pl.dayfit.dayguard.Services.MessagingService;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class CommunicationAbstractMessage extends AbstractMessage{
    protected String sender;
    protected MessagingService messagingService;

    @Override
    public void send()
    {
        messagingService.publishMessage(this);
    }
}
