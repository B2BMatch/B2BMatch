SET search_path TO usuarios, public;

/*
====================================================
 Project : b2bmatch
 Microservice : Usuarios
 File    : 18_seed_data.sql
====================================================
*/

-- =============================================
-- INITIAL DATA SEEDING
-- Description:
-- Initial user roles.
-- =============================================

INSERT INTO role (name, description) VALUES
('ADMIN', 'Administrador del sistema'),
('CUSTOMER', 'Solicitante de servicios'),
('PROFESSIONAL', 'Proveedor de servicios'),
('COMPANY', 'Empresa proveedora de servicios');

-- Usuarios demo (contraseñas ya hasheadas con BCrypt)
INSERT INTO app_user (role_id, email, password_hash, status)
SELECT r.id, 'admin@test.com', '$2b$10$5ZIsRTTIr2AL7KpjMrxmlOzSLPqxYP6PkyBTSs4yLndzmqs.zYCyO', 'ACTIVE'
FROM role r WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'admin@test.com');

INSERT INTO app_user (role_id, email, password_hash, status)
SELECT r.id, 'webtester@example.com', '$2b$10$UbXn2HZBpFBK/vWzNC3FlutcLOcQGVNX4c0gr23UIJgO4ek4huami', 'ACTIVE'
FROM role r WHERE r.name = 'PROFESSIONAL'
  AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'webtester@example.com');

INSERT INTO app_user (role_id, email, password_hash, status)
SELECT r.id, 'companytester@example.com', '$2b$10$epaWTxj4oXMRAvmR3yivXuDcbWQNx9.jTzTV9yFZmhICmHs3k3GSa', 'ACTIVE'
FROM role r WHERE r.name = 'COMPANY'
  AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'companytester@example.com');