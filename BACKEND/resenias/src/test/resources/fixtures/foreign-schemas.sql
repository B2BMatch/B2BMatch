-- Tablas minimas de OTROS microservicios que resenias necesita.
--
-- Este archivo documenta el acoplamiento real de resenias, que es el mayor de
-- los seis servicios: no solo referencia `usuarios.app_user` y
-- `perfiles.professional_profile` por FK, ademas los consulta por SQL crudo para
-- validar que la transaccion previa este completada. Si el dia de manana se
-- separan las bases, esta es exactamente la lista de datos que habria que
-- obtener por API.
--
-- Solo contiene las columnas que resenias usa; no pretende replicar el esquema
-- completo de usuarios, perfiles, catalogo ni ofertas.
--
-- `deleted_at` esta en las dos tablas de perfiles porque `ReviewService` exige
-- `deleted_at IS NULL` al validar el profesional: sin la columna, la consulta
-- falla con 'column deleted_at does not exist' en vez de con la excepcion de
-- dominio que el test espera.

CREATE SCHEMA IF NOT EXISTS usuarios;
CREATE SCHEMA IF NOT EXISTS perfiles;
CREATE SCHEMA IF NOT EXISTS catalogo;
CREATE SCHEMA IF NOT EXISTS ofertas;

-- FK de review.user_id
CREATE TABLE usuarios.app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP
);

-- FK de review.professional_id + la validacion de profesional activo
CREATE TABLE perfiles.professional_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES usuarios.app_user(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP
);

-- Transaccion previa de una COMPANY: postulacion ACCEPTED a una oferta propia
CREATE TABLE ofertas.job_offer (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES usuarios.app_user(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP
);

CREATE TABLE ofertas.application_table (
    id BIGSERIAL PRIMARY KEY,
    job_offer_id BIGINT NOT NULL REFERENCES ofertas.job_offer(id),
    user_id BIGINT NOT NULL REFERENCES usuarios.app_user(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

-- Transaccion previa de COMPANY y CUSTOMER: cotizacion ACCEPTED sobre un
-- servicio del profesional
CREATE TABLE catalogo.professional_service (
    id BIGSERIAL PRIMARY KEY,
    professional_id BIGINT NOT NULL REFERENCES perfiles.professional_profile(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP
);

CREATE TABLE ofertas.quotation (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES catalogo.professional_service(id),
    user_id BIGINT NOT NULL REFERENCES usuarios.app_user(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);
