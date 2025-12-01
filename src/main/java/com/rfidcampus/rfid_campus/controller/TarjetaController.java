package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.AsignarTarjetaDTO;
import com.rfidcampus.rfid_campus.dto.CompraMultipleRequest;
import com.rfidcampus.rfid_campus.dto.CompraRequest;
import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.service.TarjetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        try {
            TarjetaRfid tarjeta = TarjetaRfid.builder()
                    .tarjetaUid(body.get("tarjetaUid"))
                    .estado(body.get("estado"))
                    .estudiante(null)
                    .build();
            TarjetaRfid saved = tarjetaService.guardar(tarjeta);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Tarjeta registrada exitosamente",
                    "tarjetaUid", saved.getTarjetaUid(),
                    "estado", saved.getEstado()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/asignar")
    public ResponseEntity<?> asignarTarjeta(@RequestBody AsignarTarjetaDTO dto) {
        try {
            TarjetaRfid tarjeta = tarjetaService.asignarTarjeta(dto.getTarjetaUid(), dto.getIdEstudiante());
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Tarjeta asignada exitosamente",
                    "tarjetaUid", tarjeta.getTarjetaUid(),
                    "estudiante", tarjeta.getEstudiante().getNombreCompleto()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/saldo/{uid}")
    public ResponseEntity<?> consultarSaldo(@PathVariable String uid) {
        try {
            Double saldo = tarjetaService.consultarSaldo(uid);
            return ResponseEntity.ok(Map.of("saldo", saldo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/listar")
    public List<TarjetaRfid> listar() {
        return tarjetaService.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/recargar")
    public ResponseEntity<?> recargar(@RequestBody Map<String, Object> body) {
        try {
            String uid = (String) body.get("tarjetaUid");
            Double monto = Double.valueOf(body.get("monto").toString());
            Estudiante est = tarjetaService.recargarSaldo(uid, monto);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Saldo recargado exitosamente",
                    "estudiante", est.getNombreCompleto(),
                    "nuevoSaldo", est.getSaldo()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/pagar")
    public ResponseEntity<?> pagar(@RequestBody Map<String, Object> body) {
        try {
            String uid = (String) body.get("tarjetaUid");
            Double monto = Double.valueOf(body.get("monto").toString());
            Estudiante est = tarjetaService.pagar(uid, monto);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Pago realizado exitosamente",
                    "estudiante", est.getNombreCompleto(),
                    "nuevoSaldo", est.getSaldo()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Bloquear tarjeta (solo ADMIN) ----
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/bloquear/{uid}")
    public ResponseEntity<?> bloquearTarjeta(@PathVariable String uid) {
        TarjetaRfid tarjeta = tarjetaService.bloquearTarjeta(uid);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Tarjeta bloqueada",
                "uid", tarjeta.getTarjetaUid(),
                "estado", tarjeta.getEstado()));
    }

    // ---- Desbloquear tarjeta (solo ADMIN) ----
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/desbloquear/{uid}")
    public ResponseEntity<?> desbloquearTarjeta(@PathVariable String uid) {
        TarjetaRfid tarjeta = tarjetaService.desbloquearTarjeta(uid);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Tarjeta desbloqueada",
                "uid", tarjeta.getTarjetaUid(),
                "estado", tarjeta.getEstado()));
    }

    // ==== Bloquear tarjeta por el estudiante (Self-lock) ====
    @PreAuthorize("hasAuthority('STUDENT')")
    @PutMapping("/bloquear")
    public ResponseEntity<?> bloquearTarjetaEstudiante(Authentication auth) {
        String email = auth.getName();
        TarjetaRfid tarjeta = tarjetaService.bloquearTarjetaPorEstudiante(email);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Tu tarjeta fue bloqueada exitosamente ✅",
                "uid", tarjeta.getTarjetaUid(),
                "estado", tarjeta.getEstado()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/buscar/{uid}")
    public ResponseEntity<?> buscarPorUid(@PathVariable String uid) {
        TarjetaRfid tarjeta = tarjetaService.buscarPorUid(uid);

        if (tarjeta.getEstudiante() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tarjeta existe pero NO está asignada"));
        }

        Estudiante e = tarjeta.getEstudiante();

        return ResponseEntity.ok(Map.of(
                "uid", tarjeta.getTarjetaUid(),
                "estudiante", e.getNombreCompleto(),
                "saldo", e.getSaldo()));
    }

    @PreAuthorize("hasRole('ADMIN')") // O 'USER', ajústalo a tu seguridad
    @PostMapping("/comprar-multiples")
    public ResponseEntity<?> procesarCompraMultiple(@RequestBody CompraMultipleRequest req) {
        try {
            Map<String, Object> respuesta = tarjetaService.procesarCompraMultiple(req);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
