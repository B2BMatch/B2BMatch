/*
====================================================
 Project : b2bmatch
 File    : 04_create_customer_profile.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: customer_profile
-- Description:
-- Stores customer profile information.
-- One profile per user.
-- =============================================

CREATE TABLE customer_profile (

    id BIGSERIAL,

    user_id BIGINT NOT NULL,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    phone VARCHAR(20),

    address VARCHAR(255),

    city VARCHAR(100),

    country VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_customer_profile
        PRIMARY KEY (id),

    CONSTRAINT fk_customer_profile_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_customer_profile_user
        UNIQUE (user_id)

);

COMMENT ON TABLE customer_profile IS 'Customer profile information';

COMMENT ON COLUMN customer_profile.user_id IS 'Reference to app_user';

CREATE INDEX idx_customer_profile_user
ON customer_profile(user_id);

CREATE INDEX idx_customer_profile_last_name
ON customer_profile(last_name);