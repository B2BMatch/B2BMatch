package com.b2bmatch.catalogo.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de los tests de integracion de catalogo: Postgres real via
 * Testcontainers, las migraciones Flyway de catalogo aplicadas tal cual, y las
 * tablas de perfiles que catalogo consulta, creadas por
 * fixtures/foreign-schemas.sql.
 *
 * Catalogo es dueno de sus 5 tablas, asi que aqui se monto su esquema completo
 * con sus propias migraciones: a diferencia de ofertas, no depende de que otro
 * servicio haya arrancado antes.
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

    @BeforeEach
    void limpiarDatos() {
        jdbc.execute("TRUNCATE catalogo.professional_skill, catalogo.professional_service, "
                + "catalogo.company_service, catalogo.category, catalogo.skill, "
                + "perfiles.professional_profile, perfiles.company_profile "
                + "RESTART IDENTITY CASCADE");
    }

    protected Long crearCategoria(String nombre) {
        return jdbc.queryForObject(
                "INSERT INTO catalogo.category(name) VALUES (?) RETURNING id", Long.class, nombre);
    }

    protected Long crearSkill(String nombre) {
        return jdbc.queryForObject(
                "INSERT INTO catalogo.skill(name) VALUES (?) RETURNING id", Long.class, nombre);
    }

    protected String estadoCategoria(Long id) {
        return jdbc.queryForObject("SELECT status FROM catalogo.category WHERE id = ?", String.class, id);
    }

    protected String estadoSkill(Long id) {
        return jdbc.queryForObject("SELECT status FROM catalogo.skill WHERE id = ?", String.class, id);
    }

    protected boolean estaBorradaCategoria(Long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM catalogo.category WHERE id = ?", Boolean.class, id));
    }

    protected boolean estaBorradaSkill(Long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM catalogo.skill WHERE id = ?", Boolean.class, id));
    }
}
