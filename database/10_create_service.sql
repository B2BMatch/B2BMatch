-- ================================================
-- Tabla: service
-- Descripción: Servicios ofrecidos por profesionales dentro del marketplace, clasificados por categoría y con información de precio, descripción y estado.
-- Utilizada por: quotation
-- ================================================

CREATE TABLE service (

    id BIGSERIAL,

    professional_id BIGINT NOT NULL,

    category_id BIGINT NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT NOT NULL,

    price NUMERIC(10,2) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

CONSTRAINT pk_service
PRIMARY KEY (id),

CONSTRAINT fk_service_professional
foreign KEY (professional_id)
references professional_profile(id),

CONSTRAINT fk_service_category
foreign key (category_id)
references category(id),

CONSTRAINT ck_service_status
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

COMMENT ON TABLE service IS 'Professional services offered on the platform';

CREATE INDEX idx_service_professional
ON service(professional_id);

CREATE INDEX idx_service_category
ON service(category_id);