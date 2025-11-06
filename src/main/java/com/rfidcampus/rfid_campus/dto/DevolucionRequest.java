package com.rfidcampus.rfid_campus.dto;

import lombok.Data;

@Data
public class DevolucionRequest {
    private String uidTarjeta;
    private Long idPrestamo;
}