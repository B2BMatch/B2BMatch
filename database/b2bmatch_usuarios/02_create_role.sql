/*
====================================================
 Project : b2bmatch
 File    : 02_create_role.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: role
-- Description:
-- Stores the roles available in the platform.
-- =============================================

CREATE TABLE role (

    id BIGSERIAL,

    name VARCHAR(50) NOT NULL,

    description VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT pk_role
        PRIMARY KEY (id),

    CONSTRAINT uk_role_name
        UNIQUE (name)

);




COMMENT ON TABLE role IS 'Stores application roles';

COMMENT ON COLUMN role.id IS 'Primary key';

COMMENT ON COLUMN role.name IS 'Role name';

COMMENT ON COLUMN role.description IS 'Role description';

COMMENT ON COLUMN role.created_at IS 'Creation date';

COMMENT ON COLUMN role.updated_at IS 'Last update';

CREATE INDEX idx_role_name
ON role(name);