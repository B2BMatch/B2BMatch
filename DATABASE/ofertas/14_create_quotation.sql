SET search_path TO ofertas, public;

/*
====================================================
 Project : b2bmatch
 File    : 14_create_quotation.sql
 Author  : Team b2bmatch

 NOTA DE CONSISTENCIA: ver nota en 12_create_job_offer.sql.
 Espejo fiel de V1__init_schema.sql (ofertas): la columna
 de usuario es user_id (FK a usuarios.app_user), no
 customer_id.
====================================================
*/

-- Tabla: quotation
-- Cotizaciones solicitadas por clientes/empresas sobre servicios de profesionales.
CREATE TABLE quotation (

    id BIGSERIAL,

    service_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    message TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_quotation
        PRIMARY KEY (id),

    CONSTRAINT fk_quotation_service
        FOREIGN KEY (service_id) REFERENCES catalogo.professional_service(id) ON DELETE RESTRICT,

    CONSTRAINT fk_quotation_user
        FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id)

);

COMMENT ON TABLE quotation IS 'Quotations requested on professional services';

CREATE INDEX IF NOT EXISTS idx_quotation_service_id ON quotation(service_id);
CREATE INDEX IF NOT EXISTS idx_quotation_user_id ON quotation(user_id);