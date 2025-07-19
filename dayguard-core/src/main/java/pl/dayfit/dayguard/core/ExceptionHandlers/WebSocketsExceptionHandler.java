package pl.dayfit.dayguard.core.ExceptionHandlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.security.sasl.AuthenticationException;
import java.security.Principal;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketsExceptionHandler {
    private final SimpMessagingTemplate template;

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleBadRequestException(MethodArgumentNotValidException ex, Message<?> message)
    {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        Principal user = accessor.getUser();

        if (user == null)
        {
            return;
        }

        BindingResult result =  ex.getBindingResult();

        if (result == null)
        {
            return;
        }

        FieldError fieldError = result.getFieldError();

        if (fieldError == null)
        {
            return;
        }

        String defaultMessage = fieldError.getDefaultMessage();

        String exceptionMessage = "Validation failed";
        if (defaultMessage != null)
        {
            exceptionMessage = defaultMessage;
        }

        template.convertAndSend(
                "/user/" + user.getName() + "/queue/errors",
                Map.of("error", exceptionMessage)
        );
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgumentException(IllegalArgumentException ex, Message<?> message)
    {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        Principal user = accessor.getUser();

        if (user == null)
        {
            return;
        }

        template.convertAndSend(
                "/user/" + user.getName() + "/queue/errors",
                Map.of("error", ex.getMessage())
        );
    }

    @MessageExceptionHandler(AuthenticationException.class)
    public void handleBadRequestException(AuthenticationException ex, Message<?> message)
    {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        Principal user = accessor.getUser();

        if (user == null)
        {
            return;
        }

        template.convertAndSend(
                "/user/" + user.getName() + "/queue/errors",
                Map.of("error", ex.getMessage())
        );
    }

    @MessageExceptionHandler(Exception.class)
    public void handleGenericException(Message<?> message)
    {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        Principal user = accessor.getUser();

        if (user == null)
        {
            return;
        }

        template.convertAndSend(
                "/user/" + user.getName() + "/queue/errors",
                Map.of("error", "Internal server error")
        );
    }
}
