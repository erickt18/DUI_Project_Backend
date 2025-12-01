package com.rfidcampus.rfid_campus.dto;

import lombok.Data;

@Data
public class CompraRequest {
    private String tarjetaUid;
    private Long productoId;
}
