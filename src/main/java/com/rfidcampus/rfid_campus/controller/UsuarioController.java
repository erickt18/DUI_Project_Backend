package com.rfidcampus.rfid_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint para que el usuario logueado vea sus propios datos
    @GetMapping("/mi-perfil")
    public ResponseEntity<Usuario> verMiPerfil(Authentication authentication) {
        String email = authentication.getName();
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para Admin: Ver todos
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }
    
    // Endpoint para buscar por tarjeta (Útil para Bar y Biblioteca)
    @GetMapping("/buscar/rfid/{uid}")
    public ResponseEntity<Usuario> buscarPorRfid(@PathVariable String uid) {
        return usuarioService.buscarPorUid(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}