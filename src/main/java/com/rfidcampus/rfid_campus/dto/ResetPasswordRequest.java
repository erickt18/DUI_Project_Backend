package com.rfidcampus.rfid_campus.dto;

public record ResetPasswordRequest(String token, String newPassword) {}
