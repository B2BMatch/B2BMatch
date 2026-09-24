CREATE TABLE customer_profile (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_customer_profile PRIMARY KEY (id),
    CONSTRAINT uk_customer_profile_user UNIQUE (user_id),
    CONSTRAINT fk_customer_profile_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),
    CONSTRAINT ck_customer_profile_status CHECK (status IN ('ACTIVE', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_customer_profile_last_name ON customer_profile(last_name);

CREATE TABLE professional_profile (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    biography TEXT,
    experience_years INTEGER DEFAULT 0,
    hourly_rate NUMERIC(10,2),
    portfolio_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_professional_profile PRIMARY KEY (id),
    CONSTRAINT uk_professional_profile_user UNIQUE (user_id),
    CONSTRAINT fk_professional_profile_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),
    CONSTRAINT ck_professional_experience CHECK (experience_years >= 0),
    CONSTRAINT ck_professional_hourly_rate CHECK (hourly_rate IS NULL OR hourly_rate >= 0),
    CONSTRAINT ck_professional_profile_status CHECK (status IN ('ACTIVE', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_professional_profile_city ON professional_profile(city);

CREATE TABLE company_profile (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    industry VARCHAR(100),
    website VARCHAR(255),
    email VARCHAR(150),
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    company_description TEXT,
    logo_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_company_profile PRIMARY KEY (id),
    CONSTRAINT uk_company_profile_user UNIQUE (user_id),
    CONSTRAINT uk_company_tax_id UNIQUE (tax_id),
    CONSTRAINT fk_company_profile_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),
    CONSTRAINT ck_company_profile_status CHECK (status IN ('ACTIVE', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_company_name ON company_profile(company_name);
CREATE INDEX IF NOT EXISTS idx_company_city ON company_profile(city);
CREATE INDEX IF NOT EXISTS idx_company_industry ON company_profile(industry);