package com.rfidcampus.rfid_campus.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id, String nombre, String descripcion,
        BigDecimal precio, Integer stock, Boolean activo
) {}
