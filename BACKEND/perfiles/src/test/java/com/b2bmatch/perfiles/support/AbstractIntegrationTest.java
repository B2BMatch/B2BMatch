package com.b2bmatch.perfiles.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de los tests de integracion de perfiles: Postgres real via
 * Testcontainers y las migraciones Flyway de perfiles aplicadas tal cual.
 *
 * perfiles es dueno de sus tres tablas y su unica dependencia ajena es la FK a
 * `usuarios.app_user`, que crea fixtures/foreign-schemas.sql. Su V901 declara
 * las columnas de borrado de sus propias tablas, asi que aqui el esquema se
 * monta completo sin depender del orden de arranque de otros servicios.
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

    protected Long usuario;
    protected Long otroUsuario;
    protected Long admin;

    @BeforeEach
    void prepararDatos() {
        jdbc.execute("TRUNCATE perfiles.customer_profile, perfiles.professional_profile, "
                + "perfiles.company_profile, usuarios.app_user RESTART IDENTITY CASCADE");

        usuario = crearUsuario("profesional@test.local");
        otroUsuario = crearUsuario("otro@test.local");
        admin = crearUsuario("admin@test.local");
    }

    protected Long crearUsuario(String email) {
        return jdbc.queryForObject("INSERT INTO usuarios.app_user(email) VALUES (?) RETURNING id",
                Long.class, email);
    }

    protected Long crearPerfilProfesional(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.professional_profile(user_id, first_name, last_name) "
                        + "VALUES (?, 'Ada', 'Lovelace') RETURNING id",
                Long.class, userId);
    }

    protected Long crearPerfilEmpresa(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.company_profile(user_id, company_name, tax_id) "
                        + "VALUES (?, 'Acme', 'TAX-' || ?) RETURNING id",
                Long.class, userId, userId);
    }

    protected Long crearPerfilCliente(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.customer_profile(user_id, first_name, last_name) "
                        + "VALUES (?, 'Grace', 'Hopper') RETURNING id",
                Long.class, userId);
    }

    protected String estado(String tabla, Long id) {
        return jdbc.queryForObject("SELECT status FROM perfiles." + tabla + " WHERE id = ?",
                String.class, id);
    }

    protected boolean estaBorrada(String tabla, Long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM perfiles." + tabla + " WHERE id = ?",
                Boolean.class, id));
    }
}
