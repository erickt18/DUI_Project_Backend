package com.rfidcampus.rfid_campus.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_biblioteca") // O "prestamos" si cambiaste el nombre en la BD
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistroBiblioteca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long id;

    // ✅ CORRECCIÓN CRÍTICA: Cambiado de Estudiante a Usuario
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

    @Column(length = 20, nullable = false)
    private String estado = "PRESTADO"; // PRESTADO, DEVUELTO, CON_RETRASO

    // Asegura que la fecha de préstamo se asigne automáticamente al crear
    @PrePersist
    protected void onCreate() {
        if (fechaPrestamo == null) {
            fechaPrestamo = LocalDateTime.now();
        }
    }
}