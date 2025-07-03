package pl.dayfit.dayguard.EventListeners;

import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pl.dayfit.dayguard.Messages.AbstractMessage;
import pl.dayfit.dayguard.Messages.ActivityMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class DynamicMessageListenerManager {

    private final ConnectionFactory connectionFactory;
    private final SimpMessagingTemplate template;
    private final ConcurrentHashMap<String, ArrayList<SimpleMessageListenerContainer>> listeners = new ConcurrentHashMap<>();

    @EventListener
    public void registerListeners(SessionConnectEvent event)
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

        String messageQueueName = username + "queue.pm";
        String activityQueueName = username + "queue.activity";

        ArrayList<SimpleMessageListenerContainer> containers = new ArrayList<>();

        if(listeners.containsKey(username)){return;}

        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        SimpleMessageListenerContainer activityListenerContainer = new SimpleMessageListenerContainer(connectionFactory);

        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new Object()
        {
            @SuppressWarnings("unused")
            public void sendMessage(AbstractMessage message)
            {
                template.convertAndSend("/user/"+username+"/queue/messages", message);
            }
        }, "sendMessage");

        MessageListenerAdapter activitiesListenerAdapter = new MessageListenerAdapter(new Object()
        {
            @SuppressWarnings("unused")
            public void sendActivity(ActivityMessage message)
            {
                template.convertAndSend("/user/"+username+"/queue/activities", message);
            }
        }, "sendActivity");

        messageListenerAdapter.setMessageConverter(new Jackson2JsonMessageConverter());
        activitiesListenerAdapter.setMessageConverter(new Jackson2JsonMessageConverter());

        messageListenerContainer.addQueueNames(messageQueueName);
        messageListenerContainer.setMessageListener(messageListenerAdapter);
        messageListenerContainer.start();

        activityListenerContainer.addQueueNames(activityQueueName);
        activityListenerContainer.setMessageListener(activitiesListenerAdapter);
        activityListenerContainer.start();

        containers.add(messageListenerContainer);
        containers.add(activityListenerContainer);

        listeners.put(username, containers);
    }

    @EventListener
    public void removeMessageListener(SessionDisconnectEvent event)
    {
        if (event.getUser() == null)
        {
            return;
        }

        String username = event.getUser().getName();

        List<SimpleMessageListenerContainer> containers = listeners.remove(username);

        if (containers.isEmpty())
        {
            return;
        }

        containers.forEach(SimpleMessageListenerContainer::stop);
    }
}
