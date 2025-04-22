package io.dayfit.github.dayguard.Services;

import io.dayfit.github.dayguard.Events.ActivityEvent;
import io.dayfit.github.dayguard.POJOs.Messages.ActivityMessage;
import io.dayfit.github.dayguard.POJOs.Messages.MessageType;
import io.dayfit.github.dayguard.POJOs.MQ.UserMQ;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQService {
    private final @Getter ConcurrentHashMap<String,UserMQ> usersMQ = new ConcurrentHashMap<>();
    private final RabbitAdmin rabbitAdmin;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    private final @Getter TopicExchange usersActivityExchange = new TopicExchange("users.activity");
    private final @Getter String ROUTING_KEY = "users.#";

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
        String routingKeyActivity = "users."+username+".activity";
        Binding bindingActivity = BindingBuilder
                .bind(queueActivity)
                .to(usersActivityExchange)
                .with(routingKeyActivity);

        rabbitAdmin.declareQueue(queuePM);
        rabbitAdmin.declareExchange(exchangePM);
        rabbitAdmin.declareBinding(bindingPM);

        rabbitAdmin.declareQueue(queueActivity);
        rabbitAdmin.declareBinding(bindingActivity);

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
                        .routingKeyActivity(routingKeyActivity)
                        .bindingPM(bindingPM)
                        .build()
        );
    }

    public void sendActivateUsersList(String receiverUsername)
    {
        log.info("Sending activate users list to users activity exchange");

        messagingTemplate.convertAndSend("/user/"+receiverUsername+"/queue/activities",
                ActivityMessage.builder()
                        .targetUsernames(usersMQ.keySet().stream().filter(u -> !u.equals(receiverUsername)).collect(Collectors.toList()))
                        .messageId(UUID.randomUUID().toString())
                        .date(new Date())
                        .type(MessageType.ACTIVE_USERS_LIST)
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
