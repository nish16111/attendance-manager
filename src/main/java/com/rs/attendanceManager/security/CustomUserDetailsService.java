package com.rs.attendanceManager.security;

import com.rs.attendanceManager.Repo.AppUserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepo appUserRepo;

    public CustomUserDetailsService(AppUserRepo appUserRepo) {
        this.appUserRepo = appUserRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedLogin = username == null ? "" : username.trim();

        return appUserRepo.findByEmailIgnoreCase(normalizedLogin)
                .or(() -> appUserRepo.findByUsernameIgnoreCase(normalizedLogin))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or username: " + normalizedLogin));
    }
}
