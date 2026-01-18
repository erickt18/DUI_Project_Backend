package com.rfidcampus.rfid_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.service.TransaccionService;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    // Endpoint para que el Admin vea todo
    @GetMapping
    public ResponseEntity<List<Transaccion>> listarTodas() {
        return ResponseEntity.ok(transaccionService.obtenerUltimasTransacciones());
    }

    // Endpoint para buscar por usuario (ID) - LISTA NORMAL
    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<Transaccion>> listarPorUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerHistorialPorUsuario(id));
    }
    
    // ✅ ESTRUCTURA DE DATOS: PILA (Stack)
    // Agregamos este endpoint para demostrar el LIFO en tu exposición
    @GetMapping("/usuario/{id}/pila")
    public ResponseEntity<List<Transaccion>> verHistorialPila(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerHistorialPila(id));
    }

    // Endpoint para buscar por usuario (Email) y Tipo (Ej: COMPRA_BAR)
    @GetMapping("/buscar")
    public ResponseEntity<List<Transaccion>> buscarPorEmailYTipo(
            @RequestParam String email, 
            @RequestParam String tipo) {
        return ResponseEntity.ok(transaccionService.buscarPorEmailYTipo(email, tipo));
    }

    // Endpoint "Mis Transacciones" (Para el usuario logueado)
    @GetMapping("/mis-movimientos")
    public ResponseEntity<List<Transaccion>> verMisMovimientos(Authentication authentication) {
        String email = authentication.getName();
        // Usamos la búsqueda filtrada por defecto
        return ResponseEntity.ok(transaccionService.buscarPorEmailYTipo(email, "COMPRA_BAR"));
    }
}