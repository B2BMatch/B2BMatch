-- Tablas minimas de OTROS microservicios que catalogo lee.
--
-- Catalogo es dueno de sus 5 tablas (category, skill, professional_skill,
-- professional_service, company_service): las crea su propia V1 y las migra
-- suyas. Solo necesita de perfiles lo que usa para resolver duenos y para
-- validar que un profesional sigue dado de alta.
--
-- Este archivo documenta el acoplamiento real de catalogo hacia perfiles: son
-- las unicas estructuras ajenas de las que depende. Si el dia de manana se
-- separan las bases, esta es exactamente la lista de datos que habria que
-- obtener por API.
--
-- Solo se incluyen las columnas que catalogo consulta de verdad: id, user_id
-- y deleted_at. No se replica el esquema completo de perfiles.

CREATE SCHEMA IF NOT EXISTS usuarios;
CREATE SCHEMA IF NOT EXISTS perfiles;

-- ProfessionalServiceController: dueño del servicio y "el profesional existe"
CREATE TABLE perfiles.professional_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP
);

-- CompanyServiceController: dueño del servicio de empresa
CREATE TABLE perfiles.company_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP
);
