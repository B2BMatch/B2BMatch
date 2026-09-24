-- Ofertas de ejemplo publicadas por la empresa demo (companytester@example.com)
INSERT INTO job_offer (user_id, category_id, title, description, budget, deadline, status)
SELECT u.id,
    (SELECT id FROM catalogo.category WHERE name = 'Software Development'),
    'Senior Java Developer',
    'Desarrollador Java senior con experiencia en Spring Boot para proyecto B2B. Trabajo remoto, modalidad full-time.',
    8500.00,
    CURRENT_DATE + INTERVAL '60 days',
    'ACTIVE'
FROM usuarios.app_user u
JOIN perfiles.company_profile c ON c.user_id = u.id
WHERE u.email = 'companytester@example.com'
  AND EXISTS (SELECT 1 FROM catalogo.category WHERE name = 'Software Development')
  AND NOT EXISTS (SELECT 1 FROM job_offer WHERE user_id = u.id AND title = 'Senior Java Developer');

INSERT INTO job_offer (user_id, category_id, title, description, budget, deadline, status)
SELECT u.id,
    (SELECT id FROM catalogo.category WHERE name = 'Web Development'),
    'Frontend Developer React',
    'Ingeniero/a frontend con React y TypeScript para construir interfaces de marketplace.',
    6200.00,
    CURRENT_DATE + INTERVAL '45 days',
    'ACTIVE'
FROM usuarios.app_user u
JOIN perfiles.company_profile c ON c.user_id = u.id
WHERE u.email = 'companytester@example.com'
  AND EXISTS (SELECT 1 FROM catalogo.category WHERE name = 'Web Development')
  AND NOT EXISTS (SELECT 1 FROM job_offer WHERE user_id = u.id AND title = 'Frontend Developer React');

INSERT INTO job_offer (user_id, category_id, title, description, budget, deadline, status)
SELECT u.id,
    (SELECT id FROM catalogo.category WHERE name = 'Data Science'),
    'Data Analyst',
    'Analista de datos con SQL y Python para reportes de negocio y dashboards.',
    5000.00,
    CURRENT_DATE + INTERVAL '30 days',
    'ACTIVE'
FROM usuarios.app_user u
JOIN perfiles.company_profile c ON c.user_id = u.id
WHERE u.email = 'companytester@example.com'
  AND EXISTS (SELECT 1 FROM catalogo.category WHERE name = 'Data Science')
  AND NOT EXISTS (SELECT 1 FROM job_offer WHERE user_id = u.id AND title = 'Data Analyst');