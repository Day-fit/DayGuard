package pl.dayfit.dayguard.Configurations.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cookies")
public class CookiePropertiesConfiguration {
    private @Getter @Setter String sameSitePolicy;
    private @Getter @Setter boolean usingSecuredCookies;
}
