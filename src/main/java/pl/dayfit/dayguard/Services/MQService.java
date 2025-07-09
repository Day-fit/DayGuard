package pl.dayfit.dayguard.Services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pl.dayfit.dayguard.Entities.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;
import pl.dayfit.dayguard.Events.UserMQCreationEndedEvent;
import pl.dayfit.dayguard.Services.Cache.UserCacheService;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQService {
    private final @Getter ConcurrentHashMap<String, User> activeUsers = new ConcurrentHashMap<>();
    private final RabbitAdmin rabbitAdmin;
    private final @Getter TopicExchange usersActivityExchange = new TopicExchange("users.activity");
    private final @Getter FanoutExchange broadcastExchange = new FanoutExchange("users.broadcast");
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserCacheService userCacheService;

    public static final String MESSAGING_PM_PREFIX = "user.pm.";
    public static final String ACTIVITY_PREFIX = "user.activity.";

    @PostConstruct
    public void init()
    {
        rabbitAdmin.declareExchange(usersActivityExchange);
        rabbitAdmin.declareExchange(broadcastExchange);
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

        if(activeUsers.containsKey(username))
        {
            log.debug("User already exists with name {}",username);
            return;
        }

        User user = userCacheService.findByEmailOrUsername(username);

        String messagingName = MESSAGING_PM_PREFIX + username;
        DirectExchange exchangePM = new DirectExchange(messagingName);
        Queue queuePM = new Queue(messagingName);
        Binding bindingPM = BindingBuilder
                .bind(queuePM)
                .to(exchangePM)
                .with(messagingName);

        Queue queueActivity = new Queue(ACTIVITY_PREFIX + username);
        String routingKeyActivity = ACTIVITY_PREFIX + username;

        Binding bindingActivity = BindingBuilder
                .bind(queueActivity)
                .to(usersActivityExchange)
                .with(routingKeyActivity);

        Binding broadcastBinding = BindingBuilder
                .bind(queueActivity)
                .to(broadcastExchange);

        rabbitAdmin.declareQueue(queuePM);
        rabbitAdmin.declareExchange(exchangePM);
        rabbitAdmin.declareBinding(bindingPM);

        rabbitAdmin.declareQueue(queueActivity);
        rabbitAdmin.declareBinding(bindingActivity);
        rabbitAdmin.declareBinding(broadcastBinding);

        applicationEventPublisher.publishEvent(new UserMQCreationEndedEvent(username));
        activeUsers.put(username, user);
    }

    @EventListener
    public void removeUser(SessionDisconnectEvent event)
    {
        if (event.getUser() == null)
        {
            return;
        }

        String username = event.getUser().getName();

        if(!activeUsers.containsKey(username))
        {
            log.debug("Failed to find user with name {}, skipping removal", username);
            return;
        }

        User user = activeUsers.remove(username);

        rabbitAdmin.deleteQueue(MESSAGING_PM_PREFIX + user.getUsername());
        rabbitAdmin.deleteExchange(MESSAGING_PM_PREFIX + user.getUsername());
        rabbitAdmin.deleteQueue(ACTIVITY_PREFIX + user.getUsername());
    }
}
