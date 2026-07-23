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



