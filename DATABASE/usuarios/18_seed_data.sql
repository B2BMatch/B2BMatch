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

-- =============================================
-- USUARIOS DEMO
-- Contraseñas: Admin123, Webtester1, Company123
-- Los perfiles de prueba (18_seed_data.sql de perfiles)
-- dependen de webtester@example.com y companytester@example.com
-- =============================================

INSERT INTO app_user (role_id, email, password_hash, status) VALUES
((SELECT id FROM role WHERE name = 'ADMIN'),       'admin@test.com',          '$2a$10$6OgaOGNrs6Qj8CEAhxilNOpt7O7ep27.pcVKaRrf4nfIzr19NOA9.', 'ACTIVE'),
((SELECT id FROM role WHERE name = 'PROFESSIONAL'),'webtester@example.com',   '$2a$10$PGszWKVoEGbpX5IBma17G./WyujkF9BBYA1qBG6GtR24ntm3yWs/2', 'ACTIVE'),
((SELECT id FROM role WHERE name = 'COMPANY'),     'companytester@example.com','$2a$10$ZpLBJGIt91vjZ./uo/y/8usi7du2Rn.rbQH60kBWIJgdbSSNqiGlC', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;