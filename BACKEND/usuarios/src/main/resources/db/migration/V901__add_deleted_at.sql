-- Separacion de "borrado" y "estado de negocio".
--
-- Hoy `status` cumple dos funciones a la vez: estado del ciclo de vida
-- (ACTIVE / INACTIVE / SUSPENDED / ...) y bandera de borrado (DELETED).
-- Como el borrado sobrescribe `status`, el estado original se pierde y
-- `reactivate` no puede restaurarlo: devuelve una cuenta sin perfil,
-- sin ofertas y sin servicios.
--
-- `deleted_at` pasa a ser la unica fuente de verdad sobre "esta borrado".
-- `previous_status` guarda el estado de negocio previo para poder restaurarlo
-- exacto. `status = 'DELETED'` se sigue escribiendo durante la transicion
-- porque los demas microservicios todavia filtran por `status <> 'DELETED'`;
-- cuando todos lean `deleted_at IS NULL`, esta columna se puede retirar.
--
-- Las columnas de los otros 3 schemas viven aqui a proposito: `usuarios` es
-- dueno del cascade que escribe esas 6 tablas, y la cadena de arranque es
-- usuarios -> perfiles -> catalogo -> ofertas. Si cada servicio declarara las
-- suyas, `usuarios` podria arrancar y ejecutar un borrado antes de que existieran.
-- En una sola migracion el conjunto es atomico. Postgres envuelve el DDL en
-- transaccion, asi que o se aplican las 8 tablas o ninguna.

ALTER TABLE usuarios.app_user          ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE usuarios.app_user          ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE perfiles.company_profile    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE perfiles.company_profile    ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE perfiles.professional_profile ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE perfiles.professional_profile ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE perfiles.customer_profile   ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE perfiles.customer_profile   ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE ofertas.job_offer           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE ofertas.job_offer           ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE catalogo.professional_service ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE catalogo.professional_service ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

ALTER TABLE catalogo.company_service    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE catalogo.company_service    ADD COLUMN IF NOT EXISTS previous_status VARCHAR(20);

-- Backfill de lo que ya estaba borrado antes de esta migracion.
-- El estado de negocio original de esas filas ya se perdio en su momento:
-- se asume ACTIVE y queda registrado en previous_status para que sea visible
-- y corregible a mano. Es la unica parte del cambio que no es reversible.
UPDATE usuarios.app_user
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

UPDATE perfiles.company_profile
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

UPDATE perfiles.professional_profile
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

UPDATE perfiles.customer_profile
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

UPDATE ofertas.job_offer
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

UPDATE catalogo.professional_service
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;

UPDATE catalogo.company_service
   SET deleted_at = CURRENT_TIMESTAMP, previous_status = COALESCE(previous_status, 'ACTIVE')
 WHERE status = 'DELETED' AND deleted_at IS NULL;
