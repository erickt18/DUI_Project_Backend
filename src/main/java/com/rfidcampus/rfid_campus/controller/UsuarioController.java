package com.rfidcampus.rfid_campus.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.Rol;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.RolRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;
import com.rfidcampus.rfid_campus.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") 
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final TarjetaRfidRepository tarjetaRfidRepository;
    private final RolRepository rolRepository;

    public UsuarioController(UsuarioService usuarioService, 
                             UsuarioRepository usuarioRepository, 
                             TarjetaRfidRepository tarjetaRfidRepository,
                             RolRepository rolRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.tarjetaRfidRepository = tarjetaRfidRepository;
        this.rolRepository = rolRepository;
    }

    @PutMapping("/{id}/asignar-datos")
    public ResponseEntity<?> asignarTarjetaYRol(@PathVariable Long id, @RequestBody Map<String, String> datos) {
        
        String nuevoRolNombre = datos.get("rol");      
        String uidRecibido = datos.get("tarjetaUid"); 

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Asignar ROL
        if (nuevoRolNombre != null && !nuevoRolNombre.isEmpty()) {
            Optional<Rol> rolEncontrado = rolRepository.findByNombre(nuevoRolNombre);
            
            if (rolEncontrado.isPresent()) {
                usuario.setRol(rolEncontrado.get());
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "El rol " + nuevoRolNombre + " no existe en la BD.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // 2. Asignar o Quitar TARJETA RFID
        if (uidRecibido != null && !uidRecibido.isEmpty()) {
            // CASO A: ASIGNAR NUEVA TARJETA
            TarjetaRfid tarjeta = tarjetaRfidRepository.findById(uidRecibido)
                    .orElse(TarjetaRfid.builder()
                        .tarjetaUid(uidRecibido)
                        .estado("ACTIVA")
                        .build());
            
            // Vinculación bidireccional (Importante para @OneToOne)
            tarjeta.setUsuario(usuario); 
            usuario.setTarjeta(tarjeta); 
            
            tarjetaRfidRepository.save(tarjeta);
        } else {
            // CASO B: QUITAR TARJETA (Si envían string vacío)
            // Verificamos si el usuario tiene tarjeta para desvincularla
            if (usuario.getTarjeta() != null) {
                TarjetaRfid tarjetaVieja = usuario.getTarjeta();
                
                // Rompemos la relación en ambos lados
                tarjetaVieja.setUsuario(null); 
                tarjetaRfidRepository.save(tarjetaVieja); // Actualizamos la tarjeta
                
                usuario.setTarjeta(null); // Actualizamos el usuario (pondrá NULL en uid_tarjeta)
            }
        }

        usuarioRepository.save(usuario);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuario actualizado correctamente.");
        return ResponseEntity.ok(response);
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

}