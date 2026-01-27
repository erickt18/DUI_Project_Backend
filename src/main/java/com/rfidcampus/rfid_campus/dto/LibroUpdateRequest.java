package com.rfidcampus.rfid_campus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroUpdateRequest {
    
    
    private String titulo;
    private String isbn;
    private String autor;
    private String editorial;
    private Integer anio;
    private String tipoMaterial;
    private String categoria;
    private Integer stock;
}