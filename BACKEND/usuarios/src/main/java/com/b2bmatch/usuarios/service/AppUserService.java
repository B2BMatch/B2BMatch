package com.b2bmatch.usuarios.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.usuarios.dto.AdminRegisterRequest;
import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserResponseDto;
import com.b2bmatch.usuarios.dto.AuthResponse;
import com.b2bmatch.usuarios.dto.LoginRequest;
import com.b2bmatch.usuarios.dto.RegisterRequest;
import com.b2bmatch.usuarios.exception.AuthException;
import com.b2bmatch.usuarios.model.AppUser;
import com.b2bmatch.usuarios.model.CompanyProfile;
import com.b2bmatch.usuarios.model.CustomerProfile;
import com.b2bmatch.usuarios.model.ProfessionalProfile;
import com.b2bmatch.usuarios.model.Role;
import com.b2bmatch.usuarios.repository.AppUserRepository;
import com.b2bmatch.usuarios.repository.CompanyProfileRepository;
import com.b2bmatch.usuarios.repository.CustomerProfileRepository;
import com.b2bmatch.usuarios.repository.ProfessionalProfileRepository;
import com.b2bmatch.usuarios.repository.RoleRepository;
import com.b2bmatch.usuarios.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }

        if ("COMPANY".equals(request.getRole())) {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()
                    || request.getTaxId() == null || request.getTaxId().isBlank()) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "companyName y taxId son obligatorios para empresas");
            }
            if (companyProfileRepository.existsByTaxId(request.getTaxId())) {
                throw new AuthException(HttpStatus.CONFLICT, "Ya existe una empresa registrada con ese RUT/taxId");
            }
        } else {
            if (request.getFirstName() == null || request.getFirstName().isBlank()
                    || request.getLastName() == null || request.getLastName().isBlank()) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "firstName y lastName son obligatorios");
            }
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new AuthException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol no configurado: " + request.getRole()));

        LocalDateTime now = LocalDateTime.now();

        AppUser user = new AppUser();
        user.setRole(role);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedAt(now);
        user = appUserRepository.save(user);

        String displayName = switch (request.getRole()) {
            case "COMPANY" -> {
                CompanyProfile profile = new CompanyProfile();
                profile.setUser(user);
                profile.setCompanyName(request.getCompanyName());
                profile.setTaxId(request.getTaxId());
                profile.setIndustry(request.getIndustry());
                profile.setCreatedAt(now);
                companyProfileRepository.save(profile);
                yield profile.getCompanyName();
            }
            case "PROFESSIONAL" -> {
                ProfessionalProfile profile = new ProfessionalProfile();
                profile.setUser(user);
                profile.setFirstName(request.getFirstName());
                profile.setLastName(request.getLastName());
                profile.setPhone(request.getPhone());
                profile.setExperienceYears(0);
                profile.setCreatedAt(now);
                professionalProfileRepository.save(profile);
                yield profile.getFirstName() + " " + profile.getLastName();
            }
            default -> {
                CustomerProfile profile = new CustomerProfile();
                profile.setUser(user);
                profile.setFirstName(request.getFirstName());
                profile.setLastName(request.getLastName());
                profile.setPhone(request.getPhone());
                profile.setCreatedAt(now);
                customerProfileRepository.save(profile);
                yield profile.getFirstName() + " " + profile.getLastName();
            }
        };

        String token = jwtService.generateToken(user.getId(), user.getEmail(), role.getName());
        return new AuthResponse(token, user.getId(), user.getEmail(), role.getName(), displayName);
    }

    @Transactional
    public AuthResponse registerAdmin(AdminRegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }

        Role role = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new AuthException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol ADMIN no configurado"));

        AppUser user = new AppUser();
        user.setRole(role);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user = appUserRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), "ADMIN");
        return new AuthResponse(token, user.getId(), user.getEmail(), "ADMIN", user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AuthException(HttpStatus.FORBIDDEN, "La cuenta no está activa");
        }

        String displayName = resolveDisplayName(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().getName());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().getName(), displayName);
    }

    @Transactional(readOnly = true)
    public List<AppUserResponseDto> findAll() {
        return appUserRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUserResponseDto findById(Long id) {
        return toResponseDto(findUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AppUserResponseDto> findByRole(String roleName) {
        return appUserRepository.findByRole_Name(roleName).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public AppUserResponseDto register(AppUserRegisterRequestDto request) {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(request.getEmail());
        registerRequest.setPassword(request.getPassword());
        registerRequest.setRole(request.getRole());
        registerRequest.setFirstName(request.getFirstName());
        registerRequest.setLastName(request.getLastName());
        registerRequest.setPhone(request.getPhone());
        registerRequest.setCompanyName(request.getCompanyName());
        registerRequest.setTaxId(request.getTaxId());
        registerRequest.setIndustry(request.getIndustry());

        AuthResponse response = register(registerRequest);
        return toResponseDto(findUserOrThrow(response.getUserId()));
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = findUserOrThrow(id);
        user.setStatus("DELETED");
        user.setUpdatedAt(LocalDateTime.now());
        appUserRepository.save(user);
    }

    @Transactional
    public AppUserResponseDto reactivate(Long id) {
        AppUser user = findUserOrThrow(id);
        user.setStatus("ACTIVE");
        user.setUpdatedAt(LocalDateTime.now());
        return toResponseDto(appUserRepository.save(user));
    }

    private AppUser findUserOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private AppUserResponseDto toResponseDto(AppUser user) {
        return new AppUserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRole().getName(),
                user.getStatus(),
                user.getCreatedAt());
    }

    private String resolveDisplayName(AppUser user) {
        return switch (user.getRole().getName()) {
            case "COMPANY" -> companyProfileRepository.findByUserId(user.getId())
                    .map(CompanyProfile::getCompanyName)
                    .orElse(user.getEmail());
            case "PROFESSIONAL" -> professionalProfileRepository.findByUserId(user.getId())
                    .map(p -> p.getFirstName() + " " + p.getLastName())
                    .orElse(user.getEmail());
            default -> customerProfileRepository.findByUserId(user.getId())
                    .map(p -> p.getFirstName() + " " + p.getLastName())
                    .orElse(user.getEmail());
        };
    }
}
