-- Columnas de borrado para las tablas de este servicio.
--
-- `review` la creo resenias/V1, asi que sus columnas son asunto de resenias.
-- La regla que queda escrita en cada migracion de esta fase es la contraria a
-- la que seguia `usuarios/V901`: centralizar las columnas de borrado en el
-- dueno del cascade parecia correcto y por eso el mismo error se repitio
-- modulo a modulo. Que el esquema de resenias no dependa de que otro servicio
-- haya migrating antes es justo lo que permite montar su base de test sola.
--
-- `review` no tiene columna `status`: no hay estado de negocio que preservar al
-- dar de baja, asi que `deleted_at` es la unica senal y no se anade
-- `previous_status` (a diferencia de usuarios, perfiles, catalogo y ofertas).
--
-- El ALTER es IF NOT EXISTS a proposito: es idempotente, asi que da igual que se
-- aplique sobre una base recien creada por V1 como sobre una que ya tenga la
-- columna. Sin esa idempotencia, la migracion solo funcionaria en el caso que no
-- estaba fallando.
--
-- Sin esta migracion, `ddl-auto: validate` falla con
-- "missing column [deleted_at] in table [review]" al levantar el contexto de los
-- tests, porque ninguna migracion de resenias creaba la columna que la entidad
-- Review mapea.

ALTER TABLE resenias.review
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- El borrado deja de ser definitivo, y con el UNIQUE de V1 el par
-- (user_id, professional_id) seguia ocupado por una fila invisible: el autor
-- borraba su reseña y ya no podia volver a reseñar a ese profesional, con un
-- "Ya dejaste una reseña" que no se podia resolver. El UNIQUE pasa a ser
-- parcial, sobre las reseñas vivas, que es lo que la regla de "una reseña por
-- autor y profesional" describe.
--
-- Se suelta la restriccion de tabla y no se renombra a la nueva: el indice
-- parcial cumple la misma funcion y un nombre distinto evita que dos objetos
-- con el mismo nombre y distinta definicion se cruzen en el planner.
ALTER TABLE resenias.review
    DROP CONSTRAINT IF EXISTS uk_review_user_professional;

CREATE UNIQUE INDEX IF NOT EXISTS ux_review_user_professional_viva
    ON resenias.review (user_id, professional_id) WHERE deleted_at IS NULL;

-- No se anaden indices parciales para los listados por usuario y por
-- profesional: V1 ya crea `idx_review_user_id` e `idx_review_professional_id`
-- sobre esas mismas columnas, y un indice parcial sobre la misma columna-leading
-- es un subconjunto estricto del que ya existe. El planificador usa el indice
-- completo y filtra. Anadirlo seria mantenimiento en cada escritura a cambio de
-- nada, asi que lo unico que queda aqui es el indice unico parcial de arriba,
-- que si carga con trabajo: es el que impide el duplicado en la base.
