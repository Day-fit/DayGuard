package io.dayfit.github.dayguard.Messages;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class TextMessage extends CommunicationAbstractMessage {
    private String message;
}
