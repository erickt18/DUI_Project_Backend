package com.rfidcampus.rfid_campus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.dto.CompraRequest;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.service.TarjetaService;

@RestController
@RequestMapping("/api/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    // 1. GESTIÓN BÁSICA
    @GetMapping
    public ResponseEntity<List<TarjetaRfid>> listarTodas() {
        return ResponseEntity.ok(tarjetaService.listar());
    }

    @PostMapping("/asignar")
    public ResponseEntity<?> asignar(@RequestParam String uid, @RequestParam Long usuarioId) {
        try {
            tarjetaService.asignarTarjeta(uid, usuarioId);
            return ResponseEntity.ok(Map.of("message", "Tarjeta asignada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. BLOQUEO (Admin por UID y Estudiante por Token)
    @PostMapping("/bloquear/{uid}")
    public ResponseEntity<?> bloquear(@PathVariable String uid) {
        try {
            tarjetaService.bloquearTarjeta(uid);
            return ResponseEntity.ok(Map.of("message", "Tarjeta bloqueada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bloquear-mi-tarjeta") 
    public ResponseEntity<?> bloquearMiTarjeta(Authentication authentication) {
        try {
            tarjetaService.bloquearTarjetaPorEmail(authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Tu tarjeta ha sido bloqueada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 3. BAR: RECARGAR SALDO 
    @PostMapping("/recargar")
    public ResponseEntity<?> recargarSaldo(@RequestParam String uid, @RequestParam Double monto) {
        try {
            Usuario u = tarjetaService.recargarSaldo(uid, monto);
            return ResponseEntity.ok(Map.of(
                "message", "Recarga exitosa",
                "nuevoSaldo", u.getSaldo(),
                "usuario", u.getNombreCompleto()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. BAR: COBRAR CON RFID 
    @PostMapping("/compra")
    public ResponseEntity<?> procesarCompra(@RequestBody CompraRequest request) {
        try {
            return ResponseEntity.ok(tarjetaService.procesarCompraMultiple(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}