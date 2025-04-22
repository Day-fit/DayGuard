package io.dayfit.github.dayguard.POJOs.Messages;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RabbitMessage extends Message {
    private String message;
    private String sender;
    private String receiver;

    private List<Attachment> attachments;
}