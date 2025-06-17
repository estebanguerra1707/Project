package com.mx.mitienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration
public class EncoderPassword {

    @Bean//agregar un objeto a la lista de spring
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();// <- este objeto vivirá como un singleton en Spring
    }
}
