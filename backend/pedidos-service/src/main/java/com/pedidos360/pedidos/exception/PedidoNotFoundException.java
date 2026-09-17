package com.pedidos360.pedidos.exception;

public class PedidoNotFoundException extends RuntimeException {
    public PedidoNotFoundException(Long id) {
        super("Pedido no encontrado con id: " + id);
    }
}
