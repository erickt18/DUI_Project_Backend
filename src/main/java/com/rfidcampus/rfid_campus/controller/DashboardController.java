package com.rfidcampus.rfid_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; 

import com.rfidcampus.rfid_campus.dto.DashboardDTO;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.service.DashboardService;
import com.rfidcampus.rfid_campus.service.TransaccionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final TransaccionService transaccionService; 

    @GetMapping("/info")
    public DashboardDTO getDashboardData() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/actividad-reciente")
    public ResponseEntity<List<Transaccion>> getActividadReciente(
            @RequestParam(defaultValue = "10") int limit
    ) {
        
        List<Transaccion> todas = transaccionService.obtenerUltimasTransacciones();
        return ResponseEntity.ok(todas.stream().limit(limit).toList());
    }
}