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