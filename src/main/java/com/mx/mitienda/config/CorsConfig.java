package com.mx.mitienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 👉 Orígenes permitidos (añade tu dominio/EC2 aquí)
        corsConfiguration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:5173",
                "http://ec2-3-141-173-188.us-east-2.compute.amazonaws.com:5173" // tu frontend en AWS
        ));

        corsConfiguration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
