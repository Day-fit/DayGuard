package pl.dayfit.dayguard.core.Messages;

import pl.dayfit.dayguard.core.DTOs.MessageResponseDTO;

public interface MessageSender {
    void publishMessage(MessageResponseDTO message, String receiver);
    void publishMessageFanout(MessageResponseDTO message);
}
