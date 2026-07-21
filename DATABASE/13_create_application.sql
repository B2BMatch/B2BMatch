-- ================================================
-- Tabla: application
-- Descripción: Postulaciones realizadas por profesionales a las ofertas de trabajo, incluyendo propuesta, precio esperado y estado.
-- Utilizada por: notification
-- ================================================

CREATE TABLE application (

    id BIGSERIAL,

    job_offer_id BIGINT NOT NULL,

    professional_id BIGINT NOT NULL,

    proposal TEXT NOT NULL,

    expected_price NUMERIC(12,2),

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_application
        PRIMARY KEY (id),

    CONSTRAINT fk_application_job_offer
        FOREIGN KEY (job_offer_id)
        REFERENCES job_offer(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_application_professional
        FOREIGN KEY (professional_id)
        REFERENCES professional_profile(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_application
        UNIQUE(job_offer_id, professional_id),

    CONSTRAINT ck_application_status
        CHECK (
            status IN (
                'PENDING',
                'ACCEPTED',
                'REJECTED',
                'WITHDRAWN'
            )
        )

);

COMMENT ON TABLE application IS 'Applications submitted by professionals';

CREATE INDEX idx_application_job_offer
ON application(job_offer_id);

CREATE INDEX idx_application_professional
ON application(professional_id);