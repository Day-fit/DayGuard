package io.dayfit.github.dayguard.Interceptors;

import io.dayfit.github.dayguard.POJOs.MQ.StompPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    @Override
    @SuppressWarnings("NullableProblems")
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand()))
        {
            String username = accessor.getFirstNativeHeader("username");
            log.debug("username is {} (IN HEADER)", username);

            if(username != null && !username.isEmpty())
            {
                accessor.setUser(new StompPrincipal(username));
            }

            else
            {
                throw new RuntimeException("Username can not be empty");
            }
        }

        return message;
    }
}
