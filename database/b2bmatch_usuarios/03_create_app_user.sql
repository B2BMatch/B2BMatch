/*
====================================================
 Project : b2bmatch
 File    : 03_create_app_user.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: app_user
-- Description:
-- Stores all authenticated users.
-- =============================================

CREATE TABLE app_user (

    id BIGSERIAL,

    role_id BIGINT NOT NULL,

    email VARCHAR(150) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_app_user
        PRIMARY KEY (id),

    CONSTRAINT fk_app_user_role
        FOREIGN KEY (role_id)
        REFERENCES role(id),

    CONSTRAINT uk_app_user_email
        UNIQUE (email),

    CONSTRAINT ck_app_user_status
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

COMMENT ON TABLE app_user IS 'Stores all platform users';

COMMENT ON COLUMN app_user.id IS 'Primary key';

COMMENT ON COLUMN app_user.role_id IS 'User role';

COMMENT ON COLUMN app_user.email IS 'Unique email address';

COMMENT ON COLUMN app_user.password_hash IS 'Encrypted password';

COMMENT ON COLUMN app_user.status IS 'Account status';

COMMENT ON COLUMN app_user.created_at IS 'Creation date';

COMMENT ON COLUMN app_user.updated_at IS 'Last update';

CREATE INDEX idx_app_user_role
ON app_user(role_id);

CREATE INDEX idx_app_user_email
ON app_user(email);