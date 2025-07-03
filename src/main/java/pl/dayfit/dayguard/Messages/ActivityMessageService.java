package pl.dayfit.dayguard.Messages;

import pl.dayfit.dayguard.DTOs.ActivityMessageDTO;

@FunctionalInterface
public interface ActivityMessageService {
    void handleActivityMessageSending(ActivityMessageDTO dto);
}