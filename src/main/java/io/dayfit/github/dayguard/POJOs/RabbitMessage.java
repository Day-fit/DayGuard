package io.dayfit.github.dayguard.POJOs;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RabbitMessage extends Message {
    private String message;
    private String sender;
    private String receiver;
}
