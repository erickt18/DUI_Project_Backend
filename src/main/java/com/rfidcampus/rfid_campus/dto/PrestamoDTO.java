package com.rfidcampus.rfid_campus.dto;

import java.time.LocalDate;

public class PrestamoDTO {
    private LocalDate fechaPrestamo;
    private String tituloLibro;
    private String estado;

    public PrestamoDTO(LocalDate fechaPrestamo, String tituloLibro, String estado) {
        this.fechaPrestamo = fechaPrestamo;
        this.tituloLibro = tituloLibro;
        this.estado = estado;
    }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public String getTituloLibro() { return tituloLibro; }
    public String getEstado() { return estado; }
}
