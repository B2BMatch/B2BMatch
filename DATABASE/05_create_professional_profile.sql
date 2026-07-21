/*
====================================================
 Project : b2bmatch
 File    : 05_create_professional_profile.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: professional_profile
-- Description:
-- Stores professional/freelancer information.
-- One profile per user.
-- =============================================

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

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_professional_profile
        PRIMARY KEY (id),

    CONSTRAINT fk_professional_profile_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_professional_profile_user
        UNIQUE (user_id),

    CONSTRAINT ck_professional_experience
        CHECK (experience_years >= 0),

    CONSTRAINT ck_professional_hourly_rate
        CHECK (hourly_rate IS NULL OR hourly_rate >= 0)

);

COMMENT ON TABLE professional_profile IS 'Professional profile information';

COMMENT ON COLUMN professional_profile.user_id IS 'Reference to app_user';

COMMENT ON COLUMN professional_profile.hourly_rate IS 'Hourly service rate';

COMMENT ON COLUMN professional_profile.experience_years IS 'Years of professional experience';

CREATE INDEX idx_professional_profile_user
ON professional_profile(user_id);

CREATE INDEX idx_professional_profile_city
ON professional_profile(city);