package pl.dayfit.dayguard.core.Messages;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
public abstract class AbstractMessage implements Sendable {
    protected UUID messageUuid;
    protected Instant timestamp;
    public static MessageSender messageSender;
}