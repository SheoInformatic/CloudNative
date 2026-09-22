package com.pedidos360.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PedidoRequest(@NotEmpty @Valid List<ItemPedidoRequest> items) {
}
