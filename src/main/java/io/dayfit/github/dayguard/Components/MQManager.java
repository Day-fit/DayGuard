package io.dayfit.github.dayguard.Components;

import io.dayfit.github.dayguard.Events.ActivityEvent;
import io.dayfit.github.dayguard.POJOs.ActivityMessage;
import io.dayfit.github.dayguard.POJOs.MessageType;
import io.dayfit.github.dayguard.POJOs.UserMQ;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQManager {
    private final ConcurrentHashMap<String,UserMQ> usersMQ = new ConcurrentHashMap<>();
    private final RabbitAdmin rabbitAdmin;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    private final @Getter TopicExchange usersActivityExchange = new TopicExchange("users.activity");
    private final @Getter String ROUTING_KEY = "users.*";

    @PostConstruct
    public void init()
    {
        try{
            rabbitAdmin.declareExchange(usersActivityExchange);
        } catch (Exception e)
        {
            log.error(e.getMessage());
        }
    }

    public void addUser(String username)
    {
        if(usersMQ.containsKey(username))
        {
            log.warn("User already exists with name {}",username);
            return;
        }

        TopicExchange exchangePM = new TopicExchange(username+".exchangePM");
        Queue queuePM = new Queue(username+".queuePM");
        String routingKeyPM = username+".routing.key";
        Binding bindingPM = BindingBuilder
                .bind(queuePM)
                .to(exchangePM)
                .with(routingKeyPM);

        Queue queueActivity = new Queue(username+".queue.activity");
        String routingKeyActivity = username+".routing.key.activity";

        rabbitAdmin.declareQueue(queuePM);
        rabbitAdmin.declareExchange(exchangePM);
        rabbitAdmin.declareBinding(bindingPM);

        rabbitAdmin.declareQueue(queueActivity);
        rabbitAdmin.declareBinding(BindingBuilder
                .bind(queueActivity)
                .to(usersActivityExchange)
                .with(routingKeyActivity));

        for(String activeUser : usersMQ.keySet())
        {
            messagingTemplate.convertAndSend("/user/"+username+"/queue/activities",
                    ActivityMessage.builder()
                            .targetUsername(activeUser)
                            .messageId(UUID.randomUUID().toString())
                            .date(new Date())
                            .type(MessageType.JOIN)
                            .build()
            );
        }

        eventPublisher.publishEvent(
                new ActivityEvent(
                        ActivityMessage.builder()
                                .targetUsername(username)
                                .messageId(UUID.randomUUID().toString())
                                .date(new Date())
                                .type(MessageType.JOIN)
                                .build()
                )
        );

        usersMQ.put(username,
                UserMQ.builder()
                        .username(username)
                        .exchangePM(exchangePM)
                        .queuePM(queuePM)
                        .queueActivity(queueActivity)
                        .routingKeyPM(routingKeyPM)
                        .bindingPM(bindingPM)
                        .build()
        );
    }

    public void removeUser(String username)
    {
        if(usersMQ.containsKey(username))
        {
            UserMQ userToRemove = usersMQ.remove(username);

            rabbitAdmin.deleteQueue(userToRemove.getQueuePM().getName());
            rabbitAdmin.deleteExchange(userToRemove.getExchangePM().getName());

            rabbitAdmin.deleteQueue(userToRemove.getQueueActivity().getName());

            eventPublisher.publishEvent(
                    new ActivityEvent(
                            ActivityMessage.builder()
                                    .targetUsername(username)
                                    .messageId(UUID.randomUUID().toString())
                                    .date(new Date())
                                    .type(MessageType.LEAVE)
                                    .build()
                    )
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
