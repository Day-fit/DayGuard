package pl.dayfit.dayguard.Services;

import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pl.dayfit.dayguard.Messages.ActivityMessage;
import pl.dayfit.dayguard.Messages.DedicatedActivityMessage;
import pl.dayfit.dayguard.POJOs.Messages.ActivityType;
import pl.dayfit.dayguard.POJOs.MQ.UserMQ;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQService {
    private final @Getter ConcurrentHashMap<String,UserMQ> usersMQ = new ConcurrentHashMap<>();
    private final RabbitAdmin rabbitAdmin;
    private final @Getter TopicExchange usersActivityExchange = new TopicExchange("users.activity");

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

    @EventListener
    public void addUser(SessionConnectEvent event)
    {
        if (event.getUser() == null)
        {
            return;
        }

        String username = event.getUser().getName();

        if (username == null)
        {
            return;
        }

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

        ActivityMessage.builder()
                .sendingService(activityMessageService)
                .messageUuid(UUID.randomUUID())
                .targetUsername(username)
                .type(ActivityType.JOIN)
                .timestamp(Instant.now())
                .build()
                .send();

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
        log.debug("Sending activate users list to users activity exchange");

        usersMQ.keySet().forEach(user ->
            DedicatedActivityMessage.builder()
                    .receiver(receiverUsername)
                    .messageUuid(UUID.randomUUID())
                    .timestamp(Instant.now())
                    .type(ActivityType.IS_CONNECTED)
                    .sendingService(activityMessageService)
                    .build()
        );
    }

    @EventListener
    public void removeUser(SessionDisconnectEvent event)
    {
        if (event.getUser() == null)
        {
            return;
        }

        String username = event.getUser().getName();

        if(!usersMQ.containsKey(username))
        {
            log.debug("Failed to find user with name {}, skipping removal", username);
            return;
        }

        UserMQ userToRemove = usersMQ.remove(username);

        rabbitAdmin.deleteQueue(userToRemove.getQueuePM().getName());
        rabbitAdmin.deleteExchange(userToRemove.getExchangePM().getName());

        rabbitAdmin.deleteQueue(userToRemove.getQueueActivity().getName());

        ActivityMessage.builder()
                .messageUuid(UUID.randomUUID())
                .sendingService(activityMessageService)
                .targetUsername(username).timestamp(Instant.now())
                .type(ActivityType.JOIN)
                .build()
                .send();
    }

    public UserMQ getUser(String username)
    {
        return usersMQ.get(username);
    }
}
