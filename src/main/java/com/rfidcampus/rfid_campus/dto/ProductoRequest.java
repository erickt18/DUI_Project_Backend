package com.rfidcampus.rfid_campus.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max=120) String nombre,
        @Size(max=255) String descripcion,
        @NotNull @DecimalMin("0.00") BigDecimal precio,
        @NotNull @Min(0) Integer stock,
        @NotNull Boolean activo
) {}
