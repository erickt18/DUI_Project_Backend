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

    // Listar normal (sin orden manual)
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }



    // Opciones: intercambio, seleccion, insercion, shell
    @GetMapping("/ordenar")
    public ResponseEntity<List<Producto>> listarOrdenados(@RequestParam(defaultValue = "intercambio") String metodo) {
        return ResponseEntity.ok(productoService.listarProductosOrdenados(metodo));
    }

    // ENDPOINT BÚSQUEDA BINARIA
    @GetMapping("/buscar-precio")
    public ResponseEntity<?> buscarPorPrecio(@RequestParam double precio) {
        Producto p = productoService.buscarPorPrecioBinario(precio);
        if (p != null) return ResponseEntity.ok(p);
        return ResponseEntity.notFound().build();
    }
    
    // Guardar (Para Admin)
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto p) {
        return ResponseEntity.ok(productoService.guardar(p));
    }
    // 🆕 EDITAR PRODUCTO (PUT)
    // Sirve para cambiar el precio o el stock de una hamburguesa
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoEditado) {
        // Opción rápida usando el Repo directo o a través del servicio
        // Aquí asumo que agregas un método 'actualizar' en tu ProductoService
        // O lo haces directo aquí si es urgente:
        Producto p = productoService.buscarPorId(id); // Necesitas este método en el service
        if (p == null) return ResponseEntity.notFound().build();
        
        p.setNombre(productoEditado.getNombre());
        p.setPrecio(productoEditado.getPrecio());
        p.setStock(productoEditado.getStock());
        
        return ResponseEntity.ok(productoService.guardar(p));
    }

    // 🆕 ELIMINAR PRODUCTO (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id); // Necesitas agregar este método simple en el Service
        return ResponseEntity.ok().build();
    }
    
}