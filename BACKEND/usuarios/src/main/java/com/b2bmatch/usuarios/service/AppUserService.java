package com.b2bmatch.usuarios.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.b2bmatch.usuarios.model.AppUser;
import com.b2bmatch.usuarios.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUser register(AppUser appUser) {
        String normalizedEmail = appUser.getEmail().toLowerCase().trim();
        appUser.setEmail(normalizedEmail);

        if (appUserRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        String rawPassword = appUser.getPasswordHash();
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException(
                "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número");
        }

        appUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        appUser.setStatus("ACTIVE");
        appUser.setCreatedAt(LocalDateTime.now());

        return appUserRepository.save(appUser);
    }

    public List<AppUser> findAll(){
        return appUserRepository.findAll();
    }

    public Optional<AppUser> findById(Long id){
        return appUserRepository.findById(id);
    }
}
