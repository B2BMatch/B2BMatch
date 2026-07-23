-- ================================================
-- Tabla: skill
-- Descripción: Catálogo de habilidades y competencias profesionales que pueden poseer los usuarios.
-- Utilizada por: professional_skill
-- ================================================


CREATE TABLE skill (

    id BIGSERIAL PRIMARY KEY,
    
    name VARCHAR(100) NOT NULL UNIQUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);


