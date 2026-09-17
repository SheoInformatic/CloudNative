package com.pedidos360.pedidos.service;

import com.pedidos360.pedidos.client.ProductosClient;
import com.pedidos360.pedidos.dto.ItemPedidoRequest;
import com.pedidos360.pedidos.dto.PedidoRequest;
import com.pedidos360.pedidos.dto.ProductoDto;
import com.pedidos360.pedidos.exception.PedidoNotFoundException;
import com.pedidos360.pedidos.exception.ProductoInvalidoException;
import com.pedidos360.pedidos.model.EstadoPedido;
import com.pedidos360.pedidos.model.ItemPedido;
import com.pedidos360.pedidos.model.Pedido;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PedidoService {

    private final Map<Long, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final ProductosClient productosClient;

    public PedidoService(ProductosClient productosClient) {
        this.productosClient = productosClient;
    }

    public List<Pedido> listar(String clienteId) {
        var todos = pedidos.values().stream();
        if (clienteId != null && !clienteId.isBlank()) {
            todos = todos.filter(p -> clienteId.equals(p.getClienteId()));
        }
        return todos.toList();
    }

    public Pedido obtener(Long id) {
        Pedido pedido = pedidos.get(id);
        if (pedido == null) {
            throw new PedidoNotFoundException(id);
        }
        return pedido;
    }

    public Pedido crear(PedidoRequest request, String clienteId, String clienteEmail) {
        List<ItemPedido> items = request.items().stream()
                .map(this::resolverItem)
                .toList();

        Long id = sequence.incrementAndGet();
        Pedido pedido = new Pedido(id, clienteId, clienteEmail, items, EstadoPedido.CREADO, Instant.now());
        pedidos.put(id, pedido);
        return pedido;
    }

    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtener(id);
        pedido.setEstado(nuevoEstado);
        return pedido;
    }

    private ItemPedido resolverItem(ItemPedidoRequest itemRequest) {
        ProductoDto producto;
        try {
            producto = productosClient.obtenerProducto(itemRequest.productoId());
        } catch (WebClientResponseException.NotFound ex) {
            throw new ProductoInvalidoException(itemRequest.productoId());
        }

        if (producto == null || producto.stock() < itemRequest.cantidad()) {
            throw new ProductoInvalidoException(itemRequest.productoId());
        }

        return new ItemPedido(producto.id(), producto.nombre(), itemRequest.cantidad(), producto.precio());
    }
}
