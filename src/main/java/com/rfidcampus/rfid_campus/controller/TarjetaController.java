package com.rfidcampus.rfid_campus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.service.TarjetaService;

@RestController
@RequestMapping("/api/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @GetMapping
    public ResponseEntity<List<TarjetaRfid>> listarTodas() {
        return ResponseEntity.ok(tarjetaService.listar());
    }

    @PostMapping("/bloquear/{uid}")
    public ResponseEntity<?> bloquear(@PathVariable String uid) {
        try {
            tarjetaService.bloquearTarjeta(uid);
            return ResponseEntity.ok(Map.of("message", "Tarjeta bloqueada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Si tenías un método para asignar tarjeta a estudiante, cámbialo para usar ID de usuario
    @PostMapping("/asignar")
    public ResponseEntity<?> asignar(@RequestParam String uid, @RequestParam Long usuarioId) {
        try {
            tarjetaService.asignarTarjeta(uid, usuarioId); // Asegúrate que tu servicio tenga este método actualizado
            return ResponseEntity.ok(Map.of("message", "Tarjeta asignada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}