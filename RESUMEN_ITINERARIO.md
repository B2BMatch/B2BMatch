# Itinerario de sesiones (A5→A7) — Stack B2BMatch (Rama_Backend)

## Contexto

Repositorio clonado (rama de trabajo) del equipo B2BMatch: un sistema B2B de ofertas de empleo con 6 microservicios Spring Boot sobre PostgreSQL. Pillar de trabajo 'Rama_Backend' (Windows, sin git), con la imagen base docker **develfa/boot4postgre** que provee el entorno Boot 4.1 + Jetty + PostgreSQL + Flyway.

Color llegamos aquí: el stack estuvo caído/roto durante días (sin arrancar desde cero). El objetivo de esta sesión (A5) fue lograrlo con un único `docker compose down -v` + `up --build` + segundo `up` sin romper nada, con las 6 migraciones Flyway aplicadas y sus seeds.


## Diagnóstico y causas raíz

1. **`usuarios unhealthy + tablas ausentes`**: Spring Boot **4.1 movió la auto-config de Flyway fuera de `spring-boot-autoconfigure`** a un módulo aparte (`spring-boot-flyway`). La stack (docker-compose con 6 microservicios) corría con poms que tenían `flyway-core`/`flyway-database-postgresql`, pero NO el módulo `spring-boot-flyway`, por lo que Flyway jamás se ejecutaba: no se creaban esquemas ni tablas, y Hibernate validaba contra nada → `missing table [app_user]`. Se verificó el fat-jar de una imagen previa: 0 clases `FlywayAutoConfiguration` (confirmación).
2. **Módulo `spring-cloud-starter-openfeign` sin versión (`Non-readable POM perfiles/pom.xml: input contained no data`)**: el pom de `perfiles` quedó en 0 bytes (edición previa mal aplicada). Además, en docker build `ofertas` incluía dependencias Feign/OpenAPI sin versión (springdoc) → `[ERROR] cannot find symbol: class OpenAPI`.
3. **`/actuator/health` devolvía 401 con `Token JWT requerido`**: el `SecurityConfig` de cada microservicio protegía `/actuator/**`; el `HEALTHCHECK` de los contenedores (curl al `/actuator/health`) recibía 401 → `unhealthy`. 
4. **`schema does not exist`**: (resuelto con `create-schemas`) Flyway debe tener `spring.flyway.create-schemas: true`, ya que el `default-schema` de cada microservicio es un schema separado (usuarios, perfiles, catalogo, ofertas, resenias, notificaciones) que no existía.


## Acciones

1. **6x `spring-boot-flyway` en los poms** de (usuarios, perfiles, catalogo, ofertas, resenias, notificaciones) junto con `flyway-core` + `flyway-database-postgresql`; se reconstruyeron poms de `perfiles` y `ofertas` (se eliminó el módulo openfeign no usado por código Java) y se añadió `springdoc-openapi-starter-webmvc-ui` (v3.0.3, igual que usuarios) en `ofertas` (su código usa `OpenApiConfig.java`).
2. **6x `spring.flyway.create-schemas: true`** en los `application.yaml` (usuarios, perfiles, catalogo, ofertas, resenias, notificaciones).
3. En módulos con seguridad JWT (`usuarios`, `perfiles`, `ofertas`, `catalogo`, `resenias`, `notificaciones`): agregado `requestMatchers(HttpMethod.GET/permitAll, "/actuator/health", "/actuator/health/**").permitAll()` para que el HEALTHCHECK de Docker funcione.
4. Validación local: `mvn clean compile` exit=0 para los 6 servicios; `docker compose up --build -d` exit=0; segundo `docker compose up -d` exit=0 sin romper (índice de validación de idempotencia).
5. `docker compose down -v` (borra voluntar volumes) + `up --build` desde cero: se verificó `flyway_schema_history` en los 6 schemas (V1+V2 para resenias/notificaciones; V1+V2+V3 en usuarios/perfiles/catalogo/ofertas), con seeds: 3 usuarios+4 roles, 1 perfil, 17 categorías, 3 ofertas, en los schemas correspondientes.


## Verificación

- `docker compose ps` → 8x Up/healthy (postgre, gateway + 6 microservicios).
- 6 schemas en b2bmatch con `flyway_schema_history` con V1 (init) + V2 (seed) aplicadas.
- `usuarios`: 3 app_user / 4 role; `perfiles`: 1 professional_profile; `catalogo`: 17 category; `ofertas`: 3 job_offer — cargados desde las migraciones V2 (seeds).
- Segundo `docker compose up -d` sin correr de nuevo migraciones ni romper estados (Flyway salta por versiones ya aplicadas). Todos los contenedores siguen healthy.
- **Criterio A5 cumplido**: stack reproducible desde cero (`down -v` + `up --build` + `up -d` repetido) sin fallos; migraciones y seeds aplicados.


## Notas / pendientes

- Los schemas `resenias` y `notificaciones` tienen 2 migraciones (V1+V2) y sus seeds V2 (0 filas de negocio: no tienen datos semilla que insertar o dependen de eventos).
- La tabla de historia `flyway_schema_history` quedó con esquemas por servicio (usuarios, perfiles, catalogo, ofertas, resenias, notificaciones).
- No se agregaron datos seed faltantes para `resenias`/`notificaciones` (se valida que las tablas queden creadas, no con datos obligatorios).
- Siguiente paso (A6): [asignar según siguiente ítem de la tarea].

---

## A6 — Rotacion de secretos compartidos (COMPLETADO + verificado E2E)

### Rotacion (4 claves en `BACKEND/.env`, valores criptograficos nuevos)

- `JWT_SECRET`: 48 B base64 (HS512) — nueva.
- `ADMIN_BOOTSTRAP_KEY`: 32 B base64 — nueva.
- `INTERNAL_SERVICE_KEY` / `OFFERS_GATEWAY_PASSPHRASE`: 32 B base64 — nueva.
- `POSTGRES_PASSWORD`: 24 chars alfanumerico + simbolo — nueva.

`BACKEND/.env` **NO esta trackeado** (verificado `git ls-files ":*.env"` vacio) y cubierto por `.gitignore` (`.env`, `.env.*`), asi que la rotacion no quedo en git.

### Aplicacion + idempotencia

1. `docker compose down -v` (elimina volumenes/flyway_history/seeds extras).
2. `docker compose up --build -d` - exit=0, rebuild de las 6 imagenes con el nuevo secreto.
3. `docker compose up -d` por segunda vez (powered by idempotencia) - exit=0, 8/8 contenedores Up+healthy, Flyway salta por versiones ya aplicadas.
4. `ps`: 8/8 healthy (postgre-db, gateway + 6 microservicios).

### Verificacion E2E via gateway :8080 (bateria con JWT real)

| Paso | Ruta | Resultado |
|---|---|---|
| E2E-1 | `POST /api/users/register` (email e2e temporal, rol PROFESSIONAL) | **201** — usuario creado (`id=4`) |
| E2E-2 | `POST /api/auth/login` (mismo email/pass real) | **200 + JWT** (HS512, 246 chars) |
| E2E-3 | `GET /api/users/{id}` con Bearer | **200** — datos del usuario nuevo |
| E2E-4 | `GET /api/catalogo/categories` con Bearer | **200** — 17 categorias (seed intacto) |
| E2E-5 | `GET /api/job-offers` con Bearer | **200** — ofertas publicas OK |

`POST /api/users/register` estaba devolviendo 401 al inicio: la linea de permitAll tenia `/api/auth/register` (ruta que el controller NO expone) en vez de `/api/users/register` (la real, AppUserController). Corregido el matcher en `usuarios SecurityConfig` y revalidado.

**Hallazgo no bloqueante (origen de A7)**: `GET /api/professional-profiles/me`, `GET /api/reviews/me` y `GET /api/notifications/me` **no son rutas del backend** — los endpoints reales son `/{id}`, `/user/{userId}`, `/professional/{professionalId}`. La bateria los llamo con `/me` por costumbre de otros stacks y el general `Exception` handler devolvia 500 en vez de 400. Se agrego handler de `MethodArgumentTypeMismatchException` en perfiles → ahora 400 limpio.

---

## A7 — Rutas `/me` (COMPLETADO + verificado E2E)

### Cambios de backend (3 microservicios)

| Microservicio | Ruta nueva | Nota |
|---|---|---|
| `perfiles` | `GET /api/professional-profiles/me` | Resuelve `userId` del JWT → `findByUserId(userId, userId, role)`; SecurityConfig exige token (matcher mas especifico antes del permitAll de la familia). |
| `resenias` | `GET /api/reviews/me` | Idem; SecurityConfig exige token antes del permitAll de `/api/reviews/**`. |
| `notificaciones` | `GET /api/notifications/me`, `GET /api/notifications/me/unread` | Ya cubierto por `anyRequest().authenticated()`. |

- Los paths literales `/me` tienen precedencia sobre `/{id}` en Spring PathPattern, asi que no se confunden con el GET por id.
- El frontend NO requiere cambios: sigue usando `/user/{userId}` desde `localStorage`; `/me` queda como comodidad y reduce la superficie de error (nadie puede pedir datos ajenos pasando un id).

### Verificacion E2E via gateway :8080

| Paso | Ruta | Resultado |
|---|---|---|
| `/me` con JWT de webtester (professional) | `GET /api/professional-profiles/me` | **200** — perfil completo (id=1, userId=2) |
| `/me` con JWT de companytester | `GET /api/reviews/me` | **200** — [] (sus reseñas) |
| `/me` con JWT de webtester | `GET /api/notifications/me` | **200** — [] |
| `/me/unread` con JWT de webtester | `GET /api/notifications/me/unread` | **200** — [] |
| `/me` sin token (los 3 services) | ... | **401** JSON (mensaje claro) |
| `professional-profiles/me` sin perfil profesional | companytester | **404** `Professional profile not found for user id: 3` |

Compilacion: `mvn -q compile -o` OK para perfiles, resenias y notificaciones.

---

## A8 — Ruta inexistente → 404 (fix de auditacion) (COMPLETADO + verificado E2E)

### Bug detectado en auditoria en vivo

Cualquier URL que no matchea un endpoint devolvia estados **incorrectos e inconsistentes**:

| Microservicio | Sin fix | Con fix |
|---|---|---|
| `usuarios` | **401** (sin handler generico; el error dispatch re-entraba a Spring Security) | **404** |
| `perfiles`, `ofertas` | **500** (`@ExceptionHandler(Exception.class)` capturaba `NoResourceFoundException`) | **404** |
| `catalogo`, `resenias`, `notificaciones` | **500** | **404** |

Causa: `org.springframework.web.servlet.resource.NoResourceFoundException` (Spring MVC 7) caia en el catch-all 500 de 5 servicios y en el `/error` re-protegido de usuarios.

### Fix

`@ExceptionHandler(NoResourceFoundException.class)` → **404** con `"La ruta solicitada no existe"` en los 6 `GlobalExceptionHandler`. **NO** se agrego handler generico a usuarios (se mantiene su alcance actual).

### Verificacion E2E (directo por puerto y via gateway)

- 6/6 microservicios: rutas inexistentes (p. ej. `/api/reviews/x/y`, `/api/job-applications/offer/1`, `/api/nosuch`) → **404** `La ruta solicitada no existe`.
- Regresion OK: login 200, listados publicos 200, `/me` 200, ownership 403, seed intacto (17 categorias, 3 ofertas, 4 servicios).
- Gateway: ruta inexistente con JWT → **404**; sin JWT → **401** (gate del `JwtAuthenticationFilter`, comportamiento esperado por diseno).
- Observacion menor (documentada, no corregida): bad credentials en login → **400** en vez de 401; el frontend lo maneja bien (excluye `/auth/login` del redirect global 401 en `api.js:26`).

---

## A9 — Reconciliacion de esquema: DATABASE/ vs migraciones Flyway (COMPLETADO)

### Hallazgo (durante analisis de flujo E2E)

- La BD en ejecucion se crea con las migraciones **Flyway** embebidas en cada microservicio (`BACKEND/<svc>/src/main/resources/db/migration`: V1 init + V2 seed). `docker-compose` **no** monta `DATABASE/` ni `BACKEND/init/` (carpeta vacia).
- Los SQL de `DATABASE/ofertas/` estaban desactualizados respecto del modelo desplegado: usaban `company_id` (job_offer), `professional_id` (application_table) y `customer_id` (quotation), pero el esquema real (Flyway V1) usa `user_id` (FK a `usuarios.app_user`) en las tres tablas, y el ownership se valida con esas columnas directas.
- README (item 12) afirmaba "ofertas sin columnas user_id; ownership por joins" → incorrecto para el esquema en runtime.

### Correccion

1. `DATABASE/ofertas/{12_create_job_offer,13_create_application,14_create_quotation,18_seed_data}.sql`: reescritos como **espejo fiel** de las migraciones Flyway V1/V2 (columnas `user_id` + FK, CHECK de estados, UNIQUE, indices) con nota de cabecera explicando que son referencia historica y que la fuente de verdad son las migraciones Flyway.
2. README item 12: corregido (ahora documenta `user_id` + verificaciones directas y cross-schema segun el caso) y aclarado que `DATABASE/` es historico/no ejecutado.
3. README seccion "Estructura": nota sobre `DATABASE/` como referencia historica.

Se verifico que los demas folders de `DATABASE/` (usuarios, catalogo) son consistentes con su Flyway; la divergencia era especifica de `ofertas` (por la unificacion del modelo de ofertas, item 2 del README).

**Sin cambios de runtime** (solo SQL de referencia + docs); no requirio rebuild ni afecta la BD desplegada.

---

## Correcciones de funcionalidad — Fases A y B (P5–P15)

### Introducción

Tras A5–A9 (operabilidad del stack), se auditaron y corrigieron funcionalidades core. Todo verificado con prueba lógica + prueba en vivo. **Sin commits ni push** (decisión del usuario); working tree limpio de trabajo pendiente.

### Fase A — módulo seguridades y cuentas

| ID | Corrección |
|---|---|
| **P5** | `usuarios SecurityConfig`: achicar el `permitAll` en `/api/users` (antes `"/api/users"`). Ahora la única ruta pública de la familia es `POST /api/users/register`. |
| **P6** | `AppUserController.update` valida ownership: solo el propio usuario (o ADMIN) puede modificar su cuenta → 403 en otro caso. |
| **P9** | `JobApplicationService.accept` (ofertas): operación atómica con lock pesimista (`JobOfferRepository.findByIdForUpdate`) y doble postulación 409. Evita doble ACCEPTED en carrera. |
| **P10** | `ProfessionalProfileController` (perfiles): `@Valid` en los cuerpos; 400 limpio para userId faltante; 404 si no existe el perfil. |
| **P11** | `ProfessionalProfileService.update`: si el `userId` indicado en el update difiere del dueño → 403. |
| **P19** *(vista en Fase C)* | Contrato de registro por `roleName`. |

### Fase B — comportamiento de negocio

| ID | Corrección |
|---|---|
| **P2a** | Duplicación de `mail` (usuarios) y `name` (categorías) → `DuplicateKeyException` mapeada a **409** en usuarios y catalogo. |
| **P2b** | Eliminación de un estado referenciado → `DataIntegrityViolationException` → **409**. |
| **P2c** | Crear un registro ya existente (perfiles: UNIQUE `user_id`) → **409** (antes 500). |
| **P2d** | Perfiles: `Id` en el body de creación → 400 "no puedes indicar un id". |
| **P17** | Baja de un usuario (DELETE `/api/users/{id}`, ADMIN): si era PROFESIONAL se desvincula su perfil (soft-delete) y se cancelan sus postulaciones PENDING y servicios; si era COMPANY se marcan como `CLOSED` sus ofertas y se **rechazan con `REJECTED`** las postulaciones PENDING de terceros sobre sus ofertas. Activación de la cascada correcta (antes inconsistente). |
| **P12** | Roles de pruebas: `ROLE_PROFESSIONAL` no puede crear reseñas (`ForbiddenException` en resenias). |
| **P15** | Nivel `ERROR` y stacktrace real en los catch-all (`log.error`) de los 6 microservicios; solo se expone el mensaje genérico al cliente. |

### Fase C — transacciones, habilidades y notificaciones (P16–P21)

| ID | Corrección |
|---|---|
| **P19** | `AppUserRegisterRequestDto` pide **`roleName`** (antes `roleId`): `AppUserService.register` resuelve por nombre (`UPPER` + trim); el frontend `Register.jsx` envía `roleName`. Enviar `roleId` → **400**. Registro como `ADMIN` → **400** (rol reservado). |
| **P21** | Alta de admin por bootstrap: `POST /api/users/admin-register` con header `X-Admin-Bootstrap-Key` (`ADMIN_BOOTSTRAP_KEY` en `.env`); clave validada en tiempo constante; clave inválida → **403**; `ForbiddenException` + handler en usuarios; ruta agregada a `PUBLIC_POST_PATHS` del gateway (primer admin sin token); nuevo `AppUserAdminRegisterRequestDto`. |
| **P16** | Bloqueo de login tras **5 intentos fallidos** (in-memory en `AppUserService`, con `pruneLoginAttempts` y tope `MAX_LOGIN_TRACKED_EMAILS=10_000`): 6º intento (aun con clave correcta) → **400** "Cuenta bloqueada temporalmente". El lock es por email: otros usuarios siguen autenticando. |
| **P18** | `ProfessionalSkillController` (catalogo): `GET /api/catalogo/professional-skills/{professionalId}` público (oculta skills DELETED y profesional DELETED); `POST /{id}/skills/{skillId}` y `DELETE` solo dueño o ADMIN (**403**); profesional inexistente/skill inexistente → **404**; duplicado → **409**. |
| **P7** | `ReviewService.hasCompletedTransaction`: reseña de COMPANY requiere postulación **ACCEPTED** del profesional en oferta propia **o** cotización ACCEPTED donde la empresa es requester; CUSTOMER requiere cotización ACCEPTED sobre servicio del profesional; **ADMIN bypass**. Check previo al de duplicados. |
| **P8** | `NotificationClient` (RestClient con `X-Internal-Service-Key`, timeouts 2s/3s, best-effort) en ofertas y resenias; eventos: postulación creada→empresa, aceptada/rechazada→profesional, cotización recibida→profesional, aceptada/rechazada→cliente, reseña→profesional. `INTERNAL_SERVICE_KEY` añadido a `resenias` en compose. |

Archivos clave de Fase C: `usuarios/{AppUserService, AppUserRegisterRequestDto, AppUserAdminRegisterRequestDto, AppUserController, ForbiddenException, GlobalExceptionHandler}`, `gateway/JwtAuthenticationFilter` (`PUBLIC_POST_PATHS`), `catalogo/ProfessionalSkillController`, `resenias/{ReviewService, ReviewCreateRequestDto, ReviewController}`, `ofertas/config/NotificationClient`, `resenias/config/NotificationClient`, `docker-compose.yml`.

**Sin migraciones nuevas en Fase C.** Compilación local OK (usuarios, ofertas, resenias, catalogo, gateway con `mvn -o compile`) y `npm run build` del frontend OK.

---

## Prueba en vivo del stack (2026-09-24) — RESULTADO: TODO VERDE

### Arranque

`docker compose up --build -d` → 8/8 contenedores (postgre-db, gateway + 6 microservicios) Up, 7 healthy + gateway Up. Flyway aplicó `catalogo V3` (soft delete) sobre la BD existente.

### Batería E2E via gateway `:8080`

| Ítem | Prueba | Resultado |
|---|---|---|
| Regresión | login demo (companytester, webtester, admin) | **200** c/u; categorías seed con `status=ACTIVE` |
| **P19** | register con `roleName` (PROFESSIONAL y COMPANY) | **201** |
| **P19** | register con `roleId` (contrato viejo) | **400** |
| **P19** | register con `roleName=ADMIN` | **400** |
| **P21** | `admin-register` clave correcta | **201** (rol ADMIN) |
| **P21** | `admin-register` clave incorrecta | **403** |
| **P16** | 5 logins fallidos + 6º con clave correcta | **400** "Cuenta bloqueada temporalmente"; webtester sigue **200** (lock por email) |
| **P18** | assign owner → duplicado → ajeno → skill inexistente → prof inexistente → GET → DELETE → GET | **201 / 409 / 403 / 404 / 404 / [skill] / 204 / []** |
| **P7** | reseña empresa sin transacción previa | **400** "Necesitas una transacción previa completada" |
| **P7** | reseña ADMIN sin transacción (bypass) | **201** |
| **P7** | rating fuera de rango (DTO) | **400** |
| **P7** | reseña duplicada mismo autor+profesional | **400** |
| **P7** | flujo real: postulación → `PATCH accept` → reseña | postulación **201** → accept **200** → reseña **201** |
| **P8** | filas en `notificaciones.notification` | 4 eventos verificados: reseña→webtester, postulación→empresa, aceptación→profesional, reseña→profesional |

### Defectos encontrados durante la prueba y corregidos

1. **`ProfessionalSkillController.assign` (catalogo)**: `SELECT EXISTS(...)` con `ResultSetExtractor` sin `rs.next()` → 500 `ResultSet not positioned properly`. Fix: `rs -> rs.next() && rs.getBoolean(1)`. Además el handler `DataAccessException` de catalogo no logueaba → se añadió `log.error` con stacktrace.
2. **`ReviewController.createReview` (resenias)**: `@Valid @RequestBody Review` rechazaba siempre el body porque la entidad exige `userId`, pero el servicio lo setea desde el token → creación de reseñas caía en 500. Fix: nuevo `ReviewCreateRequestDto` (professionalId, rating, comment) + conversión en el controller; `MethodArgumentNotValidException` → **400** en el handler de resenias.
3. **`ReviewService.hasCompletedTransaction` (resenias)**: mismo bug de `rs.next()` en el `SELECT EXISTS` (dejaba la reseña 500). Fix: `rs.next() && rs.getBoolean(1)`.

### Demos creadas como evidencia (BD)

- Usuarios: fasec_prof (id 8, PROFESSIONAL), fasec_comp (id 9, COMPANY), bootstrap_admin (id 10, ADMIN), fasec_lock (id 11, COMPANY).
- `perfiles.professional_profile` id 3 (fasec_prof, ACTIVE).
- Postulación ACCEPTED de fasec_prof a oferta 1 del seed (companytester la aceptó; oferta quedó CLOSED).
- Reseñas: webtester (admin bypass) y fasec_prof (post-aceptación).

### Estado final

- 8/8 contenedores healthy. Working tree con TODO el trabajo sin commitear y SIN push. `.env` no trackeado (gitignore).
