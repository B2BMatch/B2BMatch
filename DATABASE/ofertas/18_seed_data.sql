SET search_path TO ofertas, public;

/*
====================================================
 Project : b2bmatch
 Microservice : ofertas
 File    : 18_seed_data.sql
====================================================
*/

-- Ofertas de ejemplo publicadas por la empresa demo (companytester@example.com)
INSERT INTO job_offer (company_id, user_id, category_id, title, description, budget, deadline, status)
SELECT
    (SELECT c.id FROM perfiles.company_profile c
     JOIN usuarios.app_user u ON c.user_id = u.id
     WHERE u.email = 'companytester@example.com' LIMIT 1),
    (SELECT u.id FROM usuarios.app_user u WHERE u.email = 'companytester@example.com' LIMIT 1),
    (SELECT id FROM catalogo.category WHERE name = 'Software Development'),
    'Senior Java Developer',
    'Desarrollador Java senior con experiencia en Spring Boot para proyecto B2B. Trabajo remoto, modalidad full-time.',
    8500.00,
    CURRENT_DATE + INTERVAL '60 days',
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM perfiles.company_profile);

INSERT INTO job_offer (company_id, user_id, category_id, title, description, budget, deadline, status)
SELECT
    (SELECT c.id FROM perfiles.company_profile c
     JOIN usuarios.app_user u ON c.user_id = u.id
     WHERE u.email = 'companytester@example.com' LIMIT 1),
    (SELECT u.id FROM usuarios.app_user u WHERE u.email = 'companytester@example.com' LIMIT 1),
    (SELECT id FROM catalogo.category WHERE name = 'Web Development'),
    'Frontend Developer React',
    'Ingeniero/a frontend con React y TypeScript para construir interfaces de marketplace.',
    6200.00,
    CURRENT_DATE + INTERVAL '45 days',
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM perfiles.company_profile);

INSERT INTO job_offer (company_id, user_id, category_id, title, description, budget, deadline, status)
SELECT
    (SELECT c.id FROM perfiles.company_profile c
     JOIN usuarios.app_user u ON c.user_id = u.id
     WHERE u.email = 'companytester@example.com' LIMIT 1),
    (SELECT u.id FROM usuarios.app_user u WHERE u.email = 'companytester@example.com' LIMIT 1),
    (SELECT id FROM catalogo.category WHERE name = 'Data Science'),
    'Data Analyst',
    'Analista de datos con SQL y Python para reportes de negocio y dashboards.',
    5000.00,
    CURRENT_DATE + INTERVAL '30 days',
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM perfiles.company_profile);
