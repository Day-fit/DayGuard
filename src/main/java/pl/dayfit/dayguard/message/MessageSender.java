package pl.dayfit.dayguard.message;

import pl.dayfit.dayguard.dto.MessageResponseDTO;

public interface MessageSender {
    void publishMessage(MessageResponseDTO message, String receiver);
    void publishMessageFanout(MessageResponseDTO message);
}
