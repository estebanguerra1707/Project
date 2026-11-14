package com.mx.mitienda.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.mx.mitienda")
@EnableJpaRepositories(basePackages = "com.mx.mitienda.repository")
@EntityScan(basePackages = "com.mx.mitienda.model")
@ComponentScan(basePackages = "com.mx.mitienda")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@ConfigurationPropertiesScan
@EnableAsync
public class DemoApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
