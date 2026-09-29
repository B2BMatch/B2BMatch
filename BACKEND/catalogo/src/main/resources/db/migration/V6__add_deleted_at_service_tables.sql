-- catalogo asume la propiedad de las columnas de borrado de sus propias tablas
-- de servicios.
--
-- professional_service y company_service las crea catalogo/V1, asi que por el
-- mismo criterio que se aplico en ofertas/V901 (job_offer) y en catalogo/V4
-- (category y skill), las columnas que necesita declarar la tabla deben
-- declararlas aqui y no el dueno de la cascada. usuarios/V901 las creo en su
-- momento, y por eso V5 fallaba con
-- 'column previous_status does not exist' en una base donde usuarios no se
-- hubiera ejecutado antes: en produccion el orden de arranque
-- usuarios -> perfiles -> catalogo lo tapaba.
--
-- Los ALTER son IF NOT EXISTS a proposito: son idempotentes, asi que da igual
-- que esta migracion se aplique sobre una base que ya tiene las columnas (por
-- usuarios/V901) como sobre una recien creada por V1. Sin esa idempotencia,
-- esta migracion solo funcionaria en el primer caso, que es justo el que no
-- estaba fallando.

ALTER TABLE catalogo.professional_service ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE catalogo.professional_service ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);
ALTER TABLE catalogo.company_service ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE catalogo.company_service ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

-- Normalizacion que antes vivia en V5, movida aqui porque ahora la columna
-- existe de forma garantizada. `previous_status` en professional_service
-- tambien puede ser PENDING, que es un estado valido de esa tabla, asi que el
-- COALESCE solo cubre el caso nulo.

UPDATE catalogo.professional_service
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

UPDATE catalogo.company_service
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

-- Indices parciales para los listados, que siempre filtran `deleted_at IS NULL`.
CREATE INDEX IF NOT EXISTS ix_professional_service_not_deleted
    ON catalogo.professional_service (professional_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_company_service_not_deleted
    ON catalogo.company_service (company_id) WHERE deleted_at IS NULL;
