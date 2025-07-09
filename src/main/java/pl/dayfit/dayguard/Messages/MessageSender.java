package pl.dayfit.dayguard.Messages;

import pl.dayfit.dayguard.DTOs.MessageResponseDTO;

public interface MessageSender {
    void publishMessage(MessageResponseDTO message, String receiver);
    void publishMessageFanout(MessageResponseDTO message);
}
