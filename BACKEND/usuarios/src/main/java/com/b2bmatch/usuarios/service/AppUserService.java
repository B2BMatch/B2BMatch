package com.b2bmatch.usuarios.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserResponseDto;
import com.b2bmatch.usuarios.dto.AppUserUpdateRequestDto;
import com.b2bmatch.usuarios.model.AppUser;
import com.b2bmatch.usuarios.model.Role;
import com.b2bmatch.usuarios.repository.AppUserRepository;
import com.b2bmatch.usuarios.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService {
    //Capa de seguridad a password(al menos una mayusculta, una minuscula un numero y desde 8 caracteres)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    
    //Normalizar email(Todo en minusculas y sin espacios al comienzo y al final)
    public AppUserResponseDto register(AppUserRegisterRequestDto request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        if (appUserRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe"));

        AppUser appUser = new AppUser();
        appUser.setEmail(normalizedEmail);
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(role);
        appUser.setStatus("ACTIVE");
        appUser.setCreatedAt(LocalDateTime.now());

        return toDto(appUserRepository.save(appUser));
    }

    public List<AppUserResponseDto> findAll() {
        return appUserRepository.findByStatusNot("DELETED").stream()
                .map(this::toDto)
                .toList();
    }

    public AppUserResponseDto findById(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
        return toDto(appUser);
    }

    public List<AppUserResponseDto> findByRole(String roleName) {
        return appUserRepository.findByRole_NameAndStatusNot(roleName.toUpperCase(), "DELETED").stream()
                .map(this::toDto)
                .toList();
    }

    public void delete(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if ("DELETED".equals(appUser.getStatus())) {
            throw new IllegalArgumentException("El usuario ya está eliminado");
        }

        appUser.setStatus("DELETED");
        appUser.setUpdatedAt(LocalDateTime.now());
        appUserRepository.save(appUser);
    }

    public AppUserResponseDto reactivate(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (!"DELETED".equals(appUser.getStatus())) {
            throw new IllegalArgumentException("El usuario no está eliminado, no se puede reactivar");
        }

        appUser.setStatus("ACTIVE");
        appUser.setUpdatedAt(LocalDateTime.now());
        return toDto(appUserRepository.save(appUser));
    }

    private AppUserResponseDto toDto(AppUser appUser) {
        return new AppUserResponseDto(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getStatus(),
                appUser.getRole().getName(),
                appUser.getCreatedAt(),
                appUser.getUpdatedAt());
    }

    public AppUserResponseDto update(Long id, AppUserUpdateRequestDto request) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        String normalizedEmail = request.getEmail().toLowerCase().trim();

        appUserRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("El email ya está en uso por otro usuario");
                });

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe"));

        appUser.setEmail(normalizedEmail);
        appUser.setRole(role);
        appUser.setUpdatedAt(LocalDateTime.now());

        return toDto(appUserRepository.save(appUser));
    }
}