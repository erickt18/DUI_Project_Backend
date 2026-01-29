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
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;
import com.rfidcampus.rfid_campus.service.TarjetaService;

@RestController
@RequestMapping("/api/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;
    private final UsuarioRepository usuarioRepo;
    private final TarjetaRfidRepository tarjetaRepo;

    public TarjetaController(TarjetaService tarjetaService, 
                             UsuarioRepository usuarioRepo,
                             TarjetaRfidRepository tarjetaRepo) {
        this.tarjetaService = tarjetaService;
        this.usuarioRepo = usuarioRepo;
        this.tarjetaRepo = tarjetaRepo;
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

    // 2. BLOQUEO
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

    // 5. VERIFICAR TARJETA (PARA EL BAR)
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarTarjeta(@RequestParam String uid) {
        try {
            TarjetaRfid tarjeta = tarjetaService.buscarPorUid(uid);

            if (tarjeta == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Tarjeta no encontrada"));
            }

            Usuario usuario = tarjeta.getUsuario();

            if (usuario == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "No hay usuario asociado"));
            }

            return ResponseEntity.ok(Map.of(
                    "uid", tarjeta.getTarjetaUid(),
                    "saldo", usuario.getSaldo(),
                    "nombreCompleto", usuario.getNombreCompleto() != null 
                            ? usuario.getNombreCompleto() 
                            : usuario.getEmail(),
                    "usuario", usuario.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al verificar: " + e.getMessage()));
        }
    }

    // 6.  OBTENER TARJETA DE UN USUARIO (PARA EL PERFIL DEL ESTUDIANTE)
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerTarjetaPorUsuario(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = usuarioRepo.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            TarjetaRfid tarjeta = tarjetaRepo.findByUsuario(usuario).orElse(null);
            
            if (tarjeta == null) {
                return ResponseEntity.ok(Map.of(
                        "codigoRfid", "—",
                        "estado", "—",
                        "fechaEmision", "—",
                        "mensaje", "No tienes tarjeta asignada"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                    "codigoRfid", tarjeta.getTarjetaUid(),
                    "estado", tarjeta.getEstado(),
                    "fechaEmision", "—",
                    "bloqueada", "BLOQUEADA".equalsIgnoreCase(tarjeta.getEstado())
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al obtener tarjeta: " + e.getMessage()));
        }
    }

}
