package com.pedidos360.pedidos.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Pedido {
    private Long id;
    private String clienteId;
    private String clienteEmail;
    private List<ItemPedido> items;
    private EstadoPedido estado;
    private Instant creadoEn;

    public Pedido() {
    }

    public Pedido(Long id, String clienteId, String clienteEmail, List<ItemPedido> items, EstadoPedido estado, Instant creadoEn) {
        this.id = id;
        this.clienteId = clienteId;
        this.clienteEmail = clienteEmail;
        this.items = items;
        this.estado = estado;
        this.creadoEn = creadoEn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public List<ItemPedido> getItems() { return items; }
    public void setItems(List<ItemPedido> items) { this.items = items; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }

    public BigDecimal getTotal() {
        return items.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
