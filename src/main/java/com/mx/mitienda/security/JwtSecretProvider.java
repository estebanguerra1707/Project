package com.mx.mitienda.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Component
public class JwtSecretProvider {

    @Value("${mi-tienda.jwt.secret-secret-name}")
    private String secretName;

    @Value("${mi-tienda.jwt.secret-json-key:JWT_SECRET}")
    private String jsonKey;

    // Usa la región de la instancia (IMDS) o la env var AWS_REGION (la pondremos en la EC2)
    private final SecretsManagerClient client = SecretsManagerClient.builder()
            .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-2")))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private volatile String jwtSecret;

    @PostConstruct
    public void init() {
        try {
            var req = GetSecretValueRequest.builder().secretId(secretName).build();
            var res = client.getSecretValue(req);
            String secretString = res.secretString();

            String value;
            if (secretString != null && secretString.trim().startsWith("{")) {
                JsonNode root = new ObjectMapper().readTree(secretString);
                if (root.get(jsonKey) == null || root.get(jsonKey).asText().isBlank()) {
                    throw new IllegalStateException("Clave '" + jsonKey + "' no encontrada en el secreto JSON.");
                }
                value = root.get(jsonKey).asText();
            } else {
                value = secretString;
            }

            if (value == null || value.isBlank()) {
                throw new IllegalStateException("JWT secret vacío o no encontrado en: " + secretName);
            }
            this.jwtSecret = value;
        } catch (Exception e) {
            throw new IllegalStateException("No pude leer el secreto de JWT desde Secrets Manager", e);
        }
    }

    public String get() {
        return jwtSecret;
    }
}
