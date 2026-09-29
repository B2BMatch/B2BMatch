-- Columnas de borrado para las tablas de este servicio.
--
-- La misma definicion esta tambien en `usuarios/V901`, que las aplica en sus
-- propias tablas por ser dueno del cascade. Repetirla aqui es intencional:
--
--   - Es idempotente (`IF NOT EXISTS`), asi que da igual que se aplique una,
--     otra o las dos veces. El orden de arranque de produccion es
--     usuarios -> perfiles -> catalogo -> ofertas, pero que el esquema de
--     ofertas no dependa de que otro servicio haya migrating antes es lo que
--     hace que su base de test se pueda montar sola (Testcontainers corre solo
--     las migraciones de este modulo).
--   - Una columna de una tabla debe declararla el servicio dueno de esa tabla.
--     `job_offer` la creo ofertas/V1, asi que sus columnas son asunto de ofertas.
--
-- Sin esta migracion, `ddl-auto: validate` falla con
-- "missing column [deleted_at] in table [job_offer]" al levantar el contexto
-- de los tests, porque ninguna migracion de ofertas creaba la columna que la
-- entidad JobOffer mapea.

ALTER TABLE ofertas.job_offer
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

ALTER TABLE ofertas.job_offer
    ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);
