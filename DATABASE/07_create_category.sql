-- =====================================================
-- Tabla: category
-- Descripción: Catálogo de categorías de servicios
-- Utilizada por:
-- service
-- company_service
-- job_offer
-- =====================================================

CREATE TABLE category (

    id BIGSERIAL PRIMARY KEY,
    
    name VARCHAR(100) NOT NULL UNIQUE,

    description TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);




-- =====================================================
-- Inserción de categorías iniciales
-- =====================================================

INSERT INTO category (name, description) VALUES

('Software Development', 
 'Desarrollo de software y soluciones tecnológicas'),

('Web Development', 
 'Diseño y desarrollo de aplicaciones web'),

('Mobile Development', 
 'Desarrollo de aplicaciones móviles Android e iOS'),

('UI/UX Design', 
 'Diseño de interfaces y experiencia de usuario'),

('Graphic Design', 
 'Diseño gráfico y creación de contenido visual'),

('Digital Marketing', 
 'Marketing digital, publicidad y estrategias online'),

('Accounting', 
 'Servicios contables y financieros'),

('Legal Services', 
 'Servicios legales y asesoría jurídica'),

('Architecture', 
 'Servicios de arquitectura y diseño de espacios'),

('Construction', 
 'Servicios relacionados a construcción y obras'),

('Photography', 
 'Servicios profesionales de fotografía'),

('Translation', 
 'Servicios de traducción e interpretación'),

('Cybersecurity', 
 'Seguridad informática y protección de datos'),

('Cloud Computing', 
 'Servicios basados en infraestructura cloud'),

('Data Science', 
 'Análisis de datos y ciencia de datos'),

('Artificial Intelligence', 
 'Soluciones basadas en inteligencia artificial'),

('Business Consulting', 
 'Consultoría empresarial y estratégica');

