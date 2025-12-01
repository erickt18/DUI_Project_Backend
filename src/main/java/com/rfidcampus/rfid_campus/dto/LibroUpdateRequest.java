package com.rfidcampus.rfid_campus.dto;

import lombok.Data;

@Data
public class LibroUpdateRequest {
    private String titulo;
    private String isbn;
    private String autor;
    private String editorial;
    private Integer anio;
    private String tipoMaterial; // libro, revista, tesis
    private String categoria;
    private Integer stock;
    private Boolean disponible;
}
