package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.AsignarTarjetaDTO;
import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.service.TarjetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    // Solo admin puede registrar tarjeta sin estudiante
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
                "estado", saved.getEstado()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Solo admin puede asignar tarjeta
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/asignar")
    public ResponseEntity<?> asignarTarjeta(@RequestBody AsignarTarjetaDTO dto) {
        try {
            TarjetaRfid tarjeta = tarjetaService.asignarTarjeta(dto.getTarjetaUid(), dto.getIdEstudiante());
            return ResponseEntity.ok(Map.of(
                "mensaje", "Tarjeta asignada exitosamente",
                "tarjetaUid", tarjeta.getTarjetaUid(),
                "estudiante", tarjeta.getEstudiante().getNombreCompleto()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Consultar saldo (solo estudiante)
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

    // Listar tarjetas (puede ser solo admin si quieres, aquí lo dejo público)
    @GetMapping("/listar")
    public List<TarjetaRfid> listar() {
        return tarjetaService.listar();
    }

    // Recargar saldo SOLO ADMIN
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
                "nuevoSaldo", est.getSaldo()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Pagar SOLO ESTUDIANTE
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
                "nuevoSaldo", est.getSaldo()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
