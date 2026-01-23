package com.rfidcampus.rfid_campus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.TarjetaRfid; // <--- TU MODELO REAL
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository; // <--- TU REPO REAL
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;
import com.rfidcampus.rfid_campus.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final TarjetaRfidRepository tarjetaRfidRepository; // <--- CAMBIO AQUÍ

    public UsuarioController(UsuarioService usuarioService, 
                             UsuarioRepository usuarioRepository, 
                             TarjetaRfidRepository tarjetaRfidRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.tarjetaRfidRepository = tarjetaRfidRepository;
    }

    // =======================================================
    // 🆕 MÉTODO CORREGIDO PARA TU ENTIDAD TarjetaRfid
    // =======================================================
    @PutMapping("/{id}/asignar-datos")
    public ResponseEntity<?> asignarTarjetaYRol(@PathVariable Long id, @RequestBody Map<String, String> datos) {
        
        String nuevoRol = datos.get("rol");          
        String uidRecibido = datos.get("tarjetaUid"); 

        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Asignar Rol (Lógica simple para actualizar String, ajusta si usas entidad Rol)
        if (nuevoRol != null && !nuevoRol.isEmpty()) {
            // usuario.setRol(nuevoRol); // Descomenta si tu Usuario usa String para rol
            // Si usas Entidad Rol, aquí deberías buscar el rol en RolRepository
        }

        // 3. Asignar Tarjeta RFID
        if (uidRecibido != null && !uidRecibido.isEmpty()) {
            
            // Buscamos si la tarjeta ya existe por su ID (que es el UID)
            TarjetaRfid tarjeta = tarjetaRfidRepository.findById(uidRecibido)
                    .orElse(TarjetaRfid.builder()
                        .tarjetaUid(uidRecibido)
                        .estado("ACTIVA")
                        .build());
            
            // VINCULACIÓN: Tu entidad TarjetaRfid es la dueña de la relación.
            // Le decimos a la tarjeta: "Tu dueño es este usuario".
            tarjeta.setUsuario(usuario);
            
            // Guardamos la TARJETA para que se actualice la columna 'id_usuario_fk'
            tarjetaRfidRepository.save(tarjeta);
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Datos asignados correctamente."));
    }

    // =======================================================
    // TUS MÉTODOS ANTERIORES
    // =======================================================

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
    
    // Este método busca por ID de tarjeta (UID)
    @GetMapping("/buscar/rfid/{uid}")
    public ResponseEntity<Usuario> buscarPorRfid(@PathVariable String uid) {
        // OJO: Aquí quizás debas actualizar tu lógica en UsuarioService
        // para buscar en la tabla tarjetas_rfid
        return usuarioService.buscarPorUid(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}