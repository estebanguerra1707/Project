package com.mx.mitienda.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleMapsDistanceClient {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Value("${delivery.origin-lat}")
    private String originLat;

    @Value("${delivery.origin-lon}")
    private String originLon;

    private final RestTemplate restTemplate;

    public GoogleMapsDistanceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Devuelve la distancia en kilómetros entre la cocina y el cliente.
     */
    public double calculateDistanceInKm(double destinationLat, double destinationLon) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s,%s&destinations=%s,%s&mode=driving&key=%s",
                originLat, originLon, destinationLat, destinationLon, apiKey
        );

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && "OK".equals(response.path("status").asText())) {
                JsonNode element = response.path("rows").get(0).path("elements").get(0);

                if ("OK".equals(element.path("status").asText())) {
                    // Google devuelve la distancia en metros
                    int distanceInMetros = element.path("distance").path("value").asInt();
                    return distanceInMetros / 1000.0;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al consultar Google Maps API: " + e.getMessage());
        }

        return -1.0; // Indicador de error o fuera de ruta
    }
}