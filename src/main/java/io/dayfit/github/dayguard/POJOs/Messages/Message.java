package io.dayfit.github.dayguard.POJOs.Messages;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class Message {
    private String messageId;
    private Date date;
    private MessageType type;
}
