package io.dayfit.github.dayguard.EventListeners;

import io.dayfit.github.dayguard.POJOs.ActivityMessage;
import io.dayfit.github.dayguard.POJOs.RabbitMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class DynamicMessageListenerManager {

    private final ConnectionFactory connectionFactory;
    private final SimpMessagingTemplate template;
    private final HashMap<String, ArrayList<SimpleMessageListenerContainer>> listeners = new HashMap<>();

    public void registerListeners(String username, String messageQueueName, String activityQueueName)
    {
        ArrayList<SimpleMessageListenerContainer> containers = new ArrayList<>();

        if(listeners.containsKey(username)){return;}

        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        SimpleMessageListenerContainer activityListenerContainer = new SimpleMessageListenerContainer(connectionFactory);

        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new Object()
        {
            @SuppressWarnings("unused")
            public void sendMessage(RabbitMessage message)
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

    public void removeMessageListener(String username)
    {
        ArrayList<SimpleMessageListenerContainer> containers = listeners.remove(username);
        containers.forEach(SimpleMessageListenerContainer::stop);
    }
}
