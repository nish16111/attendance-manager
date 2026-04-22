package com.rs.attendanceManager.Dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username,
        String role
) {
}
