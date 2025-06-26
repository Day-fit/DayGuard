package io.dayfit.github.dayguard.Messages;

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
    protected String receiver;
    protected Instant timestamp;
}
