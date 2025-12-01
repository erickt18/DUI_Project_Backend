package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import com.rfidcampus.rfid_campus.service.EstudianteService;
import com.rfidcampus.rfid_campus.service.TarjetaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final TransaccionRepository transaccionRepo;
    private final TarjetaService tarjetaService; // ✅ AÑADIDO

    public EstudianteController(
            EstudianteService estudianteService,
            TransaccionRepository transaccionRepo,
            TarjetaService tarjetaService // ✅ AÑADIDO
    ) {
        this.estudianteService = estudianteService;
        this.transaccionRepo = transaccionRepo;
        this.tarjetaService = tarjetaService; // ✅ AÑADIDO
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/registrar")
    public ResponseEntity<Estudiante> registrar(@RequestBody Estudiante estudiante) {
        Estudiante nuevo = estudianteService.guardar(estudiante);
        return ResponseEntity.ok(nuevo);
    }

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

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mi-perfil")
    public ResponseEntity<Estudiante> miPerfil(Authentication auth) {
        String email = auth.getName();
        return estudianteService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mi-saldo")
    public ResponseEntity<Double> saldo(Authentication auth) {
        String email = auth.getName();
        return estudianteService.buscarPorEmail(email)
                .map(est -> ResponseEntity.ok(est.getSaldo()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mis-transacciones")
    public ResponseEntity<List<Transaccion>> historialTransacciones(Authentication auth) {
        String email = auth.getName();
        return estudianteService.buscarPorEmail(email)
                .map(est -> ResponseEntity.ok(transaccionRepo.findByEstudianteIdOrderByFechaDesc(est.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/saldo")
    public ResponseEntity<Double> consultarSaldo(@PathVariable Long id) {
        return estudianteService.buscarPorId(id)
                .map(est -> ResponseEntity.ok(est.getSaldo()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> obtenerPorId(@PathVariable Long id) {
        return estudianteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/transacciones")
    public ResponseEntity<List<Transaccion>> obtenerHistorial(@PathVariable Long id) {
        List<Transaccion> historial = transaccionRepo.findByEstudianteIdOrderByFechaDesc(id);
        return ResponseEntity.ok(historial);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/uid/{uid}")
    public ResponseEntity<?> buscarPorUid(@PathVariable String uid) {
        var estudianteOpt = estudianteService.buscarPorUid(uid);

        if (estudianteOpt.isPresent()) {
            return ResponseEntity.ok(estudianteOpt.get());
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estudiante no encontrado para UID " + uid));
        }
    }

    // ✅ Asignar tarjeta RFID a estudiante
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/asignar-tarjeta")
    public ResponseEntity<?> asignarTarjeta(
            @RequestParam Long id,
            @RequestParam String uidTarjeta) {

        Estudiante estudiante = estudianteService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        TarjetaRfid tarjeta = tarjetaService.buscarPorUid(uidTarjeta);

        if (tarjeta.getEstudiante() != null && !tarjeta.getEstudiante().getId().equals(id)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La tarjeta ya está asignada a otro estudiante"));
        }

        tarjeta.setEstudiante(estudiante);
        estudiante.setUidTarjeta(uidTarjeta);

        tarjetaService.guardar(tarjeta);
        estudianteService.guardar(estudiante);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Tarjeta asignada correctamente ✅",
                "estudiante", estudiante.getNombreCompleto(),
                "uid", uidTarjeta
        ));
    }
}
