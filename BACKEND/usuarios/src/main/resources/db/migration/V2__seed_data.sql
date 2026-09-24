INSERT INTO role (name, description) VALUES
('ADMIN', 'Administrador del sistema'),
('CUSTOMER', 'Solicitante de servicios'),
('PROFESSIONAL', 'Proveedor de servicios'),
('COMPANY', 'Empresa proveedora de servicios')
ON CONFLICT (name) DO NOTHING;

-- Usuarios demo (contraseñas ya hasheadas con BCrypt)
INSERT INTO app_user (role_id, email, name, password_hash, status)
SELECT r.id, 'admin@test.com', 'Admin B2BMatch', '$2b$10$5ZIsRTTIr2AL7KpjMrxmlOzSLPqxYP6PkyBTSs4yLndzmqs.zYCyO', 'ACTIVE'
FROM role r WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'admin@test.com');

INSERT INTO app_user (role_id, email, name, password_hash, status)
SELECT r.id, 'webtester@example.com', 'Web Tester', '$2b$10$UbXn2HZBpFBK/vWzNC3FlutcLOcQGVNX4c0gr23UIJgO4ek4huami', 'ACTIVE'
FROM role r WHERE r.name = 'PROFESSIONAL'
  AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'webtester@example.com');

INSERT INTO app_user (role_id, email, name, password_hash, status)
SELECT r.id, 'companytester@example.com', 'Company Tester', '$2b$10$epaWTxj4oXMRAvmR3yivXuDcbWQNx9.jTzTV9yFZmhICmHs3k3GSa', 'ACTIVE'
FROM role r WHERE r.name = 'COMPANY'
  AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'companytester@example.com');