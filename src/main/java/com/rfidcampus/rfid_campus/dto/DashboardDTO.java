package com.rfidcampus.rfid_campus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardDTO {

    private long totalEstudiantes;
    private long totalTransacciones;
    private double totalSaldo;

    private long totalProductos;

}
