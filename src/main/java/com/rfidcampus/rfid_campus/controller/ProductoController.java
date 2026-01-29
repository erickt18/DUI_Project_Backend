package com.rfidcampus.rfid_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.Producto;
import com.rfidcampus.rfid_campus.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Listar todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    // Listar productos ordenados
    @GetMapping("/ordenar")
    public ResponseEntity<List<Producto>> listarOrdenados(
            @RequestParam(defaultValue = "intercambio") String metodo) {
        return ResponseEntity.ok(productoService.listarProductosOrdenados(metodo));
    }

    // Búsqueda binaria por precio
    @GetMapping("/buscar-precio")
    public ResponseEntity<?> buscarPorPrecio(@RequestParam double precio) {
        Producto p = productoService.buscarPorPrecioBinario(precio);
        if (p != null) return ResponseEntity.ok(p);
        return ResponseEntity.notFound().build();
    }
    
    // Crear nuevo producto
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.guardar(producto));
    }

    //  ACTUALIZAR PRODUCTO (PUT) - ESTE ES EL IMPORTANTE
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id, 
            @RequestBody Producto productoEditado) {
        
        Producto productoExistente = productoService.buscarPorId(id);
        
        if (productoExistente == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Actualizar campos
        productoExistente.setNombre(productoEditado.getNombre());
        productoExistente.setDescripcion(productoEditado.getDescripcion());
        productoExistente.setPrecio(productoEditado.getPrecio());
        productoExistente.setStock(productoEditado.getStock());
        productoExistente.setActivo(productoEditado.getActivo());
        
        Producto productoActualizado = productoService.guardar(productoExistente);
        
        return ResponseEntity.ok(productoActualizado);
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}