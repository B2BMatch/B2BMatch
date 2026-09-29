package com.b2bmatch.ofertas.support;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.b2bmatch.ofertas.dto.JobApplicationRequest;
import com.b2bmatch.ofertas.dto.JobOfferRequest;

/**
 * Base de los tests de integracion: Postgres real via Testcontainers, las
 * migraciones Flyway de ofertas aplicadas tal cual, y las tablas ajenas
 * minimas creadas por fixtures/foreign-schemas.sql.
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

    protected Long empresa;
    protected Long otraEmpresa;
    protected Long profesional;
    protected Long otroProfesional;
    protected Long admin;
    protected Long categoria;

    @BeforeEach
    void prepararDatos() {
        jdbc.execute("TRUNCATE ofertas.application_table, ofertas.quotation, ofertas.job_offer, "
                + "catalogo.professional_service, catalogo.category, "
                + "perfiles.professional_profile, perfiles.company_profile, usuarios.app_user "
                + "RESTART IDENTITY CASCADE");

        empresa = crearUsuario("empresa@test.local");
        otraEmpresa = crearUsuario("otra-empresa@test.local");
        profesional = crearUsuario("profesional@test.local");
        otroProfesional = crearUsuario("otro-profesional@test.local");
        admin = crearUsuario("admin@test.local");

        crearPerfilEmpresa(empresa);
        crearPerfilEmpresa(otraEmpresa);
        crearPerfilProfesional(profesional);
        crearPerfilProfesional(otroProfesional);

        categoria = crearCategoria("Software Development");
    }

    protected Long crearUsuario(String email) {
        return jdbc.queryForObject(
                "INSERT INTO usuarios.app_user(email) VALUES (?) RETURNING id",
                Long.class, email);
    }

    protected Long crearPerfilEmpresa(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.company_profile(user_id) VALUES (?) RETURNING id",
                Long.class, userId);
    }

    protected Long crearPerfilProfesional(Long userId) {
        return jdbc.queryForObject(
                "INSERT INTO perfiles.professional_profile(user_id) VALUES (?) RETURNING id",
                Long.class, userId);
    }

    protected Long crearCategoria(String nombre) {
        return jdbc.queryForObject(
                "INSERT INTO catalogo.category(name) VALUES (?) RETURNING id",
                Long.class, nombre);
    }

    protected Long crearOferta(Long userId, String estado) {
        return jdbc.queryForObject(
                "INSERT INTO ofertas.job_offer(user_id, category_id, title, description, budget, deadline, status) "
                        + "VALUES (?, ?, 'Oferta de prueba', 'Descripcion de prueba', 1000.00, "
                        + "CURRENT_DATE + 30, ?) RETURNING id",
                Long.class, userId, categoria, estado);
    }

    /**
     * Oferta dada de baja. El borrado ya no se marca con status='DELETED': lo
     * decide `deleted_at` y `status` conserva el estado de negocio que tuviera.
     */
    protected Long crearOfertaDadaDeBaja(Long userId, String estadoNegocio) {
        return jdbc.queryForObject(
                "INSERT INTO ofertas.job_offer(user_id, category_id, title, description, budget, deadline, "
                        + "status, deleted_at) VALUES (?, ?, 'Oferta de prueba', 'Descripcion de prueba', "
                        + "1000.00, CURRENT_DATE + 30, ?, CURRENT_TIMESTAMP) RETURNING id",
                Long.class, userId, categoria, estadoNegocio);
    }

    protected Long crearPostulacion(Long ofertaId, Long userId, String estado) {
        return jdbc.queryForObject(
                "INSERT INTO ofertas.application_table(job_offer_id, user_id, proposal, status) "
                        + "VALUES (?, ?, 'Propuesta de prueba', ?) RETURNING id",
                Long.class, ofertaId, userId, estado);
    }

    protected String estadoOferta(Long ofertaId) {
        return jdbc.queryForObject("SELECT status FROM ofertas.job_offer WHERE id = ?", String.class, ofertaId);
    }

    protected String estadoPostulacion(Long postulacionId) {
        return jdbc.queryForObject("SELECT status FROM ofertas.application_table WHERE id = ?",
                String.class, postulacionId);
    }

    protected JobOfferRequest solicitudOferta() {
        JobOfferRequest r = new JobOfferRequest();
        r.setCategoryId(categoria);
        r.setTitle("Oferta nueva");
        r.setDescription("Descripcion de la oferta nueva");
        r.setBudget(new BigDecimal("1500.00"));
        r.setDeadline(LocalDate.now().plusDays(30));
        return r;
    }

    protected JobApplicationRequest solicitudPostulacion(Long ofertaId) {
        JobApplicationRequest r = new JobApplicationRequest();
        r.setJobOfferId(ofertaId);
        r.setProposal("Mi propuesta");
        r.setExpectedPrice(new BigDecimal("900.00"));
        return r;
    }
}
