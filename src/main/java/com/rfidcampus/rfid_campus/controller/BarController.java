package com.rfidcampus.rfid_campus.controller;

import java.util.Map; // Asegúrate de tener este DTO

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.dto.CompraRequest;
import com.rfidcampus.rfid_campus.service.TarjetaService;

@RestController
@RequestMapping("/api/bar")
public class BarController {

    private final TarjetaService tarjetaService;

    public BarController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @PostMapping("/cobrar")
    public ResponseEntity<?> cobrarProducto(@RequestBody CompraRequest request) {
        try {
            // El servicio ahora devuelve un Map o un DTO, ya no un "Estudiante" directo si no quieres
            var resultado = tarjetaService.procesarCompraMultiple(request); 
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}