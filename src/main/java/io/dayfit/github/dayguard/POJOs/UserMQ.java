package io.dayfit.github.dayguard.POJOs;

import lombok.Builder;
import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

@Data
@Builder
public class UserMQ {
    private String username;

    private TopicExchange exchangePM;
    private Queue queuePM;
    private String routingKeyPM;
    private Binding bindingPM;

    private Queue queueActivity;
    private String routingKeyActivity;
}
