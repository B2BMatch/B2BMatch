package com.b2bmatch.resenias.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.b2bmatch.resenias.model.Review;

/**
 * Base de los tests de integracion de resenias: Postgres real via
 * Testcontainers y las migraciones Flyway de resenias aplicadas tal cual.
 *
 * resenias es dueno de su unica tabla y sus migraciones (V1 y V901) declaran
 * las columnas de borrado de ella, asi que aqui el esquema se monta completo
 * sin depender del orden de arranque de otros servicios. Lo que si es ajeno, y
 * por eso lo crea fixtures/foreign-schemas.sql, son las cinco tablas que
 * ReviewService consulta por SQL crudo y las dos FK de review.
 *
 * El contenedor se arranca una sola vez por JVM (patron singleton) y se
 * reutiliza entre clases de test.
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

    /** Autora de las reseñas: rol CUSTOMER. */
    protected Long cliente;
    /** Otra autora, para las pruebas de propiedad. */
    protected Long otraClienta;
    protected Long empresa;
    /** Usuario dueno del perfil profesional. */
    protected Long profesional;
    protected Long otroProfesional;
    protected Long admin;
    /**
     * Id de `perfiles.professional_profile`, que es lo que guarda
     * `review.professional_id`. Se mantiene aparte del usuario dueno porque
     * resenias habla siempre con el id del perfil, no con el del usuario.
     */
    protected Long perfilProfesional;
    protected Long perfilOtroProfesional;

    @BeforeEach
    void prepararDatos() {
        jdbc.execute("TRUNCATE resenias.review, ofertas.quotation, ofertas.application_table, "
                + "ofertas.job_offer, catalogo.professional_service, "
                + "perfiles.professional_profile, usuarios.app_user RESTART IDENTITY CASCADE");

        cliente = crearUsuario("cliente@test.local");
        otraClienta = crearUsuario("otra-cliente@test.local");
        empresa = crearUsuario("empresa@test.local");
        profesional = crearUsuario("profesional@test.local");
        otroProfesional = crearUsuario("otro-profesional@test.local");
        admin = crearUsuario("admin@test.local");

        perfilProfesional = crearPerfilProfesional(profesional);
        perfilOtroProfesional = crearPerfilProfesional(otroProfesional);
    }
    protected Long crearUsuario(String email) {
        return jdbc.queryForObject("INSERT INTO usuarios.app_user(email) VALUES (?) RETURNING id",
                Long.class, email);
    }

    protected Long crearPerfilProfesional(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.professional_profile(user_id) VALUES (?) RETURNING id",
                Long.class, userId);
    }

    /**
     * Perfil profesional dado de baja. El borrado vive en `deleted_at`, que es
     * lo que `createReview` comprueba antes de aceptar una reseña.
     */
    protected Long crearPerfilProfesionalDadoDeBaja(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.professional_profile(user_id, deleted_at) "
                        + "VALUES (?, CURRENT_TIMESTAMP) RETURNING id",
                Long.class, userId);
    }

    protected Long crearResenia(Long userId, Long professionalId, int rating, String comment) {
        return jdbc.queryForObject(
                "INSERT INTO resenias.review(user_id, professional_id, rating, comment, created_at) "
                        + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id",
                Long.class, userId, professionalId, rating, comment);
    }

    /**
     * Reseña sin persistir, como la que arma el controller a partir del cuerpo
     * de la peticion. `userId` no se fija: el servicio lo toma del token.
     */
    protected Review resena(Long professionalId, Integer rating, String comment) {
        return Review.builder()
                .professionalId(professionalId)
                .rating(rating)
                .comment(comment)
                .build();
    }

    /** Postulacion ACCEPTED del profesional a una oferta de la empresa. */
    protected Long crearPostulacionAceptada(Long empresaId, Long profesionalId) {
        Long ofertaId = jdbc.queryForObject(
                "INSERT INTO ofertas.job_offer(user_id) VALUES (?) RETURNING id", Long.class, empresaId);
        return jdbc.queryForObject(
                "INSERT INTO ofertas.application_table(job_offer_id, user_id, status) "
                        + "VALUES (?, ?, 'ACCEPTED') RETURNING id", Long.class, ofertaId, profesionalId);
    }

    /** Cotizacion ACCEPTED del cliente o de la empresa sobre un servicio del profesional. */
    protected Long crearCotizacionAceptada(Long userId, Long professionalId) {
        Long servicioId = jdbc.queryForObject(
                "INSERT INTO catalogo.professional_service(professional_id) VALUES (?) RETURNING id",
                Long.class, professionalId);
        return jdbc.queryForObject(
                "INSERT INTO ofertas.quotation(service_id, user_id, status) "
                        + "VALUES (?, ?, 'ACCEPTED') RETURNING id", Long.class, servicioId, userId);
    }

    protected boolean estaBorrada(Long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM resenias.review WHERE id = ?", Boolean.class, id));
    }

    /** La fila sigue existiendo: el borrado es logico, no un DELETE. */
    protected boolean existeFila(Long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT count(*) > 0 FROM resenias.review WHERE id = ?", Boolean.class, id));
    }
}
