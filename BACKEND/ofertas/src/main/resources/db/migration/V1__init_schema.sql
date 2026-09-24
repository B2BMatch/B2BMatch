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
    CONSTRAINT pk_job_offer PRIMARY KEY (id),
    CONSTRAINT fk_job_offer_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),
    CONSTRAINT fk_job_offer_category FOREIGN KEY (category_id) REFERENCES catalogo.category(id) ON DELETE RESTRICT,
    CONSTRAINT ck_job_offer_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'DELETED', 'CLOSED', 'EXPIRED')),
    CONSTRAINT ck_job_offer_budget CHECK (budget > 0),
    CONSTRAINT ck_job_offer_deadline CHECK (deadline > CURRENT_DATE)
);

CREATE INDEX IF NOT EXISTS idx_job_offer_user ON job_offer(user_id);
CREATE INDEX IF NOT EXISTS idx_job_offer_category ON job_offer(category_id);

CREATE TABLE application_table (
    id BIGSERIAL,
    job_offer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    proposal TEXT NOT NULL,
    expected_price NUMERIC(12,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_application PRIMARY KEY (id),
    CONSTRAINT fk_application_job_offer FOREIGN KEY (job_offer_id) REFERENCES job_offer(id) ON DELETE CASCADE,
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),
    CONSTRAINT uk_application UNIQUE (job_offer_id, user_id),
    CONSTRAINT ck_application_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_application_job_offer ON application_table(job_offer_id);
CREATE INDEX IF NOT EXISTS idx_application_user ON application_table(user_id);

CREATE TABLE quotation (
    id BIGSERIAL,
    service_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_quotation PRIMARY KEY (id),
    CONSTRAINT fk_quotation_service FOREIGN KEY (service_id) REFERENCES catalogo.professional_service(id) ON DELETE RESTRICT,
    CONSTRAINT fk_quotation_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id)
);

CREATE INDEX IF NOT EXISTS idx_quotation_service_id ON quotation(service_id);
CREATE INDEX IF NOT EXISTS idx_quotation_user_id ON quotation(user_id);