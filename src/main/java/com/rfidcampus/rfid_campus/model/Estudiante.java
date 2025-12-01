package com.rfidcampus.rfid_campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estudiantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(name = "carrera", nullable = false, length = 80)
    private String carrera;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Double saldo = 0.0;

    @Column(name = "uid_tarjeta", unique = true, length = 100)
    private String uidTarjeta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // Getter directo para obtener el nombre del rol
    public String getRolNombre() {
        return rol != null ? rol.getNombre() : "STUDENT";
    }

    @JsonIgnoreProperties("estudiante")
    @OneToOne(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private TarjetaRfid tarjeta;

    public TarjetaRfid getTarjeta() {
        return tarjeta;
    }

}
