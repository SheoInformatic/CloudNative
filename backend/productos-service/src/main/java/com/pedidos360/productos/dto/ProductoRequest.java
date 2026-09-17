package com.pedidos360.productos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank String sku,
        @NotBlank String nombre,
        String descripcion,
        @NotNull @PositiveOrZero BigDecimal precio,
        @NotNull @PositiveOrZero Integer stock
) {
}
