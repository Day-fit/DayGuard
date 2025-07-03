package pl.dayfit.dayguard.Messages;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DedicatedActivityMessage extends ActivityMessage {
    private String receiver;
}
