-- Tabla minima de OTROS microservicios que perfiles necesita.
--
-- perfiles/V1 declara en sus tres tablas
--   FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id)
-- asi que el esquema de perfiles no se monta solo: sin esta tabla las
-- migraciones fallan al crear las FK.
--
-- perfiles no consulta ninguna otra estructura ajena (ni de catalogo ni de
-- ofertas): su unico acoplamiento es esa FK.

CREATE SCHEMA IF NOT EXISTS usuarios;

CREATE TABLE usuarios.app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP
);
