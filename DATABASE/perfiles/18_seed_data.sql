SET search_path TO perfiles, public;

/*
====================================================
 Project : b2bmatch
 Microservice : perfiles
 File    : 18_seed_data.sql
====================================================
*/

-- Inserta un professional_profile para el usuario webtester@example.com (si existe y no tiene perfil)
INSERT INTO professional_profile (user_id, first_name, last_name, phone, biography, experience_years, hourly_rate, city, country)
SELECT u.id, 'Web', 'Tester', '123456789', 'Perfil de prueba creado por migraciones para pruebas web.', 2, 20.00, 'Ciudad Test', 'Pais Test'
FROM usuarios.app_user u
WHERE u.email = 'webtester@example.com'
  AND NOT EXISTS (SELECT 1 FROM professional_profile p WHERE p.user_id = u.id);

-- Inserta un company_profile para el usuario companytester@example.com (si existe y no tiene perfil)
INSERT INTO company_profile (user_id, company_name, tax_id, industry, website, email, phone, address, city, country, company_description)
SELECT u.id, 'Company Tester SRL', 'TAX-0001', 'Servicios TI', 'https://companytester.local', u.email, '987654321', 'Calle Falsa 123', 'Ciudad Test', 'Pais Test', 'Empresa usada para pruebas'
FROM usuarios.app_user u
WHERE u.email = 'companytester@example.com'
  AND NOT EXISTS (SELECT 1 FROM company_profile c WHERE c.user_id = u.id);

-- Fallback: si no existen esos emails, y existe un usuario cualquiera, inserta un professional_profile para el primer usuario (para facilitar pruebas)
INSERT INTO professional_profile (user_id, first_name, last_name, phone, biography, experience_years, hourly_rate, city, country)
SELECT u.id, 'Auto', 'Tester', '000000000', 'Perfil de prueba automático', 1, 10.00, 'Ciudad Auto', 'Pais Auto'
FROM (
  SELECT id FROM usuarios.app_user ORDER BY id LIMIT 1
) u
WHERE NOT EXISTS (SELECT 1 FROM professional_profile p WHERE p.user_id = u.id);
