package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import com.rfidcampus.rfid_campus.service.EstudianteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final TransaccionRepository transaccionRepo;

    public EstudianteController(EstudianteService estudianteService, 
                               TransaccionRepository transaccionRepo) {
        this.estudianteService = estudianteService;
        this.transaccionRepo = transaccionRepo;
    }

    // Solo admin puede registrar estudiantes
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/registrar")
    public ResponseEntity<Estudiante> registrar(@RequestBody Estudiante estudiante) {
        Estudiante nuevo = estudianteService.guardar(estudiante);
        return ResponseEntity.ok(nuevo);
    }

    // Solo admin puede listar todos los estudiantes
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listar")
    public List<Estudiante> listar() {
        return estudianteService.listarTodos();
    }

    @GetMapping("/buscar")
    public ResponseEntity<Estudiante> buscar(@RequestParam String email) {
        return estudianteService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- Endpoints para ESTUDIANTE (acceso propio) ----------

    // Perfil propio
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mi-perfil")
    public ResponseEntity<Estudiante> miPerfil(Authentication auth) {
        String email = auth.getName();
        return estudianteService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Saldo propio
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mi-saldo")
    public ResponseEntity<Double> saldo(Authentication auth) {
        String email = auth.getName();
        return estudianteService.buscarPorEmail(email)
                .map(est -> ResponseEntity.ok(est.getSaldo()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Historial de transacciones propio
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mis-transacciones")
    public ResponseEntity<List<Transaccion>> historialTransacciones(Authentication auth) {
        String email = auth.getName();
        return estudianteService.buscarPorEmail(email)
                .map(est -> ResponseEntity.ok(transaccionRepo.findByEstudianteIdOrderByFechaDesc(est.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    // (Opcional) Consultar saldo por ID (si quieres evitar/permitir admin/otros)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/saldo")
    public ResponseEntity<Double> consultarSaldo(@PathVariable Long id) {
        return estudianteService.buscarPorId(id)
                .map(est -> ResponseEntity.ok(est.getSaldo()))
                .orElse(ResponseEntity.notFound().build());
    }

    // (Opcional) Obtener datos del estudiante por ID (solo admin)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> obtenerPorId(@PathVariable Long id) {
        return estudianteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // (Opcional) Historial por ID (admin)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/transacciones")
    public ResponseEntity<List<Transaccion>> obtenerHistorial(@PathVariable Long id) {
        List<Transaccion> historial = transaccionRepo.findByEstudianteIdOrderByFechaDesc(id);
        return ResponseEntity.ok(historial);
    }
}
