package com.pedidos360.bff.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente generico hacia los microservicios internos (pedidos-service, productos-service).
 * Agrega las cabeceras de identidad ya validada por este BFF, para que los microservicios
 * de negocio no necesiten volver a interpretar el JWT.
 */
@Component
public class DownstreamClient {

    private final RestTemplate restTemplate;

    public DownstreamClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> ResponseEntity<T> forward(String baseUrl, String path, HttpMethod method,
                                          Object body, Class<T> responseType,
                                          String userId, String userEmail) {
        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.add("X-User-Id", userId);
        }
        if (userEmail != null) {
            headers.add("X-User-Email", userEmail);
        }
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl + path, method, entity, responseType);
    }
}
