package com.mx.mitienda.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = "com.mx.mitienda")
@EnableJpaRepositories(basePackages = "com.mx.mitienda.repository")
@EntityScan(basePackages = "com.mx.mitienda.model")
@ComponentScan(basePackages = "com.mx.mitienda")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
