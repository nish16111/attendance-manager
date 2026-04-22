package com.rs.attendanceManager.Service.auth;

import com.rs.attendanceManager.Dto.auth.AuthLoginRequest;
import com.rs.attendanceManager.Dto.auth.AuthRegisterRequest;
import com.rs.attendanceManager.Dto.auth.AuthResponse;

public interface AuthService {

    AuthResponse register(AuthRegisterRequest request);

    AuthResponse login(AuthLoginRequest request);
}
