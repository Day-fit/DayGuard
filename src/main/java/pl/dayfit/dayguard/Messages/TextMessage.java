package pl.dayfit.dayguard.Messages;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class TextMessage extends CommunicationAbstractMessage {
    private String message;
}
