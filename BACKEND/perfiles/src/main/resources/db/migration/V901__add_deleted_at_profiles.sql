-- perfiles asume la propiedad de las columnas de borrado de sus propias tablas.
--
-- company_profile, professional_profile y customer_profile las crea perfiles/V1,
-- asi que por el mismo criterio ya aplicado en ofertas/V901 (job_offer) y
-- catalogo/V6 (professional_service, company_service), las columnas que
-- necesita declarar una tabla las declara el servicio dueno de esa tabla.
--
-- Hasta aqui las declaraba usuarios/V901, por ser duena de la cascada de
-- borrado. Eso hacia que perfiles NO fuera autocontenido: su cadena de
-- migraciones reventaba con
--   missing column [deleted_at] in table [professional_profile]
-- (el fallo de `ddl-auto: validate` al montar Hibernate) en cualquier base
-- donde usuarios no se hubiera ejecutado antes. En produccion el orden
-- usuarios -> perfiles -> catalogo -> ofertas lo tapaba; en el arranque de
-- perfiles, no.
--
-- Los ALTER son IF NOT EXISTS a proposito: son idempotentes, asi que da igual
-- que esta migracion se aplique sobre una base que ya tiene las columnas (por
-- usuarios/V901) como sobre una recien creada por V1. Sin esa idempotencia
-- solo funcionaria en el primer caso, que es justo el que no fallaba.
--
-- `previous_status` se crea por compatibilidad con la entidad, que todavia la
-- mapea, pero queda inerte: ni el borrado ni la reactivacion lo escriben, y
-- `status` en estas tres tablas solo admite 'ACTIVE' o 'DELETED' por CHECK
-- (V1), asi que no hay estado de negocio que preservar aqui.

ALTER TABLE perfiles.professional_profile ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE perfiles.professional_profile ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE perfiles.company_profile ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE perfiles.company_profile ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE perfiles.customer_profile ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE perfiles.customer_profile ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

-- Indices parciales para los listados, que siempre filtran `deleted_at IS NULL`.
CREATE INDEX IF NOT EXISTS ix_professional_profile_not_deleted
    ON perfiles.professional_profile (id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_company_profile_not_deleted
    ON perfiles.company_profile (id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_customer_profile_not_deleted
    ON perfiles.customer_profile (id) WHERE deleted_at IS NULL;
