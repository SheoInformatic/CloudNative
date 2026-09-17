package com.pedidos360.pedidos.client;

import com.pedidos360.pedidos.dto.ProductoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Cliente HTTP hacia productos-service. Ambos servicios viven en la misma red
 * interna de Azure Container Apps y se resuelven por nombre de servicio (DNS interno),
 * por lo que este llamado nunca sale a internet.
 */
@Component
public class ProductosClient {

    private final WebClient webClient;

    public ProductosClient(@Value("${servicios.productos.url}") String productosBaseUrl) {
        this.webClient = WebClient.builder().baseUrl(productosBaseUrl).build();
    }

    public ProductoDto obtenerProducto(Long productoId) {
        return webClient.get()
                .uri("/api/v1/productos/{id}", productoId)
                .retrieve()
                .bodyToMono(ProductoDto.class)
                .block();
    }
}
