package io.dayfit.github.dayguard.POJOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RabbitMessage {
    private String messageId;
    private String message;

    private String sender;
    private String receiver;

    private Date date;

    private MessageType type;
}
