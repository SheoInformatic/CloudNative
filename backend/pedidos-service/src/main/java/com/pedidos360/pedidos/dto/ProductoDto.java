package com.pedidos360.pedidos.dto;

import java.math.BigDecimal;

/**
 * Representa la respuesta que entrega productos-service.
 */
public record ProductoDto(Long id, String sku, String nombre, String descripcion,
                           BigDecimal precio, Integer stock) {
}
