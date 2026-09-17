package com.pedidos360.productos.controller;

import com.pedidos360.productos.dto.ProductoRequest;
import com.pedidos360.productos.model.Producto;
import com.pedidos360.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * Microservicio de catalogo de productos.
 * En esta primera etapa es consumido unicamente por el BFF, nunca directamente por el frontend.
 */
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return List.copyOf(productoService.buscarPorNombre(q));
        }
        return productoService.listar();
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@Valid @RequestBody ProductoRequest request) {
        Producto producto = new Producto(null, request.sku(), request.nombre(),
                request.descripcion(), request.precio(), request.stock());
        return productoService.crear(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        Producto cambios = new Producto(null, request.sku(), request.nombre(),
                request.descripcion(), request.precio(), request.stock());
        return productoService.actualizar(id, cambios);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
