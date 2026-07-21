/*
====================================================
 Project : b2bmatch
 File    : 18_seed_data.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- INITIAL DATA SEEDING
-- Description:
-- Populates initial required roles.
-- =============================================

INSERT INTO role (name, description) VALUES 
('ADMIN', 'System administrator'),
('CUSTOMER', 'Service requester'),
('PROFESSIONAL', 'Service provider'),
('COMPANY', 'Corporate service provider');