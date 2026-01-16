package com.rfidcampus.rfid_campus.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal; 
import java.time.LocalDateTime;

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

    // Campos opcionales (Solo para estudiantes)
    private String carrera;
    
    @Column(name = "fecha_nacimiento")
    private LocalDateTime fechaNacimiento;

    // ✅ CAMPO QUE FALTABA (Vital para que compile PasswordResetService)
    @Column(name = "uid_tarjeta", unique = true, length = 100)
    private String uidTarjeta;

    // ✅ DINERO EXACTO
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

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

    // Helper para obtener el nombre del rol fácilmente
    public String getRolNombre() {
        return rol != null ? rol.getNombre() : "ROLE_ESTUDIANTE";
    }
}