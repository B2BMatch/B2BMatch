package com.b2bmatch.usuarios.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de los tests de integracion de usuarios: Postgres real via Testcontainers
 * y las migraciones Flyway de usuarios aplicadas tal cual.
 *
 * usuarios es dueno de app_user y de la cascada de borrado, asi que su fixture
 * crea stubs de las 8 tablas de 5 schemas que el cascade toca. Ver
 * fixtures/foreign-schemas.sql: esa lista es la documentacion del
 * acoplamiento real.
 *
 * El contenedor se arranca una sola vez por JVM (patron singleton).
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("b2bmatch")
            .withInitScript("fixtures/foreign-schemas.sql");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Long usuario;
    protected Long profesional;
    protected Long empresa;
    protected Long rol;

    @BeforeEach
    void prepararDatos() {
        jdbc.execute("TRUNCATE notificaciones.notification, resenias.review, "
                + "catalogo.professional_service, catalogo.company_service, ofertas.job_offer, "
                + "perfiles.customer_profile, perfiles.professional_profile, perfiles.company_profile, "
                + "usuarios.app_user RESTART IDENTITY CASCADE");

        // V2 ya siembra los roles, asi que se reutiliza el existente en vez de
        // insertar uno nuevo (role.name es UNIQUE).
        rol = jdbc.queryForObject(
                "SELECT id FROM usuarios.role WHERE name = 'PROFESSIONAL'", Long.class);
        // El cascade resuelve los servicios por subconsulta a perfiles, asi que
        // hace falta un profesional con sus perfiles y servicios, y una empresa
        // con los suyos.
        empresa = crearUsuario("empresa@test.local", rol);
        profesional = crearUsuario("profesional@test.local", rol);
        usuario = crearUsuario("simple@test.local", rol);

        perfilEmpresa(empresa);
        perfilProfesional(profesional);
        perfilCliente(usuario);
    }

    protected Long crearUsuario(String email, Long rol) {
        return jdbc.queryForObject(
                "INSERT INTO usuarios.app_user(role_id, email, password_hash) VALUES (?, ?, 'x') RETURNING id",
                Long.class, rol, email);
    }

    /**
     * Usuario con una clave de verdad, para los tests que pasan por el login. El
     * resto del fixture usa 'x' porque a la cascada no le importa la clave, y
     * meter un BCrypt real en todos haria el setup mas lento sin ganar nada.
     */
    protected Long crearUsuarioConClave(String email, String clave) {
        return jdbc.queryForObject(
                "INSERT INTO usuarios.app_user(role_id, email, password_hash) VALUES (?, ?, ?) RETURNING id",
                Long.class, rol, email, passwordEncoder.encode(clave));
    }

    protected Long perfilEmpresa(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.company_profile(user_id) VALUES (?) RETURNING id", Long.class, userId);
    }

    protected Long perfilProfesional(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.professional_profile(user_id) VALUES (?) RETURNING id", Long.class, userId);
    }

    protected Long perfilCliente(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.customer_profile(user_id) VALUES (?) RETURNING id", Long.class, userId);
    }

    protected void crearOferta(Long userId) {
        jdbc.update("INSERT INTO ofertas.job_offer(user_id) VALUES (?)", userId);
    }

    protected void crearServicioProfesional(Long professionalId) {
        jdbc.update("INSERT INTO catalogo.professional_service(professional_id) VALUES (?)", professionalId);
    }

    protected void crearServicioEmpresa(Long companyId) {
        jdbc.update("INSERT INTO catalogo.company_service(company_id) VALUES (?)", companyId);
    }

    protected void crearNotificacion(Long userId) {
        jdbc.update("INSERT INTO notificaciones.notification(user_id) VALUES (?)", userId);
    }

    protected void crearResenia(Long userId) {
        jdbc.update("INSERT INTO resenias.review(user_id) VALUES (?)", userId);
    }

    /** Filas dadas de baja de una tabla, por id. */
    protected long dadasDeBaja(String tabla) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM " + tabla + " WHERE deleted_at IS NOT NULL", Long.class);
    }

    protected long vivas(String tabla) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM " + tabla + " WHERE deleted_at IS NULL", Long.class);
    }

    protected String estado(String tabla, Long id) {
        return jdbc.queryForObject("SELECT status FROM " + tabla + " WHERE id = ?", String.class, id);
    }
}
