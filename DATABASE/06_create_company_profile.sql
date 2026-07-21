/*
====================================================
 Project : b2bmatch
 File    : 06_create_company_profile.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: company_profile
-- Description:
-- Stores company profile information.
-- One profile per company user.
-- =============================================

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

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_company_profile
        PRIMARY KEY (id),

    CONSTRAINT fk_company_profile_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_company_profile_user
        UNIQUE (user_id),

    CONSTRAINT uk_company_tax_id
        UNIQUE (tax_id)

);

COMMENT ON TABLE company_profile IS 'Stores company information';

COMMENT ON COLUMN company_profile.user_id IS 'Reference to app_user';

COMMENT ON COLUMN company_profile.company_name IS 'Company legal name';

COMMENT ON COLUMN company_profile.tax_id IS 'Company tax identifier';

COMMENT ON COLUMN company_profile.website IS 'Official website';

COMMENT ON COLUMN company_profile.logo_url IS 'Company logo URL';

CREATE INDEX idx_company_name
ON company_profile(company_name);

CREATE INDEX idx_company_city
ON company_profile(city);

CREATE INDEX idx_company_industry
ON company_profile(industry);