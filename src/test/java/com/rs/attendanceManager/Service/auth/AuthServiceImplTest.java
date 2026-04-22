package com.rs.attendanceManager.Service.auth;

import com.rs.attendanceManager.Dto.auth.AuthLoginRequest;
import com.rs.attendanceManager.Dto.auth.AuthRegisterRequest;
import com.rs.attendanceManager.Dto.auth.AuthResponse;
import com.rs.attendanceManager.Entity.AppUser;
import com.rs.attendanceManager.Entity.AppUserRole;
import com.rs.attendanceManager.Repo.AppUserRepo;
import com.rs.attendanceManager.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepo appUserRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerNormalizesEmailAndUsernameBeforeSaving() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setUsername("  TestUser  ");
        request.setEmail("  USER@Example.COM  ");
        request.setPassword("password123");

        AppUser savedUser = new AppUser();
        savedUser.setUsername("TestUser");
        savedUser.setEmail("user@example.com");
        savedUser.setRole(AppUserRole.USER);

        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(appUserRepo.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(savedUser)).thenReturn("jwt-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(3600000L);

        AuthResponse response = authService.register(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepo).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getUsername()).isEqualTo("TestUser");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("TestUser");
    }

    @Test
    void loginAuthenticatesUsingNormalizedEmail() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("  USER@Example.COM  ");
        request.setPassword("password123");

        AppUser appUser = new AppUser();
        appUser.setUsername("testuser");
        appUser.setEmail("user@example.com");
        appUser.setRole(AppUserRole.USER);

        Authentication authenticationResult = new UsernamePasswordAuthenticationToken(
                appUser,
                null,
                appUser.getAuthorities()
        );

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticationResult);
        when(jwtService.generateAccessToken(appUser)).thenReturn("jwt-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(3600000L);

        AuthResponse response = authService.login(request);

        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(authCaptor.capture());

        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo("user@example.com");
        assertThat(authCaptor.getValue().getCredentials()).isEqualTo("password123");
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("testuser");
    }
}
