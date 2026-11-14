package com.mx.mitienda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "delivery")
public record DeliveryProperties(double originLat, double originLon, double maxKm) {}
