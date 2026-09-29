-- Tablas minimas de OTROS microservicios que ofertas lee o referencia.
--
-- Este archivo documenta el acoplamiento real de ofertas: son las unicas
-- estructuras ajenas de las que depende. Si el dia de manana se separan las
-- bases, esta es exactamente la lista de datos que habria que obtener por API.
--
-- Solo contiene las columnas que ofertas usa; no pretende replicar el esquema
-- completo de usuarios, perfiles ni catalogo.
--
-- `deleted_at` esta en las cinco porque ofertas ya no las filtra por
-- status='DELETED': ProfileOwnerResolver consulta `deleted_at IS NULL` sobre
-- perfiles y catalogo. Sin la columna esas consultas fallan con
-- 'column deleted_at does not exist' en vez de con la excepcion de dominio que
-- el test espera.

CREATE SCHEMA IF NOT EXISTS usuarios;
CREATE SCHEMA IF NOT EXISTS perfiles;
CREATE SCHEMA IF NOT EXISTS catalogo;
CREATE SCHEMA IF NOT EXISTS ofertas;

-- FK de job_offer.user_id, application_table.user_id y quotation.user_id
CREATE TABLE usuarios.app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
,
    deleted_at TIMESTAMP);

-- ProfileOwnerResolver.hasActiveProfessionalProfile / serviceOwnerUserId
CREATE TABLE perfiles.professional_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES usuarios.app_user(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
,
    deleted_at TIMESTAMP);

-- ProfileOwnerResolver.hasActiveCompanyProfile
CREATE TABLE perfiles.company_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES usuarios.app_user(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
,
    deleted_at TIMESTAMP);

-- FK de job_offer.category_id + ProfileOwnerResolver.hasActiveCategory
CREATE TABLE catalogo.category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
,
    deleted_at TIMESTAMP);

-- FK de quotation.service_id + ProfileOwnerResolver.isActiveService
CREATE TABLE catalogo.professional_service (
    id BIGSERIAL PRIMARY KEY,
    professional_id BIGINT NOT NULL REFERENCES perfiles.professional_profile(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
,
    deleted_at TIMESTAMP);
