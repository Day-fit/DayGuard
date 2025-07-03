package pl.dayfit.dayguard.ExceptionHandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class WebSocketsExceptionHandler extends StompSubProtocolErrorHandler {
    @Override
    public Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, Throwable ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        accessor.setNativeHeader("error-code", "401");

        String errorMessage = "Authentication failed. "+ ex.getMessage();

        accessor.setMessage(errorMessage);
        return MessageBuilder.createMessage(
                errorMessage.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }
}
