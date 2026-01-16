package com.rfidcampus.rfid_campus.dto;

import java.util.List;

import lombok.Data;

@Data
public class CompraRequest {
    private String tarjetaUid;        // UID de la tarjeta leída
    private List<Long> productosIds;  // IDs de los productos a comprar (Ej: [1, 5, 2])
}