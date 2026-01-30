package com.rfidcampus.rfid_campus.model;

import java.math.BigDecimal; 
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String carrera;
    
    @Column(name = "fecha_nacimiento")
    private LocalDateTime fechaNacimiento;

    // ✅ RELACIÓN BIDIRECCIONAL (mappedBy porque TarjetaRfid es el dueño)
    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("usuario")
    private TarjetaRfid tarjeta;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;
    
    @Builder.Default
    private Boolean activo = true;

    // RELACIÓN CON ROL
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getRolNombre() {
        return rol != null ? rol.getNombre() : "ROLE_ESTUDIANTE";
    }
    
    // ✅ Helper para obtener el UID de la tarjeta fácilmente
    public String getUidTarjeta() {
        return tarjeta != null ? tarjeta.getTarjetaUid() : null; 
    }
}
