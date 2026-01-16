package com.rfidcampus.rfid_campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tarjetas_rfid")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TarjetaRfid {

    @Id
    @Column(name = "tarjeta_uid", length = 64)
    private String tarjetaUid;

    @Column(name = "estado", length = 20)
    private String estado; // ACTIVA, BLOQUEADA

    // ✅ CORRECCIÓN: Ahora apunta a Usuario, no a Estudiante
    @JsonIgnoreProperties("tarjeta")
    @OneToOne // Relación 1 a 1 (Una tarjeta, un usuario)
    @JoinColumn(name = "id_usuario_fk", referencedColumnName = "id_usuario")
    private Usuario usuario;
}