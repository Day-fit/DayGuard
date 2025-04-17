package io.dayfit.github.dayguard.EventListeners;

import io.dayfit.github.dayguard.POJOs.RabbitMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class DynamicMessageListenerManager {

    private final ConnectionFactory connectionFactory;
    private final SimpMessagingTemplate template;
    private final HashMap<String, SimpleMessageListenerContainer> listeners = new HashMap<>();

    public void registerMessageListener(String username, String queueName)
    {
        if(listeners.containsKey(username)){return;}

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(new Object()
        {
            @SuppressWarnings("unused")
            public void sendMessage(RabbitMessage message)
            {
                template.convertAndSend("/user/"+username+"/queue/messages", message);
            }
        }, "sendMessage");

        messageListenerAdapter.setMessageConverter(new Jackson2JsonMessageConverter());

        container.addQueueNames(queueName);
        container.setMessageListener(messageListenerAdapter);
        container.start();
        listeners.put(username, container);
    }

    public void removeMessageListener(String username)
    {
        SimpleMessageListenerContainer container = listeners.remove(username);
        container.stop();
    }
}
