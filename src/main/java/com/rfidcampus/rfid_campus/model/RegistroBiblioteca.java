package com.rfidcampus.rfid_campus.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_biblioteca")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistroBiblioteca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_estudiante_fk", nullable = false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "id_libro_fk", nullable = false)
    private Libro libro;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo = LocalDateTime.now();

    @Column(name = "fecha_devolucion_estimada")
    private LocalDateTime fechaDevolucionEstimada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Column(length = 20, nullable = false)
    private String estado = "PRESTADO"; // PRESTADO, DEVUELTO
}