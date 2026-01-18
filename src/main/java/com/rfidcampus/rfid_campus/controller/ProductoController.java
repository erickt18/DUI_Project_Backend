package com.rfidcampus.rfid_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    // ✅ ENDPOINT PARA TUS ALGORITMOS
    // Prueba en Postman: GET /api/productos/ordenar?metodo=shell
    // Opciones: intercambio, seleccion, insercion, shell
    @GetMapping("/ordenar")
    public ResponseEntity<List<Producto>> listarOrdenados(@RequestParam(defaultValue = "intercambio") String metodo) {
        return ResponseEntity.ok(productoService.listarProductosOrdenados(metodo));
    }

    // ✅ ENDPOINT BÚSQUEDA BINARIA
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
}