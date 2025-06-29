package pl.dayfit.dayguard.Messages;

import pl.dayfit.dayguard.POJOs.Messages.ActivitiesType;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ActivityMessage extends AbstractMessage {
    private ActivitiesType type;
    private String targetUser;

    /**
     * Method that handles the sending logic
     * //TODO: to be implemented
     */
    @Override
    public void send() {

    }
}
