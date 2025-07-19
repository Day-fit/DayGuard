package pl.dayfit.dayguard.core.Configurations;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.dayfit.dayguard.core.DTOs.MessageRequestDTO;
import pl.dayfit.dayguard.core.Deserializers.MessageRequestDTODeserializer;

@Configuration
public class JacksonConfiguration {

    @Bean
    public SimpleModule messageRequestDTOModule()
    {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(MessageRequestDTO.class, new MessageRequestDTODeserializer());
        return module;
    }
}
