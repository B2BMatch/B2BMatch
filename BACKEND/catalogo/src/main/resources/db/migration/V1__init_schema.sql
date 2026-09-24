CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skill (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE professional_skill (
    professional_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_professional_skill PRIMARY KEY (professional_id, skill_id),
    CONSTRAINT fk_professional_skill_professional FOREIGN KEY (professional_id) REFERENCES perfiles.professional_profile(id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_skill_skill FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_professional_skill_skill ON professional_skill(skill_id);

CREATE TABLE professional_service (
    id BIGSERIAL,
    professional_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_professional_service PRIMARY KEY (id),
    CONSTRAINT fk_professional_service_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_professional_service_professional FOREIGN KEY (professional_id) REFERENCES perfiles.professional_profile(id) ON DELETE RESTRICT,
    CONSTRAINT ck_professional_service_status CHECK (status IN ('ACTIVE', 'PENDING', 'SUSPENDED', 'INACTIVE', 'DELETED')),
    CONSTRAINT ck_professional_service_price CHECK (price IS NULL OR price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_professional_service_professional ON professional_service(professional_id);
CREATE INDEX IF NOT EXISTS idx_professional_service_category ON professional_service(category_id);

CREATE TABLE company_service (
    id BIGSERIAL,
    company_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_company_service PRIMARY KEY (id),
    CONSTRAINT fk_company_service_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_company_service_company FOREIGN KEY (company_id) REFERENCES perfiles.company_profile(id) ON DELETE RESTRICT,
    CONSTRAINT ck_company_service_status CHECK (status IN ('ACTIVE', 'PENDING', 'SUSPENDED', 'INACTIVE', 'DELETED')),
    CONSTRAINT ck_company_service_price CHECK (price IS NULL OR price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_company_service_company ON company_service(company_id);
CREATE INDEX IF NOT EXISTS idx_company_service_category ON company_service(category_id);