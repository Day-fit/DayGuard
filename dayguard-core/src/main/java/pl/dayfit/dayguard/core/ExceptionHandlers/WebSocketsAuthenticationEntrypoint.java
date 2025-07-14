package pl.dayfit.dayguard.core.ExceptionHandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class WebSocketsAuthenticationEntrypoint extends StompSubProtocolErrorHandler {
    @Override
    public Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, @Nullable Throwable ex) {
        Throwable cause = getRootCause(ex);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        String errorMessage = "";

        if (ex == null)
        {
            return MessageBuilder
                    .createMessage("Unknown error".getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
        }

        if (cause instanceof AuthenticationException)
        {
            accessor.setNativeHeader("error-code", "401");

            errorMessage = "Authentication failed. "+ ex.getMessage();
            accessor.setMessage(errorMessage);
        }

        return MessageBuilder.createMessage(
                errorMessage.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
