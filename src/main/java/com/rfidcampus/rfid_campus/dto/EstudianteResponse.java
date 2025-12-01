package com.rfidcampus.rfid_campus.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstudianteResponse {
    private Long id;
    private String nombreCompleto;
    private String carrera;
    private String email;
    private String uidTarjeta;
    private Double saldo; // si tu modelo lo maneja
}
