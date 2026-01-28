package com.rfidcampus.rfid_campus.controller;

import java.time.YearMonth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.dto.ReporteMensualDTO;
import com.rfidcampus.rfid_campus.service.DashboardBarService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bar/dashboard")
@RequiredArgsConstructor
public class DashboardBarController {

    private final DashboardBarService dashboardBarService;

    /**
     * Endpoint para obtener reportes mensuales con datos para gráficos
     * Ejemplo: GET /api/bar/dashboard/reportes-mensuales?mes=1&anio=2025
     * Si no se envían parámetros, usa el mes actual
     */
    @GetMapping("/reportes-mensuales")
    public ResponseEntity<ReporteMensualDTO> getReportesMensuales(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio
    ) {
        // Si no vienen parámetros, usar mes actual
        YearMonth mesActual = YearMonth.now();
        int mesConsulta = (mes != null) ? mes : mesActual.getMonthValue();
        int anioConsulta = (anio != null) ? anio : mesActual.getYear();
        
        ReporteMensualDTO reporte = dashboardBarService.generarReporteMensual(mesConsulta, anioConsulta);
        return ResponseEntity.ok(reporte);
    }
}