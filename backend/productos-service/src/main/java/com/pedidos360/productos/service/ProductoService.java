package com.pedidos360.productos.service;

import com.pedidos360.productos.exception.ProductoNotFoundException;
import com.pedidos360.productos.model.Producto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductoService {

    private final Map<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public ProductoService() {
        crear(new Producto(null, "SKU-001", "Notebook 14\"", "Notebook 14 pulgadas, 16GB RAM", new BigDecimal("499990"), 25));
        crear(new Producto(null, "SKU-002", "Mouse inalambrico", "Mouse ergonomico inalambrico", new BigDecimal("12990"), 120));
        crear(new Producto(null, "SKU-003", "Teclado mecanico", "Teclado mecanico switches rojos", new BigDecimal("39990"), 60));
    }

    public List<Producto> listar() {
        return List.copyOf(productos.values());
    }

    public Producto obtener(Long id) {
        Producto producto = productos.get(id);
        if (producto == null) {
            throw new ProductoNotFoundException(id);
        }
        return producto;
    }

    public Producto crear(Producto producto) {
        Long id = sequence.incrementAndGet();
        producto.setId(id);
        productos.put(id, producto);
        return producto;
    }

    public Producto actualizar(Long id, Producto cambios) {
        Producto existente = obtener(id);
        existente.setNombre(cambios.getNombre());
        existente.setDescripcion(cambios.getDescripcion());
        existente.setPrecio(cambios.getPrecio());
        existente.setStock(cambios.getStock());
        return existente;
    }

    public void eliminar(Long id) {
        obtener(id);
        productos.remove(id);
    }

    public Collection<Producto> buscarPorNombre(String texto) {
        String needle = texto.toLowerCase();
        return productos.values().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(needle))
                .toList();
    }
}
