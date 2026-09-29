-- Tablas minimas de OTROS microservicios que usuarios necesita.
--
-- A diferencia de los demas modulos, usuarios tiene la superficie de acoplamiento
-- mas grande del proyecto: es dueno de la cascada de borrado, asi que su
-- migracion V901 altera 6 tablas de 4 schemas y su servicio escribe en 8
-- tablas de 5 schemas. Este fixture es la lista exacta de lo que el cascade
-- toca; si el dia de manana se separan las bases, esta es la lista de datos que
-- habria que obtener por API.
--
-- Se crean aqui porque el init script corre ANTES que Flyway, y V901 hace
-- `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` sobre cada una: sin las tablas
-- esas migraciones fallan con 'relation does not exist'.
--
-- Nota: son stubs, no replicas del esquema real. Las columnas de borrado
-- (deleted_at, previous_status) las agrega la propia V901 de usuarios.

CREATE SCHEMA IF NOT EXISTS perfiles;
CREATE SCHEMA IF NOT EXISTS ofertas;
CREATE SCHEMA IF NOT EXISTS catalogo;
CREATE SCHEMA IF NOT EXISTS notificaciones;
CREATE SCHEMA IF NOT EXISTS resenias;

-- Cascade: perfiles.* por user_id
CREATE TABLE perfiles.company_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP
);
CREATE TABLE perfiles.professional_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP
);
CREATE TABLE perfiles.customer_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP
);

-- Cascade: ofertas.job_offer por user_id
CREATE TABLE ofertas.job_offer (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP
);

-- Cascade: catalogo.* por professional_id / company_id (subconsulta a perfiles)
CREATE TABLE catalogo.professional_service (
    id BIGSERIAL PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP
);
CREATE TABLE catalogo.company_service (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP
);

-- NO forman parte de la cascata reversible: el cascade las borra en duro
-- (DELETE, no UPDATE) porque no tienen columna de borrado: notificar y reseñar
-- a un usuario dado de baja no tiene sentido y no se restauran.
CREATE TABLE notificaciones.notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL
);
CREATE TABLE resenias.review (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL
);
