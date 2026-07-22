package com.b2bmatch.usuarios.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.b2bmatch.usuarios.model.AppUser;
import com.b2bmatch.usuarios.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUser register(AppUser appUser){
        if(appUserRepository.findByEmail(appUser.getEmail()).isPresent()){
            throw new IllegalArgumentException("El email ya está registrado");
        }

        appUser.setPasswordHash(passwordEncoder.encode(appUser.getPasswordHash()));
        appUser.setStatus("ACTIVE");
        appUser.setCreatedAt(LocalDateTime.now());

        return appUserRepository.save(appUser);
    }
}
