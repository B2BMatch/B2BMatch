-- ================================================
-- Tabla: job_offer
-- Descripción: Ofertas de trabajo o proyectos publicadas por empresas para contratar profesionales a través de la plataforma.
-- Utilizada por: application
-- ================================================

CREATE TABLE job_offer (

    id BIGSERIAL,

    company_id BIGINT NOT NULL,

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

    CONSTRAINT fk_job_offer_company
        FOREIGN KEY (company_id)
        REFERENCES company_profile(id),

    CONSTRAINT fk_job_offer_category
        FOREIGN KEY (category_id)
        REFERENCES category(id),

    CONSTRAINT ck_job_offer_status
        CHECK (
            status IN (
                'ACTIVE',
                'PENDING',
                'SUSPENDED',
                'INACTIVE',
                'DELETED'
            )
        )

);

COMMENT ON TABLE job_offer IS 'Job offers published by companies';

CREATE INDEX idx_job_offer_company
ON job_offer(company_id);

CREATE INDEX idx_job_offer_category
ON job_offer(category_id);