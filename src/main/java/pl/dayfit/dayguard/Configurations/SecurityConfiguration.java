package pl.dayfit.dayguard.Configurations;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pl.dayfit.dayguard.Configurations.Properties.CookiePropertiesConfiguration;
import pl.dayfit.dayguard.Configurations.Properties.SecurityPropertiesConfiguration;
import pl.dayfit.dayguard.Filters.CookieJwtFilter;
import pl.dayfit.dayguard.Services.Auth.Jwt.UserDetailsService;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final SecurityPropertiesConfiguration securityProperties;
    private final CookiePropertiesConfiguration cookieProperties;

    @PostConstruct
    private void init()
    {
        if (cookieProperties.isUsingSecuredCookies())
        {
            log.warn("Using not secured cookies is supposed to be used only in dev profile");
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CookieJwtFilter cookieJwtFilter) throws Exception
    {
        return http
                .csrf(AbstractHttpConfigurer::disable) //CSRF is not a possible cause of same-site policy, httpOnly cookies, etc.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        request -> {
                            request.requestMatchers(securityProperties.getProtectedPaths().toArray(new String[0]));
                            request.anyRequest().permitAll();
                        }
                )
                .addFilterBefore(cookieJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(securityProperties.getAllowedOrigins());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }
}
