package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.AsistenciaRequest;
import com.rfidcampus.rfid_campus.service.AsistenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    // ✅ Registrar asistencia (RFID)
    @PostMapping("/marcar")
    public ResponseEntity<?> marcar(@RequestBody AsistenciaRequest req) {

        String result = asistenciaService.registrarAsistencia(req.getUid(), req.getAula());

        return ResponseEntity.ok(
            Map.of(
                "status", result,
                "uid", req.getUid(),
                "aula", req.getAula()
            )
        );
    }

    // ✅ Consultar historial por ID del estudiante
    @GetMapping("/historial/estudiante/{id}")
    public ResponseEntity<?> historialPorEstudiante(@PathVariable Long id) {
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciaPorEstudiante(id));
    }

    // ✅ Consultar historial por aula
    @GetMapping("/historial/aula/{aula}")
    public ResponseEntity<?> historialPorAula(@PathVariable String aula) {
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciaPorAula(aula));
    }

    // ✅ Consultar historial por UID de tarjeta
    @GetMapping("/historial/uid/{uid}")
    public ResponseEntity<?> historialPorUid(@PathVariable String uid) {
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciaPorUid(uid));
    }
}
