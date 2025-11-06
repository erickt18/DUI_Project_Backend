package com.rfidcampus.rfid_campus.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_asistencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistroAsistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_estudiante_fk", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false)
    private String aula;

    @Builder.Default
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now(); // ✅ default respetado por @Builder

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "PRESENTE";

    // ✅ Cinturón y tirantes: si algo llega null, lo fijamos antes del INSERT
    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
        if (estado == null) estado = "PRESENTE";
    }
}
