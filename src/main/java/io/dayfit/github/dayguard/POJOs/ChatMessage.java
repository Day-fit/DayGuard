package io.dayfit.github.dayguard.POJOs;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String sender;
    private String recipient;
    private String message;

    private MessageType type;
}
