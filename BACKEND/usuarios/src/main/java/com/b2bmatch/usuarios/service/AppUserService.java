package com.b2bmatch.usuarios.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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

    /** Fallos por par (IP, email) antes de frenar ese par concreto. */
    private static final int MAX_INTENTOS_POR_CUENTA = 5;
    /** Fallos desde una misma IP. Mas alto que el de cuenta porque una oficina
     *  entera detras de una NAT comparte IP y hay gente que se equivoca. */
    private static final int MAX_INTENTOS_POR_IP = 20;
    private static final long DURACION_BLOQUEO_MINUTOS = 15;
    private static final int MAX_INTENTOS_SEGUIDOS = 10_000;

    /**
     * Los unicos roles que uno puede elegir al registrarse. Lo que no este aqui se
     * rechaza, en vez de enumerar los que no: con una lista de prohibidos, el
     * proximo rol privilegiado que se anada a la semilla queda registrable desde
     * internet sin tocar una linea de Java, y el agujero aparece en el momento
     * menos conveniente. Aqui ocurre al reves, anadir un rol nuevo al sistema no
     * lo hace registrable y hay que decidirlo a mano.
     *
     * ADMIN entra por `registerAdmin`, que exige la clave de bootstrap.
     */
    private static final Set<String> ROLES_DE_AUTOSERVICIO =
            Set.of("CUSTOMER", "PROFESSIONAL", "COMPANY");

    @Value("${app.admin-bootstrap-key:}")
    private String adminBootstrapKey;

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private final ContadorIntentos intentosDeLogin = new ContadorIntentos(
            MAX_INTENTOS_POR_CUENTA,
            MAX_INTENTOS_POR_IP,
            Duration.ofMinutes(DURACION_BLOQUEO_MINUTOS),
            MAX_INTENTOS_SEGUIDOS,
            Clock.systemDefaultZone());

    @PostConstruct
    void init() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public AppUserResponseDto register(AppUserRegisterRequestDto request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        checkNewEmail(normalizedEmail);
        checkPasswordPattern(request.getPassword());

        String roleName = request.getRoleName().toUpperCase().trim();
        if (!ROLES_DE_AUTOSERVICIO.contains(roleName)) {
            // Se responde igual tanto si el rol existe pero no es de autoservicio
            // como si no existe, para no confirmar desde fuera que nombres hay en
            // la tabla. Y se corta aqui, antes de la consulta.
            throw new IllegalArgumentException("Ese rol no se puede elegir en el registro");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Ese rol no está disponible"));

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

    /**
     * Un unico mensaje para todo lo que no es un acierto. Tres casos distintos
     * llegaban con tres textos: cuenta inexistente, cuenta suspendida y cuenta
     * bloqueada por intentos. Con eso el login servia para enumerar quien tiene
     * cuenta, y bastaba con cinco intentos fallidos y un sexto para leer la
     * respuesta y saber si el email estaba registrado.
     *
     * El precio es que un usuario al que le ha caducado la ventana de bloqueo ve
     * "contraseña incorrecta" en vez de "espera 15 minutos". Se acepta: la
     * alternativa es confirmar la existencia de la cuenta a quien la ataca.
     */
    private static final String CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";

    public LoginResponseDto login(LoginRequestDto request, String origen) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        String ip = normalizarOrigen(origen);

        // Dos contadores, no uno. El de por IP acota la enumeracion desde una sola
        // maquina. El de cuenta combina IP y email, y esa combinacion es lo que
        // quita el bloqueo como arma: con el contador por email solo, cinco
        // intentos bloqueaban la cuenta de una victima desde cualquier sitio y
        // durante quince minutos, o sea que el endpoint servia tanto para tumbar
        // cuentas como para adivinarlas. Con la IP en la clave, atacar a alguien
        // desde una maquina solo te frena a ti.
        String claveCuenta = "cuenta|" + ip + "|" + normalizedEmail;
        String claveIp = "ip|" + ip;

        if (intentosDeLogin.bloqueado(claveCuenta) || intentosDeLogin.bloqueado(claveIp)) {
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }

        // Filtra por `deleted_at` y no solo por `status`: dar de baja no pisa el
        // estado, asi que un usuario borrado sigue en ACTIVE y entraria con el
        // token que le emite este mismo metodo, otra vez y otra vez.
        AppUser appUser = appUserRepository.findVivaByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException(CREDENCIALES_INVALIDAS));

        // Mismo mensaje que el de una cuenta que no existe. Antes decia "esta
        // cuenta no esta activa", y con eso un POST bastaba para confirmar que el
        // email estaba registrado y ademas suspendido.
        if (!"ACTIVE".equals(appUser.getStatus())) {
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }

        if (!passwordEncoder.matches(request.getPassword(), appUser.getPasswordHash())) {
            intentosDeLogin.falloDeCuenta(claveCuenta);
            intentosDeLogin.falloDeIp(claveIp);
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }

        intentosDeLogin.exito(claveCuenta, claveIp);

        String token = jwtService.generateToken(appUser.getEmail(), appUser.getRole().getName(), appUser.getId());
        return new LoginResponseDto(appUser.getId(), appUser.getName(), token, appUser.getEmail(), appUser.getRole().getName());
    }

    private String normalizarOrigen(String origen) {
        if (origen == null || origen.isBlank()) {
            return "desconocida";
        }
        return origen.trim();
    }

    // Nadie lo llama: no hay endpoint que llegue aqui. Se le pone el finder con rol
    // igual que a los vivos para no dejar un metodo que revienta en cuanto alguien
    // lo cablee, pero sin test propio, porque un test de codigo muerto estorba mas
    // que ayuda.
    public AppUserResponseDto update(Long id, AppUserUpdateRequestDto request) {
        AppUser appUser = appUserRepository.findPorIdConRol(id)
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
                ? appUserRepository.findTodosConRol()
                : appUserRepository.findVivosConRol();
        return users.stream()
                .map(this::toDto)
                .toList();
    }

    public AppUserResponseDto findById(Long id) {
        AppUser appUser = appUserRepository.findPorIdConRol(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
        return toDto(appUser);
    }

    public List<AppUserResponseDto> findByRole(String roleName) {
        return appUserRepository.findPorRolConRol(roleName.toUpperCase()).stream()
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

    // Transaccional, igual que `delete` y `reactivate`, y por dos motivos. Uno: el
    // DTO se arma sobre lo que devuelve `save()`, y el resultado de un `merge` sale
    // con un proxy de rol nuevo que sin sesion abierta no se puede leer. Un
    // `JOIN FETCH` en la carga no arregla eso, porque la entidad que se lee despues
    // no es la que se cargo. Dos: es un leer-modificar-escribir, y sin transaccion
    // dos cambios de estado concurrentes se pisan sin que nadie lo note.
    @Transactional
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