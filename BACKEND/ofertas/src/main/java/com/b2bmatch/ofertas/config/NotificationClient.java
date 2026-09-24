package com.b2bmatch.ofertas.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationClient {

    @Value("${app.internal-service-key:}")
    private String internalServiceKey;

    @Value("${app.notifications-base-url:http://notificaciones:8086}")
    private String baseUrl;

    private final RestClient restClient = buildRestClient();

    private static RestClient buildRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(2000));
        factory.setReadTimeout(Duration.ofMillis(3000));
        return RestClient.builder().requestFactory(factory).build();
    }

    public void notify(Long userId, String title, String message) {
        if (userId == null || internalServiceKey == null || internalServiceKey.isEmpty()) {
            return;
        }
        try {
            restClient.post()
                    .uri(baseUrl + "/api/notifications/internal")
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NotificationBody(userId, title, message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("No se pudo notificar al userId={}: {}", userId, ex.getMessage());
        }
    }

    private record NotificationBody(Long userId, String title, String message) {
    }
}