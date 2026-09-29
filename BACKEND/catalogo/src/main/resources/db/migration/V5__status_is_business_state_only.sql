-- Cierre de la transicion en las 4 tablas de catalogo. La counterparts en
-- usuarios/perfiles/ofertas esta en usuarios V902, por el mismo criterio de
-- arranque documentado en V901.
--
-- V3/V4 escribian `status = 'DELETED'` como marca de borrado. Los 21 lectores
-- de catalogo ya filtran por `deleted_at IS NULL`, asi que la escritura se retira
-- y `status` vuelve a ser solo estado de negocio (ACTIVE / INACTIVE /
-- SUSPENDED en catalogos; ACTIVE / PENDING / SUSPENDED / INACTIVE en servicios).

-- category: restaurar el estado de negocio de lo que quedo en DELETED.
UPDATE catalogo.category
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

UPDATE catalogo.skill
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

-- professional_service y company_service se normalizan en V6, no aqui.
--
-- Estas dos tablas tambien son de catalogo, pero sus columnas las declaraba
-- usuarios/V901 (por ser duena de la cascada), de modo que V5 fallaba con
-- 'column previous_status does not exist' en cualquier base donde usuarios no
-- se hubiera ejecutado antes. V6 asume la propiedad de esas columnas con ALTER
-- idempotente, y hace alli la normalizacion.

