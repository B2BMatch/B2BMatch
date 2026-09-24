SET search_path TO ofertas, public;

/*
====================================================
 Project : b2bmatch
 File    : 13_create_application.sql
 Author  : Team b2bmatch

 NOTA DE CONSISTENCIA: ver nota en 12_create_job_offer.sql.
 Espejo fiel de V1__init_schema.sql (ofertas): la columna
 de usuario es user_id (FK a usuarios.app_user), no
 professional_id.
====================================================
*/

-- Tabla: application_table
-- Postulaciones de profesionales a ofertas (proposal, precio esperado, estado).
CREATE TABLE application_table (

    id BIGSERIAL,

    job_offer_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    proposal TEXT NOT NULL,

    expected_price NUMERIC(12,2),

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_application
        PRIMARY KEY (id),

    CONSTRAINT fk_application_job_offer
        FOREIGN KEY (job_offer_id) REFERENCES job_offer(id) ON DELETE CASCADE,

    CONSTRAINT fk_application_user
        FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),

    CONSTRAINT uk_application
        UNIQUE (job_offer_id, user_id),

    CONSTRAINT ck_application_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))

);

COMMENT ON TABLE application_table IS 'Applications submitted by professionals';

CREATE INDEX IF NOT EXISTS idx_application_job_offer ON application_table(job_offer_id);
CREATE INDEX IF NOT EXISTS idx_application_user ON application_table(user_id);