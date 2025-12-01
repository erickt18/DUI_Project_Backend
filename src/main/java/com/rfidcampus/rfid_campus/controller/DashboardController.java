package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.DashboardDTO;
import com.rfidcampus.rfid_campus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/info")
    public DashboardDTO getDashboardData() {
        return dashboardService.getDashboardData();
    }
}
