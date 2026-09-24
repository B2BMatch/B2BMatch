SET search_path TO ofertas, public;

/*
====================================================
 Project : b2bmatch
 File    : 12_create_job_offer.sql
 Author  : Team b2bmatch

 NOTA DE CONSISTENCIA: estas DDL son REFERENCIA HISTORICA.
 La base de datos en ejecucion se crea con las migraciones
 Flyway de BACKEND/<servicio>/src/main/resources/db/migration
 (V1__init_schema.sql / V2__seed_data.sql), que son la fuente
 de verdad. Este archivo busca reflejarlas fielmente.
====================================================
*/

-- Tabla: job_offer
-- Ofertas publicadas por empresas para contratar profesionales.
CREATE TABLE job_offer (

    id BIGSERIAL,

    user_id BIGINT NOT NULL,

    category_id BIGINT NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT NOT NULL,

    budget NUMERIC(12,2) NOT NULL,

    deadline DATE NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_job_offer
        PRIMARY KEY (id),

    CONSTRAINT fk_job_offer_user
        FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),

    CONSTRAINT fk_job_offer_category
        FOREIGN KEY (category_id) REFERENCES catalogo.category(id) ON DELETE RESTRICT,

    CONSTRAINT ck_job_offer_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'DELETED', 'CLOSED', 'EXPIRED')),

    CONSTRAINT ck_job_offer_budget
        CHECK (budget > 0),

    CONSTRAINT ck_job_offer_deadline
        CHECK (deadline > CURRENT_DATE)

);

COMMENT ON TABLE job_offer IS 'Job offers published by companies';

CREATE INDEX IF NOT EXISTS idx_job_offer_user ON job_offer(user_id);
CREATE INDEX IF NOT EXISTS idx_job_offer_category ON job_offer(category_id);