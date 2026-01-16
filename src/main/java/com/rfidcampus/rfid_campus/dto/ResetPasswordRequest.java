package com.rfidcampus.rfid_campus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    private String token;       // ✅ Esto soluciona req.getToken()
    private String newPassword; // ✅ Esto soluciona req.getNewPassword()
}