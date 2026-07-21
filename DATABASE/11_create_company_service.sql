-- ================================================
-- Tabla: company_service
-- Descripción: Servicios ofrecidos por empresas registradas en la plataforma, clasificados por categoría e información comercial.
-- Utilizada por: review
-- ================================================

CREATE TABLE company_service (

    id BIGSERIAL,

    company_id BIGINT NOT NULL,

    category_id BIGINT NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT NOT NULL,

    price NUMERIC(10,2) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_company_service
        PRIMARY KEY (id),

    CONSTRAINT fk_company_service_company
        FOREIGN KEY (company_id)
        REFERENCES company_profile(id),

    CONSTRAINT fk_company_service_category
        FOREIGN KEY (category_id)
        REFERENCES category(id),

    CONSTRAINT ck_company_service_status
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

COMMENT ON TABLE company_service IS 'Services published by companies';

CREATE INDEX idx_company_service_company
ON company_service(company_id);

CREATE INDEX idx_company_service_category
ON company_service(category_id);