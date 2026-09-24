CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_role PRIMARY KEY (id),
    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL,
    role_id BIGINT NOT NULL,
    email VARCHAR(150) NOT NULL,
    name VARCHAR(150),
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT fk_app_user_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT uk_app_user_email UNIQUE (email),
    CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'PENDING', 'SUSPENDED', 'INACTIVE', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_app_user_role ON app_user(role_id);