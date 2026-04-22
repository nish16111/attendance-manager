package com.rs.attendanceManager.Service.auth;

import com.rs.attendanceManager.Dto.auth.AuthLoginRequest;
import com.rs.attendanceManager.Dto.auth.AuthRegisterRequest;
import com.rs.attendanceManager.Dto.auth.AuthResponse;
import com.rs.attendanceManager.Entity.AppUser;
import com.rs.attendanceManager.Entity.AppUserRole;
import com.rs.attendanceManager.Repo.AppUserRepo;
import com.rs.attendanceManager.exception.ConflictException;
import com.rs.attendanceManager.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AppUserRepo appUserRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            AppUserRepo appUserRepo,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.appUserRepo = appUserRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse register(AuthRegisterRequest request) {
        String normalizedUsername = request.getUsername().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (appUserRepo.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new ConflictException("Username is already taken");
        }
        if (appUserRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already in use");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(normalizedUsername);
        appUser.setEmail(normalizedEmail);
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(AppUserRole.USER);
        appUser.setEnabled(true);

        AppUser savedUser = appUserRepo.save(appUser);
        String accessToken = jwtService.generateAccessToken(savedUser);

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.getAccessTokenExpirationMs() / 1000,
                savedUser.getUsername(),
                savedUser.getRole().name()
        );
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        String normalizedLogin = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedLogin, request.getPassword())
        );

        AppUser appUser = (AppUser) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(appUser);

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.getAccessTokenExpirationMs() / 1000,
                appUser.getUsername(),
                appUser.getRole().name()
        );
    }
}
