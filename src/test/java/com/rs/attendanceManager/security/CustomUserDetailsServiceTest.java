package com.rs.attendanceManager.security;

import com.rs.attendanceManager.Entity.AppUser;
import com.rs.attendanceManager.Repo.AppUserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepo appUserRepo;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadsUserByEmailBeforeTryingUsername() {
        AppUser appUser = new AppUser();
        appUser.setEmail("user@example.com");
        appUser.setUsername("testuser");

        when(appUserRepo.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(appUser));

        AppUser loadedUser = (AppUser) customUserDetailsService.loadUserByUsername("  user@example.com  ");

        verify(appUserRepo).findByEmailIgnoreCase("user@example.com");
        assertThat(loadedUser.getUsername()).isEqualTo("testuser");
    }
}
