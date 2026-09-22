package com.pedidos360.bff.controller;

import com.pedidos360.bff.client.DownstreamClient;
import com.pedidos360.bff.security.JwtUserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Fachada del BFF hacia pedidos-service.
 * Lectura y creacion: cualquier usuario autenticado con rol "User" o "Admin".
 * Cambio de estado de un pedido: solo rol "Admin" (por ejemplo, confirmar/enviar/cancelar).
 */
@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasAnyRole(User, Admin)")
public class PedidosController {

    private final DownstreamClient downstreamClient;
    private final String pedidosBaseUrl;

    public PedidosController(DownstreamClient downstreamClient,
                              @Value("${servicios.pedidos.url}") String pedidosBaseUrl) {
        this.downstreamClient = downstreamClient;
        this.pedidosBaseUrl = pedidosBaseUrl;
    }

    @GetMapping
    public ResponseEntity<Object> listar(@AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(pedidosBaseUrl, "/api/v1/pedidos", HttpMethod.GET, null,
                Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(pedidosBaseUrl, "/api/v1/pedidos/" + id, HttpMethod.GET, null,
                Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }

    @PostMapping
    public ResponseEntity<Object> crear(@RequestBody Object body, @AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(pedidosBaseUrl, "/api/v1/pedidos", HttpMethod.POST, body,
                Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole(Admin)")
    public ResponseEntity<Object> cambiarEstado(@PathVariable Long id, @RequestBody Object nuevoEstado,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(pedidosBaseUrl, "/api/v1/pedidos/" + id + "/estado", HttpMethod.PATCH,
                nuevoEstado, Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }
}
