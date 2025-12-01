package com.rfidcampus.rfid_campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tarjetas_rfid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaRfid {

    @Id
    @Column(name = "tarjeta_uid", length = 64) // ancho holgado
    private String tarjetaUid;

    // ✅ La tarjeta puede estar sin asignar (NULL en BD)
    @JsonIgnoreProperties("tarjeta")
    @ManyToOne
    @JoinColumn(name = "id_estudiante_fk")
    private Estudiante estudiante;

    @Column(name = "estado", length = 20)
    private String estado;
}
