package com.mx.mitienda.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorSecurityConfig {

    @Bean
    @ConditionalOnProperty(name = "security.actuator-api-key")
    public ActuatorApiKeyFilter actuatorApiKeyFilter(
            @Value("${security.actuator-api-key}") String apiKey) {
        return new ActuatorApiKeyFilter(apiKey);
    }
}
