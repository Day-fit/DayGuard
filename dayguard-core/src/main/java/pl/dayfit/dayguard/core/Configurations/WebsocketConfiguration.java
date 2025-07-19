package pl.dayfit.dayguard.core.Configurations;

import org.springframework.messaging.simp.config.ChannelRegistration;
import pl.dayfit.dayguard.core.Configurations.Properties.SecurityPropertiesConfiguration;
import pl.dayfit.dayguard.core.Interceptors.AuthorizationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {
    private final SecurityPropertiesConfiguration configuration;
    private final AuthorizationInterceptor authInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(configuration.getAllowedOriginsPatterns().toArray(new String[0]))
                .setAllowedOriginPatterns(configuration.getAllowedOriginsPatterns().toArray(new String[0]))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
