package com.rfidcampus.rfid_campus.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registro_biblioteca") 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistroBiblioteca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long id;

    
    @ManyToOne
    @JoinColumn(name = "id_usuario_fk", nullable = false) 
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_libro_fk", nullable = false)
    private Libro libro;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_devolucion_estimada")
    private LocalDateTime fechaDevolucionEstimada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Builder.Default
    @Column(length = 20, nullable = false)
    private String estado = "PRESTADO"; // PRESTADO, DEVUELTO, CON_RETRASO

   
    @PrePersist
    protected void onCreate() {
        if (fechaPrestamo == null) {
            fechaPrestamo = LocalDateTime.now();
        }
    }
}