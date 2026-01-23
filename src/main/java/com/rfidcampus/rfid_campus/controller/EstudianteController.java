package com.rfidcampus.rfid_campus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.service.EstudianteService;

@RestController
@RequestMapping("/api/estudiante")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // 1. Ver Historial de Movimientos (Bar/Saldo)
    @GetMapping("/mis-transacciones")
    public ResponseEntity<List<Transaccion>> misTransacciones(Authentication auth) {
        return ResponseEntity.ok(estudianteService.obtenerMisTransacciones(auth.getName()));
    }

    // 2. Ver Libros que tengo en mi poder (Pendientes)
    @GetMapping("/mis-prestamos-pendientes")
    public ResponseEntity<List<RegistroBiblioteca>> misPrestamos(Authentication auth) {
        return ResponseEntity.ok(estudianteService.obtenerMisPrestamosPendientes(auth.getName()));
    }

    // 3. BUSCADOR LINEAL: Buscar en mis movimientos por nombre (ej: ?query=coca)
    @GetMapping("/buscar-movimiento")
    public ResponseEntity<?> buscarMovimiento(Authentication auth, @RequestParam String query) {
        List<Transaccion> resultado = estudianteService.buscarTransaccionPorDetalle(auth.getName(), query);
        if (resultado.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No se encontraron coincidencias."));
        }
        return ResponseEntity.ok(resultado);
    }

    // 4. BUSCADOR BINARIO: Buscar por monto exacto (ej: ?monto=5.50)
    @GetMapping("/buscar-monto")
    public ResponseEntity<?> buscarMonto(Authentication auth, @RequestParam Double monto) {
        Transaccion t = estudianteService.buscarTransaccionPorMonto(auth.getName(), monto);
        if (t == null) {
            return ResponseEntity.ok(Map.of("message", "No tienes ninguna transacción por ese monto exacto."));
        }
        return ResponseEntity.ok(t);
    }
}