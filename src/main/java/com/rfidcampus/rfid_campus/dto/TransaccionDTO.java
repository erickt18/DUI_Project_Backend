package com.rfidcampus.rfid_campus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TransaccionDTO {
    private String fecha;
    private String estudiante;
    private String tipo;
    private Double monto;
}
