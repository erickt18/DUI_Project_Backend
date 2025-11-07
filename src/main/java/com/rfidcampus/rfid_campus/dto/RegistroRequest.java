package com.rfidcampus.rfid_campus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroRequest {
    private String nombreCompleto;
    private String carrera;
    private String email;
    private String password;
    private String rolNombre; // <-- Permite indicar el rol al registrar
}
