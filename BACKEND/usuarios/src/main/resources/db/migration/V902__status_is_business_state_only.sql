-- Cierre de la transicion iniciada en V901: `deleted_at` es la unica fuente de
-- verdad sobre el borrado y `status` vuelve a ser solo estado de negocio.
--
-- V901 escribia `status = 'DELETED'` como marca de borrado mientras los demas
-- microservicios filtraban por `status <> 'DELETED'`. Los 29 lectores ya leen
-- `deleted_at IS NULL`, asi que esa escritura se retira: desde ahora un borrado
-- no toca `status`.
--
-- Consecuencia: `previous_status` deja de tener trabajo. El estado de negocio
-- ya no se sobrescribe al borrar, asi que no hay nada que guardar ni que
-- restaurar. La columna no se borra todavia para no atar esta migracion al
-- despliegue de todos los servicios a la vez; queda inerte y se puede retirar
-- en una posterior.
--
-- Estas 5 tablas (incluidas las 4 de otros schemas) las migra `usuarios` por lo
-- mismo que explica V901: es dueno del cascade y el arranque es usuarios ->
-- perfiles -> catalogo -> ofertas, asi que en una sola transaccion el conjunto
-- es atomico.

-- 1) Devolver el estado de negocio a las filas que el codigo viejo dejo en
--    DELETED. Sin esto, restaurar una de estas filas la devolveria con
--    status = 'DELETED' para siempre.
UPDATE usuarios.app_user
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

UPDATE perfiles.company_profile
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

UPDATE perfiles.professional_profile
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

UPDATE perfiles.customer_profile
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';

UPDATE ofertas.job_offer
   SET status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED';
