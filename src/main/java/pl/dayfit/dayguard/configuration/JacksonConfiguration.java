package pl.dayfit.dayguard.configuration;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.dayfit.dayguard.dto.MessageRequestDTO;
import pl.dayfit.dayguard.deserializer.MessageRequestDTODeserializer;

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
