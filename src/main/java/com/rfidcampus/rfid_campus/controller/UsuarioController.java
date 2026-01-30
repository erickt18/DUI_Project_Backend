package com.rfidcampus.rfid_campus.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;
import com.rfidcampus.rfid_campus.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService,
            UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    // ✅ ENDPOINT CORREGIDO - USA EL SERVICE
    @PutMapping("/{id}/asignar-datos")
    public ResponseEntity<?> asignarTarjetaYRol(@PathVariable Long id, @RequestBody Map<String, String> datos) {

        try {
            String nuevoRolNombre = datos.get("rol");
            String uidRecibido = datos.get("tarjetaUid");

            // ✅ USAR EL MÉTODO DEL SERVICE QUE CREA LA TARJETA AUTOMÁTICAMENTE
            Usuario usuario = usuarioService.asignarDatos(id, nuevoRolNombre, uidRecibido);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuario actualizado correctamente",
                    "usuario", usuario
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mi-perfil")
    public ResponseEntity<Usuario> verMiPerfil(Authentication authentication) {
        String email = authentication.getName();
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // ESTADÍSTICAS DEL DASHBOARD
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        long totalUsuarios = usuarioRepository.count();
        long totalEstudiantes = usuarioRepository.countEstudiantes();
        long estudiantesActivos = usuarioRepository.countEstudiantesActivos();
        BigDecimal dineroTotal = usuarioRepository.sumarSaldoTotal();
        long tarjetasAsignadas = usuarioRepository.countUsuariosConTarjeta();

        double porcentaje = totalUsuarios > 0 ? ((double) tarjetasAsignadas / totalUsuarios) * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEstudiantes", totalEstudiantes);
        stats.put("estudiantesActivos", estudiantesActivos);
        stats.put("dineroTotal", dineroTotal);
        stats.put("tarjetasAsignadas", (int) porcentaje);
        stats.put("totalUsuarios", totalUsuarios);

        return ResponseEntity.ok(stats);
    }

    @PutMapping("/mi-perfil/actualizar-carrera")
    public ResponseEntity<?> actualizarCarrera(
            Authentication authentication,
            @RequestBody Map<String, String> body) {

        try {
            String email = authentication.getName();
            String nuevaCarrera = body.get("carrera");

            if (nuevaCarrera == null || nuevaCarrera.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La carrera no puede estar vacía"));
            }

            Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.setCarrera(nuevaCarrera);
            usuarioService.guardar(usuario);

            return ResponseEntity.ok(Map.of(
                    "message", "Carrera actualizada correctamente",
                    "carrera", nuevaCarrera
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
