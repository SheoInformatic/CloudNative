package com.pedidos360.pedidos.controller;

import com.pedidos360.pedidos.dto.PedidoRequest;
import com.pedidos360.pedidos.model.EstadoPedido;
import com.pedidos360.pedidos.model.Pedido;
import com.pedidos360.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Microservicio de pedidos. Al igual que productos-service, solo es alcanzable desde
 * bff-service dentro de la red interna de Azure Container Apps.
 *
 * El BFF ya valido el JWT y aplico autorizacion por rol antes de reenviar la peticion;
 * aqui recibimos la identidad del usuario ya resuelta via cabeceras internas de confianza
 * (X-User-Id, X-User-Email), que el BFF agrega tras verificar el token.
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> listar(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return pedidoService.listar(userId);
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id) {
        return pedidoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody PedidoRequest request,
                         @RequestHeader(value = "X-User-Id", required = false) String userId,
                         @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return pedidoService.crear(request, userId, userEmail);
    }

    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Long id, @RequestBody EstadoPedido nuevoEstado) {
        return pedidoService.cambiarEstado(id, nuevoEstado);
    }
}
