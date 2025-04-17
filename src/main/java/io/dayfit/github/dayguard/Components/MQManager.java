package io.dayfit.github.dayguard.Components;

import io.dayfit.github.dayguard.POJOs.UserMQ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQManager {
    private final HashSet<UserMQ> usersMQ = new HashSet<>();
    private final RabbitAdmin rabbitAdmin;

    public void addUser(String username)
    {
        TopicExchange exchange = new TopicExchange(username+".exchange");
        Queue queue = new Queue(username+".queue");
        String routingKey = username+".routing.key";

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(routingKey));

        usersMQ.add(
                UserMQ.builder()
                        .username(username)
                        .exchange(exchange)
                        .queue(queue)
                        .routingKey(routingKey)
                        .binding(
                                BindingBuilder
                                        .bind(queue)
                                        .to(exchange)
                                        .with(routingKey)
                        )
                        .build()
        );
    }

    public void removeUser(String username)
    {
        UserMQ userMQ = getUser(username);

        if(userMQ == null)
        {
            log.warn("Failed to remove user {}, user does not exist", username);
            return;
        }

        rabbitAdmin.deleteQueue(username+".queue");
        rabbitAdmin.deleteExchange(username+".exchange");

        usersMQ.remove(userMQ);
    }

    public UserMQ getUser(String username)
    {
        return usersMQ.stream()
                    .filter(userMQ -> userMQ.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
    }
}
