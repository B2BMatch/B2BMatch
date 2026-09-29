-- Fase 2 de la separacion borrado/estado: catalogo.category y catalogo.skill.
--
-- V3 agrego `status` a estas dos tablas para poder "borrarlas" sin DELETE
-- fisico, pero el mismo problema que V901 resolvio en el resto del schema
-- sigue presente: `status` cumple dos funciones a la vez (estado de negocio
-- ACTIVE/INACTIVE/SUSPENDED y bandera de borrado DELETED). Al borrar se
-- sobrescribe el estado de negocio y no hay forma de recuperarlo, porque no
-- existe ningun endpoint de restauracion para estas dos tablas.
--
-- Aqui `deleted_at` pasa a ser la unica fuente de verdad y `previous_status`
-- guarda el estado previo, igual que en V901.
--
-- Estas columnas viven en `catalogo` y no en `usuarios` a diferencia de V901:
-- el cascade de borrado de usuario escribe company_profile, professional_profile,
-- customer_profile, job_offer, professional_service y company_service, pero NO
-- toca category ni skill. Category y skill son taxonomia global, no datos de
-- usuario: borrarle la cuenta a un profesional no debe tocar el catalogo.
-- Por lo tanto catalogo es su unico dueno y su migracion va aqui.
--
-- `deleted_at` y `previous_status` son NOT NULL-free a proposito: NULL es lo
-- que significa "no borrado", y la columna debe poder distinguirlo sin ambiguedad.

ALTER TABLE catalogo.category ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE catalogo.category ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE catalogo.skill ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE catalogo.skill ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

-- Backfill de lo que V3 dejo como DELETED sin deleted_at. El estado de negocio
-- original de esas filas ya se perdio cuando se borraron, asi que se asume
-- ACTIVE y queda registrado en previous_status para que sea visible y corregible
-- a mano. Es la unica parte del cambio que no es reversible, igual que en V901.
UPDATE catalogo.category
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

UPDATE catalogo.skill
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

-- Indice parcial para los listados, que son la operacion dominante y siempre
-- filtran por `deleted_at IS NULL`. Sin indice, `IS NULL` no usa el indice de la
-- PK y cada listado termina haciendo seq scan sobre la tabla completa.
CREATE INDEX IF NOT EXISTS ix_category_not_deleted
    ON catalogo.category (name) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS ix_skill_not_deleted
    ON catalogo.skill (name) WHERE deleted_at IS NULL;
