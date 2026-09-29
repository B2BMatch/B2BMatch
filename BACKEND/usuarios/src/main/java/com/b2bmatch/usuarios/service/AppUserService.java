package com.b2bmatch.usuarios.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import com.b2bmatch.usuarios.config.JwtService;
import com.b2bmatch.usuarios.dto.AppUserAdminRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserResponseDto;
import com.b2bmatch.usuarios.dto.AppUserUpdateRequestDto;
import com.b2bmatch.usuarios.dto.LoginRequestDto;
import com.b2bmatch.usuarios.dto.LoginResponseDto;
import com.b2bmatch.usuarios.exception.ForbiddenException;
import com.b2bmatch.usuarios.model.AppUser;
import com.b2bmatch.usuarios.model.Role;
import com.b2bmatch.usuarios.repository.AppUserRepository;
import com.b2bmatch.usuarios.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    private static final int MAX_LOGIN_TRACKED_EMAILS = 10_000;

    @Value("${app.admin-bootstrap-key:}")
    private String adminBootstrapKey;

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private final ConcurrentHashMap<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public AppUserResponseDto register(AppUserRegisterRequestDto request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        checkNewEmail(normalizedEmail);
        checkPasswordPattern(request.getPassword());

        Role role = roleRepository.findByName(request.getRoleName().toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe"));

        if ("ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException("No está permitido registrarse como administrador");
        }

        return createUser(normalizedEmail, request.getName().trim(), request.getPassword(), role);
    }

    public AppUserResponseDto registerAdmin(AppUserAdminRegisterRequestDto request, String bootstrapKey) {
        if (!isValidBootstrapKey(bootstrapKey)) {
            throw new ForbiddenException("Clave de bootstrap inválida");
        }

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        checkNewEmail(normalizedEmail);
        checkPasswordPattern(request.getPassword());

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalArgumentException("El rol ADMIN no existe"));

        return createUser(normalizedEmail, request.getName().trim(), request.getPassword(), adminRole);
    }

    private AppUserResponseDto createUser(String normalizedEmail, String name, String password, Role role) {
        AppUser appUser = new AppUser();
        appUser.setEmail(normalizedEmail);
        appUser.setName(name);
        appUser.setPasswordHash(passwordEncoder.encode(password));
        appUser.setRole(role);
        appUser.setStatus("ACTIVE");
        appUser.setCreatedAt(LocalDateTime.now());

        return toDto(appUserRepository.save(appUser));
    }

    private void checkNewEmail(String normalizedEmail) {
        if (appUserRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
    }

    private void checkPasswordPattern(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número");
        }
    }

    private boolean isValidBootstrapKey(String presentedKey) {
        if (presentedKey == null || adminBootstrapKey == null || adminBootstrapKey.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(
                adminBootstrapKey.getBytes(StandardCharsets.UTF_8),
                presentedKey.getBytes(StandardCharsets.UTF_8));
    }

    public LoginResponseDto login(LoginRequestDto request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        pruneLoginAttempts();

        LoginAttempt attempt = loginAttempts.get(normalizedEmail);
        if (attempt != null && attempt.lockedUntil != null) {
            if (LocalDateTime.now().isBefore(attempt.lockedUntil)) {
                throw new IllegalArgumentException(
                        "Demasiados intentos fallidos. Cuenta bloqueada temporalmente, intente nuevamente más tarde");
            }
            loginAttempts.remove(normalizedEmail);
        }

        // Filtra por `deleted_at` y no solo por `status`: dar de baja no pisa el
        // estado, asi que un usuario borrado sigue en ACTIVE y entraria con el
        // token que le emite este mismo metodo, otra vez y otra vez. El mensaje
        // es el de una cuenta inexistente a proposito: distinguirlos confirmaria
        // que el email existe y esta dado de baja.
        AppUser appUser = appUserRepository.findVivaByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));

        if (!"ACTIVE".equals(appUser.getStatus())) {
            throw new IllegalArgumentException("Esta cuenta no está activa");
        }

        if (!passwordEncoder.matches(request.getPassword(), appUser.getPasswordHash())) {
            registerLoginFailure(normalizedEmail);
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }

        loginAttempts.remove(normalizedEmail);

        String token = jwtService.generateToken(appUser.getEmail(), appUser.getRole().getName(), appUser.getId());
        return new LoginResponseDto(appUser.getId(), appUser.getName(), token, appUser.getEmail(), appUser.getRole().getName());
    }

    private void registerLoginFailure(String email) {
        pruneLoginAttempts();
        LoginAttempt attempt = loginAttempts.computeIfAbsent(email, k -> new LoginAttempt());
        attempt.count++;
        if (attempt.count >= MAX_LOGIN_ATTEMPTS) {
            attempt.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            attempt.count = 0;
        }
    }

    private void pruneLoginAttempts() {
        if (loginAttempts.size() <= MAX_LOGIN_TRACKED_EMAILS) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        loginAttempts.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            return attempt.lockedUntil == null || attempt.lockedUntil.isBefore(now);
        });
    }

    private static final class LoginAttempt {
        int count;
        LocalDateTime lockedUntil;
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
        appUser.setName(request.getName().trim());
        appUser.setRole(role);
        appUser.setUpdatedAt(LocalDateTime.now());

        return toDto(appUserRepository.save(appUser));
    }

    public List<AppUserResponseDto> findAll() {
        return findAll(false);
    }

    public List<AppUserResponseDto> findAll(boolean includeDeleted) {
        List<AppUser> users = includeDeleted
                ? appUserRepository.findAll()
                : appUserRepository.findByDeletedAtIsNull();
        return users.stream()
                .map(this::toDto)
                .toList();
    }

    public AppUserResponseDto findById(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
        return toDto(appUser);
    }

    public List<AppUserResponseDto> findByRole(String roleName) {
        return appUserRepository.findByRole_NameAndDeletedAtIsNull(roleName.toUpperCase()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (appUser.getDeletedAt() != null) {
            throw new IllegalArgumentException("El usuario ya está eliminado");
        }

        // `status` es estado de negocio: no se pisa al dar de baja. El borrado
        // vive solo en deleted_at, y por eso la restauracion no necesita adivinar
        // el estado previo.
        appUser.setDeletedAt(LocalDateTime.now());
        appUser.setUpdatedAt(LocalDateTime.now());
        appUserRepository.save(appUser);

        cascadeSoftDeleteDependentData(id);
    }

    private void cascadeSoftDeleteDependentData(Long userId) {
        softDelete("perfiles.company_profile", "user_id = ?", userId);
        softDelete("perfiles.professional_profile", "user_id = ?", userId);
        softDelete("perfiles.customer_profile", "user_id = ?", userId);
        softDelete("ofertas.job_offer", "user_id = ?", userId);
        softDelete("catalogo.professional_service", "professional_id IN "
                + "(SELECT id FROM perfiles.professional_profile WHERE user_id = ?)", userId);
        softDelete("catalogo.company_service", "company_id IN "
                + "(SELECT id FROM perfiles.company_profile WHERE user_id = ?)", userId);

        jdbcTemplate.update("DELETE FROM notificaciones.notification WHERE user_id=?", userId);
        jdbcTemplate.update("DELETE FROM resenias.review WHERE user_id=?", userId);
    }

    private void softDelete(String table, String condition, Long userId) {
        jdbcTemplate.update("UPDATE " + table
                + " SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP"
                + " WHERE deleted_at IS NULL AND " + condition, userId);
    }

    private void cascadeRestoreDependentData(Long userId) {
        restore("perfiles.company_profile", "user_id = ?", userId);
        restore("perfiles.professional_profile", "user_id = ?", userId);
        restore("perfiles.customer_profile", "user_id = ?", userId);
        restore("ofertas.job_offer", "user_id = ?", userId);
        restore("catalogo.professional_service", "professional_id IN "
                + "(SELECT id FROM perfiles.professional_profile WHERE user_id = ?)", userId);
        restore("catalogo.company_service", "company_id IN "
                + "(SELECT id FROM perfiles.company_profile WHERE user_id = ?)", userId);
    }

    private void restore(String table, String condition, Long userId) {
        jdbcTemplate.update("UPDATE " + table
                + " SET deleted_at = NULL, updated_at = CURRENT_TIMESTAMP"
                + " WHERE deleted_at IS NOT NULL AND " + condition, userId);
    }

    public AppUserResponseDto updateStatus(Long id, String status) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (appUser.getDeletedAt() != null) {
            throw new IllegalArgumentException("No se puede cambiar el estado de una cuenta eliminada");
        }

        String newStatus = status.toUpperCase();
        if (!List.of("ACTIVE", "INACTIVE", "SUSPENDED").contains(newStatus)) {
            throw new IllegalArgumentException("Estado no válido");
        }

        appUser.setStatus(newStatus);
        appUser.setUpdatedAt(LocalDateTime.now());
        return toDto(appUserRepository.save(appUser));
    }

    @Transactional
    public AppUserResponseDto reactivate(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (appUser.getDeletedAt() == null) {
            throw new IllegalArgumentException("El usuario no está eliminado, no se puede reactivar");
        }

        appUser.setDeletedAt(null);
        appUser.setUpdatedAt(LocalDateTime.now());
        AppUserResponseDto restored = toDto(appUserRepository.save(appUser));

        cascadeRestoreDependentData(id);
        return restored;
    }

    private AppUserResponseDto toDto(AppUser appUser) {
        return new AppUserResponseDto(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getName(),
                appUser.getStatus(),
                appUser.getRole().getName(),
                appUser.getCreatedAt(),
                appUser.getUpdatedAt(),
                appUser.getDeletedAt()
        );
    }
}