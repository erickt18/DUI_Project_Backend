package com.rfidcampus.rfid_campus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DashboardResponse {
    private long totalEstudiantes;
    private long totalTransacciones;
    private double totalSaldo;
    private long totalProductos;
    private List<TransaccionDTO> ultimas;
}
