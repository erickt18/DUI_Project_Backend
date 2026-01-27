package com.rfidcampus.rfid_campus.dto;

import lombok.Data;

@Data
public class PrestamoRequest {
    private String uidTarjeta;
    private Long idLibro;
    private Integer diasPrestamo; 
}