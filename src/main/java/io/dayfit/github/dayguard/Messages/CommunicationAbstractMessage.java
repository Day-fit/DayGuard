package io.dayfit.github.dayguard.Messages;

import io.dayfit.github.dayguard.Services.MessagingService;
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
