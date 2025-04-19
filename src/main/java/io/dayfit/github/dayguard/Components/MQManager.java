package io.dayfit.github.dayguard.Components;

import io.dayfit.github.dayguard.POJOs.ActivityMessage;
import io.dayfit.github.dayguard.POJOs.MessageType;
import io.dayfit.github.dayguard.POJOs.UserMQ;
import io.dayfit.github.dayguard.Services.MessageService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQManager {
    private final HashMap<String,UserMQ> usersMQ = new HashMap<>();
    private final RabbitAdmin rabbitAdmin;
    private final MessageService messageService;

    private final @Getter TopicExchange usersActivityExchange = new TopicExchange("users.activity");
    private final @Getter String ROUTING_KEY = "users.*";

    @PostConstruct
    public void init()
    {
        rabbitAdmin.declareExchange(usersActivityExchange);
    }

    public void addUser(String username)
    {
        TopicExchange exchangePM = new TopicExchange(username+".exchangePM");
        Queue queuePM = new Queue(username+".queuePM");
        String routingKey = username+".routing.key";

        Queue queueActivity = new Queue(username+".queue.activity");
        String routingKeyActivity = username+".routing.key.activity";

        rabbitAdmin.declareQueue(queuePM);
        rabbitAdmin.declareExchange(exchangePM);
        rabbitAdmin.declareBinding(BindingBuilder
                .bind(queuePM)
                .to(exchangePM)
                .with(routingKey));

        rabbitAdmin.declareQueue(queueActivity);
        rabbitAdmin.declareBinding(BindingBuilder
                .bind(queueActivity)
                .to(usersActivityExchange)
                .with(routingKeyActivity));

        usersMQ.put(username,
                UserMQ.builder()
                        .username(username)
                        .exchangePM(exchangePM)
                        .queuePM(queuePM)
                        .routingKeyPM(routingKey)
                        .bindingPM(
                                BindingBuilder
                                        .bind(queuePM)
                                        .to(exchangePM)
                                        .with(routingKey)
                        )
                        .build()
        );



        messageService.publishActivity(
                ActivityMessage.builder()
                        .targetUsername(username)
                        .messageId(UUID.randomUUID().toString())
                        .date(new Date())
                        .type(MessageType.JOIN)
                        .build());
    }

    public void removeUser(String username)
    {
        if(usersMQ.containsKey(username))
        {
            rabbitAdmin.deleteQueue(username+".queue");
            rabbitAdmin.deleteExchange(username+".exchange");

            usersMQ.remove(username);

            messageService.publishActivity(
                    ActivityMessage.builder()
                            .targetUsername(username)
                            .messageId(UUID.randomUUID().toString())
                            .date(new Date())
                            .type(MessageType.LEAVE)
                            .build()
            );
            return;
        }

        log.warn("Failed to find user with name {}, skipping removal",username);
    }

    public UserMQ getUser(String username)
    {
        return usersMQ.get(username);
    }
}
