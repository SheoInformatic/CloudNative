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
 * Fachada del BFF hacia productos-service.
 * Todas las rutas requieren un JWT valido (verificado en SecurityConfig).
 * Las operaciones de escritura ademas requieren el rol "Admin" asignado en Azure AD.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductosController {

    private final DownstreamClient downstreamClient;
    private final String productosBaseUrl;

    public ProductosController(DownstreamClient downstreamClient,
                                @Value("${servicios.productos.url}") String productosBaseUrl) {
        this.downstreamClient = downstreamClient;
        this.productosBaseUrl = productosBaseUrl;
    }

    @GetMapping
    public ResponseEntity<Object> listar(@RequestParam(required = false) String q,
                                          @AuthenticationPrincipal Jwt jwt) {
        String path = "/api/v1/productos" + (q != null ? "?q=" + q : "");
        return downstreamClient.forward(productosBaseUrl, path, HttpMethod.GET, null, Object.class,
                JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(productosBaseUrl, "/api/v1/productos/" + id, HttpMethod.GET, null,
                Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }

    @PostMapping
    @PreAuthorize("hasRole(Admin)")
    public ResponseEntity<Object> crear(@RequestBody Object body, @AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(productosBaseUrl, "/api/v1/productos", HttpMethod.POST, body,
                Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole(Admin)")
    public ResponseEntity<Object> actualizar(@PathVariable Long id, @RequestBody Object body,
                                              @AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(productosBaseUrl, "/api/v1/productos/" + id, HttpMethod.PUT, body,
                Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(Admin)")
    public ResponseEntity<Object> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return downstreamClient.forward(productosBaseUrl, "/api/v1/productos/" + id, HttpMethod.DELETE, null,
                Object.class, JwtUserContext.userId(jwt), JwtUserContext.email(jwt));
    }
}
