INSERT INTO role (name, description) VALUES
('ADMIN', 'Administrador del sistema'),
('CUSTOMER', 'Solicitante de servicios'),
('PROFESSIONAL', 'Proveedor de servicios'),
('COMPANY', 'Empresa proveedora de servicios')
ON CONFLICT (name) DO NOTHING;
