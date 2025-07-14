package pl.dayfit.dayguard.core.Interceptors;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import pl.dayfit.dayguard.core.Auth.JwtAuthenticationProvider;
import pl.dayfit.dayguard.core.Auth.JwtAuthenticationToken;
import pl.dayfit.dayguard.core.Controllers.AuthenticationController;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements ChannelInterceptor {
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !(StompCommand.CONNECT.equals(accessor.getCommand())))
        {
            return message;
        }

        List<String> rawCookies = accessor.getNativeHeader("cookie");

        if (rawCookies == null)
        {
            return message;
        }

        String allCookies = String.join(";", rawCookies);
        Map<String, String> cookies = Stream.of(allCookies.split(";"))
                .map(cookie -> {
                    cookie = cookie.trim();
                    return cookie.split("=", 2);
                }).collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> pair[1]
                ));

        String accessToken = cookies.get(AuthenticationController.ACCESS_TOKEN_NAME);

        JwtAuthenticationToken token = (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(new JwtAuthenticationToken(accessToken));
        accessor.setUser((Principal) token.getPrincipal());

        return message;
    }
}
