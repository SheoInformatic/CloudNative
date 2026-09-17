package com.pedidos360.pedidos.exception;

public class ProductoInvalidoException extends RuntimeException {
    public ProductoInvalidoException(Long productoId) {
        super("El producto " + productoId + " no existe o no tiene stock suficiente");
    }
}
