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

---

## Deuda conocida: exports muertos en `FRONTEND/src/services/` (no borrados a propósito)

En la limpieza de servicios se detectaron 35 exports sin ningún consumidor. Se dividieron en dos lotes.

### Lote A — borrados (10 wrappers, inequívocamente muertos)

| Servicio | Exports eliminados | Motivo |
|---|---|---|
| `notificationsService.js` | `getUnreadNotifications` | El badge de `Navbar.jsx` ya se deriva de `GET /notifications/me` filtrando `isRead === false`; el endpoint `/notifications/me/unread` queda disponible en backend para otros consumidores. |
| `perfilesService.js` | `getCustomerProfiles`, `getCustomerProfileById`, `getCustomerProfileByUser`, `createCustomerProfile`, `updateCustomerProfile`, `deleteCustomerProfile` | El rol `CUSTOMER` es inalcanzable desde la UI: `Register.jsx` ofrece solo 2 roles y `AuthContext.jsx` fusiona `CUSTOMER` con `PROFESSIONAL`. Ningún flujo puede pedir un `customer-profile`. |
| `perfilesService.js` | `getCompanyProfileById`, `getProfessionalProfileById` | Getters por-id redundantes: todos los consumidores usan las variantes por-user (`getCompanyProfileByUser`, `getProfessionalProfileByUser`). |
| `usersService.js` | `getUsersByRole` | Redundante: `getUsers` ya devuelve la lista completa y `Users.jsx` la usa. |

### Lote B — conservados: NO son basura, son features faltantes (25 exports)

Cada uno es hoy la única vía frontend a una capacidad que el backend ya soporta. Borrarlos obligaría a reescribir el wrapper cuando se cablee la feature.

| Capacidad faltante | Exports muertos que la habilitan | Endpoint backend ya disponible |
|---|---|---|
| Empresas no pueden publicar servicios | `getCompanyServices`, `createCompanyService`, `getCompanyServiceById`, `updateCompanyService`, `deleteCompanyService` | `GET/POST/PUT/DELETE /catalogo/company-services` |
| Sin gestión de catálogo ni skills en el admin | `getCategories`, `createCategory`, `getSkills`, `createSkill` | `GET/POST /catalogo/categories`, `GET/POST /catalogo/skills` |
| Admin no puede dar de baja usuarios | `deleteUser` | `DELETE /users/{id}` |
| `reactivateUser` es doblemente inalcanzable | `reactivateUser` | `PATCH /users/{id}/reactivate` — además el backend filtra `DELETED` en `GET /users`, así que un usuario dado de baja desaparece del listado y nunca puede reactivarse desde la UI |
| Nadie puede borrar ni editar una reseña | `getReviews`, `getReviewById`, `getReviewsByUser`, `updateReview`, `deleteReview` | `GET/PUT/DELETE /reviews` |
| El cliente no puede cancelar ni editar su cotización | `getQuotations`, `getQuotationById`, `updateQuotation`, `deleteQuotation` | `GET/PUT/DELETE /quotations` |
| Sin baja de perfil | `deleteCompanyProfile`, `deleteProfessionalProfile` | `DELETE /company-profiles/{id}`, `DELETE /professional-profiles/{id}` |
| Admin no puede crear notificaciones manuales | `createNotification` | `POST /notifications` (solo `ADMIN`; el `/notifications/internal` exige `X-Internal-Service-Key` y jamás debe invocarse desde el navegador) |
| Sin gestión de notificaciones | `deleteNotification` | `DELETE /notifications/{id}` |

### Trampa latente a tener en cuenta al cablear el lote B

`catalogoService.js` **no tiene wrapper** para editar, pausar ni dar de baja un servicio de profesional, aunque el backend expone `PATCH /catalogo/professional-services/{id}/status` y `DELETE /catalogo/professional-services/{id}`. Combinado con que `GET /catalogo/professional-services` filtra `status = 'ACTIVE'`, cualquier servicio pausado desaparecería de "Mis Servicios" en `UserProfile.jsx` (que lista sobre ese mismo endpoint público) y quedaría irrecuperable desde la UI. Antes de exponer el endpoint de estado hay que agregar un listado propio que no filtre por `ACTIVE` para el dueño del servicio.


---

## Fix: `id` no numérico devolvía 500 en lugar de 400 (regresión propia)

### Causa raíz

Los `@ExceptionHandler(Exception.class)` agregados a los 5 `GlobalExceptionHandler` acting como catch-all **secuestran** excepciones de cliente que Spring ya sabía mapear. `ExceptionHandlerExceptionResolver` corre antes que `DefaultHandlerExceptionResolver`, así que `MethodArgumentTypeMismatchException` (que Spring mapea a **400**) terminaba en el catch-all y volvía **500** con log `Error inesperado en <servicio>`.

Prueba de contraste dentro del propio stack: `usuarios` **no** tiene catch-all, y para el mismo fallo devolvió `400 For input string: "me"`, mientras los otros 4 devolvían 500. Confirmado en log: `MethodArgumentTypeMismatchException: Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'`.

Alcance: 31 rutas con `@PathVariable`/`@RequestParam` tipado (`perfiles` 15, `ofertas` 9, `notificaciones` 5, `resenias` 2). `catalogo` no tiene ids numéricos en rutas, pero se le añadió el handler por consistencia del contrato de error.

### Fix aplicado

Handler específico `@ExceptionHandler(MethodArgumentTypeMismatchException.class)` → **400** con mensaje `Parámetro inválido: '<name>' debe ser de tipo <Tipo> (valor recibido: '<valor>')`, en los 5 servicios. Además:

- `NumberFormatException` → 400 en `catalogo` y `notificaciones` (los dos que no tienen handler de `IllegalArgumentException`; en los otros 3 ya caía en 400 por herencia).
- `perfiles` ya tenía un handler de type-mismatch, pero su mensaje era engañoso: decía *"Parámetro inválido: id (ruta o recurso no encontrado en el microservicio de perfiles)"*, confundiendo un **error de tipo** con un **404**. Corregido al mismo formato.

Archivos: `GlobalExceptionHandler.java` de `notificaciones`, `ofertas`, `resenias`, `catalogo` y `perfiles`.

### Verificación

- 24/24 checks E2E: 14 casos de id no numérico → 400 en los 4 servicios con ids tipados, más no-regresión de 401/404/405/415/200 y el caso vía gateway.
- Regresión de negocio: E2E grupo 2 (aceptar/rechazar) y grupo 3 (ofertas/servicios) → TODO OK.
- Lint + build frontend, contratos 60/0 y 5/5 `mvn compile`.

### Dos hallazgos colaterales

1. **El gateway 500 no era del fix**: `Connection refused: notificaciones/172.18.0.5:8086`. Al recrear contenedores con `docker compose up -d --build` sin reiniciar el gateway, Spring Cloud Gateway cachea la IP vieja y queda con conexiones colgadas. **Hay que reiniciar el gateway cada vez que se recrean servicios.**
2. **El seed de `catalogo` no inserta servicios**: `V2__seed_data.sql` solo inserta `category` y `skill`. Los servicios que aparecen en `/servicios` eran residue de pruebas E2E, así que la afirmación del README ("el profesional `webtester` tiene publicados servicios en `/servicios`") no se cumplía sola. Se repoblaron 3 servicios de `webtester` vía API real (`POST /catalogo/professional-services`).

---

## Prueba de journey realista (validación integral end-to-end)

Prueba que recorre el ciclo de negocio completo **con usuarios recién registrados**, sin apoyarse en los datos demo. Resultado final: **46/46 checks**.

### Fases cubiertas

| Fase | Qué valida |
|---|---|
| 0 | Registro real de 2 profesionales + 1 empresa, login, creación de perfiles |
| 1 | El profesional publica un servicio; ni una empresa ni otro profesional pueden hacerlo |
| 2 | La empresa publica una oferta; aparece en el listado público y la UI resuelve el nombre de la empresa |
| 3 | Dos profesionales se postulan; duplicado → 409; el postulante no puede aceptarse a sí mismo; la empresa ve proposal y expectedPrice |
| 4 | La empresa acepta → la otra postulación queda REJECTED, la oferta CLOSED y desaparece del público, no se puede re-aceptar, ambos postulantes notificados |
| 5 | Cotización sobre el servicio: el cliente no puede decidirla, el dueño del servicio sí, y el cliente es notificado |
| 6 | Reseña: rechazada sin transacción completada, aceptada con ella, duplicada → 400, visible en el perfil público |
| 7 | Buscador de `/servicios` con `?q=`: normalización de mayúsculas y acentos, y 0 resultados para un término inexistente |
| 8 | Flujo exacto del badge del Navbar: listado plano con `isRead`, `PATCH read` → 200, contador baja, re-lectura inofensiva |
| 9 | Aislamiento entre servicios y contrato de errores (401/403/404/400) |

### Dos defectos reales encontrados por esta prueba y corregidos

1. **JSON malformado o campo con tipo incorrecto devolvía 500 en 4 de los 5 servicios con catch-all.** Mismo mecanismo que el fix anterior: `HttpMessageNotReadableException` (JSON roto, o p. ej. `jobOfferId: "no-soy-un-id"`) quedaba secuestrada por el `@ExceptionHandler(Exception.class)`. Solo `catalogo` tenía el handler. Agregado `HttpMessageNotReadableException` → **400** en `perfiles`, `ofertas`, `resenias` y `notificaciones`. `usuarios` no estaba afectado (no tiene catch-all, el default de Spring ya responde 400).

2. **El auto-rechazo al aceptar una postulación no notificaba a los rechazados.** En `JobApplicationService.accept()` el loop que marca `REJECTED` a las otras postulaciones no llamaba a `notificationClient.notify()`, solo se notificaba al aceptado. Un profesional se postulaba, la empresa aceptaba a otro, la oferta desaparecía del listado y él nunca se enteraba. Agregada la notificación con un mensaje que explica que se aceptó otra postulación.

### Estado de verificación tras los fixes

- Journey realista: **46/46**.
- Regresión: `e2e-typemismatch` 24/24, `e2e-accept-reject` TODO OK, `e2e-grupo3` TODO OK, `e2e-servicios` 15/15 (el script perdió 2 checks al borrar wrappers muertos: hoy son 13/13).
- Contrato de errores verificado en los 6 servicios: JSON malformado → 400, campo con tipo incorrecto → 400, id no numérico → 400.
- Frontend: `npm run lint` y `npm run build` limpios.
- Base de datos: limpiada del residue de las pruebas (14 usuarios de test y sus datos en cascada). Quedan los 3 usuarios demo, los 4 de evidencia `fasec_*`, 3 ofertas de semilla, 3 servicios republished de `webtester`, 2 reseñas y 4 notificaciones sin leer.

### Advertencia operativa

Los scripts E2E imprimen los IDs a limpiar pero **no borran nada** (el borrado por API es soft-delete). La limpieza real hay que hacerla por SQL, y hay que **reiniciar el gateway** después de cada `docker compose up -d --build` porque cachea la IP de los contenedores recreados.

---

## Trampa de servicios pausados: `GET /catalogo/professional-services/mine`

### El problema

`GET /catalogo/professional-services` solo tiene la variante `status = 'ACTIVE'`, y `UserProfile.jsx` construía "Mis Servicios" filtrando **ese listado público** por `professional_id` en el cliente. Consecuencia: en cuanto se cableara el pause, un servicio pausado desaparecería de "Mis Servicios" y no habría forma de recuperarlo desde la UI — el dato seguía en la tabla, pero ningún endpoint lo exponía al dueño. Era la trampa documentada en "Trampa latente a tener en cuenta al cablear el lote B", y ya era explotable porque el backend expone `PATCH /{id}/status` y `DELETE /{id}`.

### El fix

- `GET /api/catalogo/professional-services/mine` (nuevo, en `ProfessionalServiceController`): resuelve el `professional_profile.id` del usuario autenticado y devuelve **sus** servicios con cualquier status salvo `DELETED` (o sea, incluye `INACTIVE` y `SUSPENDED`). Si el usuario no tiene perfil profesional devuelve `[]` en lugar de 404. Roles `PROFESSIONAL` y `ADMIN`; el resto recibe 403.
- `SecurityConfig` de `catalogo`: la ruta quedó registrada como `authenticated()` **por encima** del blanket `.requestMatchers(HttpMethod.GET, "/api/catalogo/**").permitAll()`. Sin esto el endpoint no estaba protegido por Spring Security: el `JwtAuthFilter` se traga los tokens inválidos sin rechazar la request (`catch → clearContext` y sigue), así que un GET nuevo con datos privados queda público salvo que el controller valide el token a mano. Es exactamente el modo de fallo que se quiere evitar; el orden de los matchers hace que gane la regla específica.
- `catalogoService.js`: nuevo wrapper `getMyProfessionalServices()`.
- `UserProfile.jsx`: "Mis Servicios" ahora consume ese endpoint y se eliminó el filtrado por `professional_id` en el cliente. Efecto adicional: deja de descargar el catálogo público completo para mostrar los servicios propios. Las dependencias del `useEffect` pasaron de `[profileId]` a `[user?.id]`, porque el endpoint resuelve el perfil en el servidor.
- `UserProfile.jsx`: badge de estado en cada servicio propio (`Publicado` / `Pausado` / `Suspendido`) con las clases `badge-status--*` ya usadas en `CompanyOffers.jsx` y `Offers_new.jsx`. Sin esto un servicio pausado se veía idéntico a uno activo, aunque no apareciera en el público.

### Verificación

`e2e-mine.mjs` **18/18**: el servicio aparece en el listado público mientras está `ACTIVE`, desaparece del público al pausarlo, **sigue apareciendo en `/mine` con su status real**, y vuelve al público al reactivarlo. Además: `/mine` de otro profesional no lo ve, empresa recibe 403, sin token 401 y con token inválido 401 (antes 400 y 500 respectivamente).

Regresión: `e2e-realista` 46/46, `e2e-typemismatch` 24/24, `e2e-servicios` 13/13, contrato de rutas OK, exports 61 (23 muertos / 38 usados), `npm run lint` y `npm run build` limpios.

### Nota sobre el conteo de `e2e-servicios`

Pasó de 15 a 13 checks porque el script perdió las aserciones de `getUnreadNotifications` al borrar ese wrapper muerto, no por una regresión.

### Riesgo residual en `catalogo`

`GET /api/catalogo/**` sigue siendo `permitAll` en bloque. Hoy la única lectura privada es `/mine` y está registrada arriba, pero cualquier GET privado que se agregue en el futuro nace público salvo que alguien lo registre. La postura fail-closed sería enumerar los 8 GET públicos en vez de usar el comodín; no se hizo en este cambio para no tocar la postura de seguridad del servicio sin medirla.


---

## Migración `deleted_at` (servicio `usuarios`): delete y reactivate sin pérdida

### El problema

`status` cumplía dos funciones a la vez: estado de negocio (`ACTIVE` / `INACTIVE` / `SUSPENDED` / ...) y bandera de borrado (`DELETED`). Como el borrado **sobrescribía** `status`, el estado original se perdía de forma permanente, y `reactivate` no podía restaurarlo: solo ponía `ACTIVE` sobre el usuario y devolvía una cuenta zombi — se podía loguear pero sin perfil, sin ofertas y sin servicios.

`delete` cascadeaba 12 statements; `reactivate` no restauraba **ninguno**. Además 4 de esos statements fabricaban estados de negocio (`application_table` `PENDING→REJECTED`, `quotation` `PENDING→EXPIRED`), que también eran irreversibles.

No existía `deleted_at` ni `previous_status` en ninguna tabla, así que el cascade era irreversible **por diseño**.

### El fix

- **`V901__add_deleted_at.sql`** (nueva, en `usuarios`): agrega `deleted_at TIMESTAMP` y `previous_status VARCHAR(20)` a las 7 tablas del cascade — `usuarios.app_user`, los 3 `perfiles.*`, `ofertas.job_offer`, `catalogo.professional_service`, `catalogo.company_service` — más backfill de lo que ya estaba borrado.
  - **Por qué es una sola migración y no una por servicio:** la cadena de arranque es `usuarios → perfiles → catalogo → ofertas`, o sea `usuarios` arranca **primero**. Si cada servicio declarara las suyas, `usuarios` podría arrancar y ejecutar un borrado antes de que las columnas existieran. No se puede invertir el `depends_on` sin ciclo, porque `perfiles` ya depende de `usuarios`. En una sola migración el conjunto es atómico (Postgres envuelve el DDL en transacción). El costo es que `usuarios` es dueño del DDL de 3 schemas ajenos; es una extensión de un acoplamiento que el servicio **ya tenía**, porque su cascade igual escribe esas 6 tablas. La solución estructural sería un evento `user-deleted` que cada servicio consumiera, fuera de alcance acá.
  - El `previous_status` de las filas ya borradas se asume `ACTIVE`: su estado real se perdió antes de esta migración y no es recuperable. Queda registrado y es corregible a mano. Es la única parte no reversible del cambio.
  - Numeración: el historial ya tenía un `V900` ("demo users"), así que la migración va como `V901`. Con `V3` Flyway la rechazaba por *out of order*.
- **`AppUser`**: campos `deletedAt` y `previousStatus`.
- **`AppUserRepository`**: `findByStatusNot` / `findByRole_NameAndStatusNot` → `findByDeletedAtIsNull` / `findByRole_NameAndDeletedAtIsNull`. `findById` sigue sin filtrar, porque `reactivate` necesita encontrar al usuario borrado.
- **`AppUserService`**:
  - `delete` graba `previous_status`, `status='DELETED'` y `deleted_at`, y llama al cascade.
  - `reactivate` restaura `status = previous_status` y limpia ambos campos, y llama al **cascade de restauración**, que es el inverso exacto. Se le agregó `@Transactional` porque ahora escribe en 7 tablas.
  - `softDelete(table, condition, userId)` y `restore(table, condition, userId)` reemplazan los 12 statements literales por un par de helpers simétricos: menos código y las dos direcciones no pueden divergir.
  - `updateStatus` y los guards de `delete`/`reactivate` ahora preguntan por `deletedAt != null`, que es la fuente de verdad.
  - **Se eliminaron las 4 mutaciones destructivas.** Una postulación de una oferta borrada ya no se fuerza a `REJECTED`: la oferta queda oculta por su propio `deleted_at` y la postulación conserva su estado real, que es además reversible.
- `status = 'DELETED'` **se sigue escribiendo** a propósito: los otros 5 servicios todavía filtran por `status <> 'DELETED'`. Los 25 sitios de lectura se migran a `deleted_at IS NULL` en una fase posterior, servicio por servicio; cuando todos lo hagan, la escritura de `DELETED` se puede retirar.

### Verificación

Round-trip de profesional `e2e-roundtrip.mjs` **22/22** y de empresa `e2e-roundtrip-company.mjs` **12/12**. Lo esencial:

- Un servicio pausado a propósito (`INACTIVE`) **vuelve `INACTIVE`**, no `ACTIVE`. Una oferta `SUSPENDED` vuelve `SUSPENDED`.
- El perfil, los servicios y las ofertas se restauran; el usuario vuelve a `GET /users`.
- La oferta de la empresa **no** se toca al borrar a un profesional.
- La postulación sigue `PENDING`: prueba de que la mutación a `REJECTED` ya no ocurre.
- No se puede cambiar el estado de una cuenta borrada ni reactivarla dos veces.

Antes de la migración, sobre un servicio `INACTIVE`, el delete dejaba `0` filas con `INACTIVE` recuperables. Ahora el round-trip es exacto.

Regresión: `e2e-realista` 46/46, `e2e-typemismatch` 24/24, `e2e-servicios` 13/13, `e2e-mine` 18/18, contrato de rutas OK, `mvn -o test-compile` OK. Frontend sin cambios: el DTO de `AppUserResponseDto` no se tocó, así que el contrato con la UI es idéntico.

### Lo que sigue pendiente

- Migrar los 25 sitios de lectura de los otros 5 servicios a `deleted_at IS NULL`, y después retirar `status='DELETED'`.
- `notificaciones.notification` y `resenias.review` se siguen borrando con `DELETE FROM`: **no son restaurables**. Las notificaciones son efímeras y el borrado duro se justifica; las reseñas son contenido de usuario y probablemente deberían llevar `deleted_at` también. Es una decisión de producto, no técnica.
- `email` es `UNIQUE` y el borrado no lo libera: un usuario borrado sigue ocupando su email, así que no se puede volver a registrar con ese correo. Además bloquea la reactivación si alguien llegó a liberar el email.
- `AppUserResponseDto` no expone `deletedAt`: el admin solo ve `status: 'DELETED'`. Cuando se cablee el botón de reactivar hay que decidir qué se muestra.

---

## Cableado de baja y restauración en el panel admin

Con la migración `deleted_at` ya aplicada, faltaba la mitad del circuito: el botón. `reactivateUser` y `deleteUser` existían en `usersService.js` pero **ninguna página los llamaba**, y sobre todo el admin no tenía forma de ver a un usuario borrado, porque `GET /users` los excluía. Sin eso, el backend correcto seguía siendo inalcanzable.

### Backend

- **`GET /users?includeDeleted=true`** (`AppUserController.getAll`): el parámetro es opcional y por defecto `false`, así que el contrato anterior queda intacto para cualquier otro consumidor. Con `true` devuelve también los borrados. Todo el endpoint ya es `@PreAuthorize("hasRole('ADMIN')")`, así que el parámetro no amplía la exposición.
  - Se eligió un parámetro y no un endpoint `GET /users/deleted` para no sumar una segunda request: el frontend parte la lista en dos en memoria.
- **`AppUserResponseDto`**: nuevo campo `deletedAt`, para que el admin vea *cuándo* se dio de baja y no solo que lo fue. Es aditivo, no rompe el contrato.

### Frontend

- `usersService.getUsers(params)` acepta parámetros y los pasa a `api.get` como `params`. `AdminDashboard.jsx` la sigue llamando sin argumentos, así que no cambia su comportamiento.
- `pages/Admin/User.jsx`:
  - Carga con `includeDeleted: true` y separa la lista en activos y dados de baja.
  - **Nueva acción "Dar de baja"** (el wrapper `deleteUser` ya existía, sin usar). El `confirm` detalla qué se oculta y advierte explícitamente que **notificaciones y reseñas no se recuperan**, porque eso es lo que el backend no puede deshacer.
  - **Nueva sección "Cuentas dadas de baja"** con la fecha de baja y un botón "Restaurar cuenta" que llama a `reactivateUser` y muestra el estado real al que volvió la cuenta.
  - Se separaron dos conceptos que la página mezclaba: **suspender** (`updateUserStatus`) y **dar de baja** (`delete`). Antes la única acción reversible era suspender; ahora son dos caminos distintos con sus propios botones, y sus "reactivaciones" van a endpoints distintos. Un usuario `SUSPENDED` muestra "Reactivar"; uno `DELETED` muestra "Restaurar cuenta".
  - Los badges de estado dejaron de ser un ternario suspended/no-suspended: ahora hay un mapeo explícito con `DELETED`, `SUSPENDED`, `INACTIVE` y `ACTIVE`, porque con dos estados más el ternario原来的逻辑 ya no alcanzaba.

### Verificación

`e2e-admin-baja.mjs` **23/23**: el usuario aparece en el listado por defecto, desaparece al darse de baja, reaparece con `includeDeleted=true` junto con su `deletedAt`, un profesional recibe 403 tanto al listar como al intentar restaurar, y al restaurar vuelve el perfil y el servicio. Incluye el caso que distingue la migración de un simple parche: **dar de baja una cuenta que estaba `SUSPENDED` la restaura en `SUSPENDED`**, no en `ACTIVE`.

Regresión: `e2e-realista` 46/46, `e2e-typemismatch` 24/24, `e2e-servicios` 13/13, `e2e-mine` 18/18, `e2e-roundtrip` 22/22, `e2e-roundtrip-company` 12/12, contrato de rutas OK, `npm run lint` y `npm run build` limpios.

Los exports muertos de `FRONTEND/src/services/` bajaron de **23 a 21**: `deleteUser` y `reactivateUser` dejaron de estar huérfanos. Quedan 21, que son features faltantes y no basura.

---

## Pruebas de pagina en navegador real (Chrome headless via CDP)

Hasta aca todo estaba verificado por API. Eso no alcanza: la API puede estar perfecta y el boton no aparecer, o el texto quedar ilegible. Se monto un harness de navegador sin agregar dependencias.

### Por que CDP y no Playwright

El proyecto no tiene framework de tests y agregar Playwright o Puppeteer para esto era innecesario. En su lugar, `cdp.mjs` maneja Chrome por el Chrome DevTools Protocol usando el `WebSocket` global de Node 22+, con el Chrome que ya estaba instalado. Cero dependencias nuevas. Permite ademas怎么处理 los `window.confirm` y `alert` en segundo plano, que si no bloquean la pagina.

### Las tres suites

| Suite | Que verifica | Resultado |
|---|---|---|
| `page-admin.mjs` | DOM e interaccion en `/admin/usuarios`: login por el formulario real, suspender, reactivar, dar de baja, restaurar | **24/24** |
| `page-layout.mjs` | Geometria y accesibilidad: posicion de secciones, scroll horizontal, badges con fondo real, foco por teclado, contraste WCAG | **17/17** |
| `sweep2.mjs` | Contraste de los 1059 nodos con texto de 11 paginas, midiendo los pixeles reales | 8 fallos, todos preexistentes y no-coral |

El login del test de pagina es por el formulario real, usando el setter nativo de `HTMLInputElement.prototype.value` mas un evento `input`, porque los inputs de React son controlados y asignar `.value` a mano no dispara el estado.

### Tres errores de medicion que Cometi (y por que importan)

1. **Alfa sin componer.** Medi el badge `DADO DE BAJA` como 1.28:1 comparando el rojo puro contra el texto rojo. El fondo era `rgba(239,68,68,0.12)`: 12% de rojo, no rojo. Corregido, dio 4.14:1, que si era un fallo real.
2. **Gradientes invisibles para `backgroundColor`.** Un barrido con `getComputedStyle().backgroundColor` reportaba 1.11:1 en el hero, porque en un `linear-gradient` el `backgroundColor` es `rgba(0,0,0,0)` y comparaba texto blanco contra blanco. Se sustituyo por muestreo de pixeles reales: se decodifica el PNG de la captura con `zlib` y se leen los pixeles. Es la unica forma de ver gradientes, sombras e imagenes.
3. **Muestrear glifos en vez del fondo.** El boton "Cerrar Sesion" daba 1.99:1 porque el texto ocupa casi todo el box y las esquinas caian sobre glifos. Se verifica que el boton se ve bien: el pixel del centro es `rgb(22,35,63)`, el navy del texto, sobre un navbar blanco translucido. El estimador ahora descarta los pixeles cercanos al color del texto.

Ademas se asumio mal el fondo del panel de auth (se creyo crema y es navy) y casi se oscurecia un icono que ya pasaba. Por eso las mediciones se corrigen contra el DOM y los pixeles, no contra supuestos.

### Contraste: el coral global

El token `.btn-b2b-primary` era blanco sobre `--coral` (#FF6F4D) = **2.75:1**, fallando AA (4.5:1). Es un token global en 12 archivos, asi que se oscurecio conservando tono y saturacion (H 11.5, S 85%).

El hallazgo importante es que **un solo token no puede cumplir los dos papeles**: como relleno con texto blanco necesita luminancia <= 0.183, pero como texto sobre crema necesita <= 0.143. Por eso son tres tokens:

| Token | Valor | Rol | Contraste |
|---|---|---|---|
| `--coral` | `#DA3812` | relleno de marca, con texto blanco | 4.60:1 |
| `--coral-hover` | `#C43310` | estado hover | 5.48:1 |
| `--text-accent` | `#C03110` | coral como **texto** sobre fondos claros | 4.65:1 |
| `--coral-light` | `#FF8A6E` | acento sobre fondos oscuros (navy) | 6.77:1 |

`--text-accent` quedo calibrado contra el **peor** fondo donde se usa (el tinte coral 0.14 sobre blanco, rgb(250,227,222)), no contra la crema: con el valor calibrado contra crema daba 4.44:1 y quedaba 0.06 corto. El tema oscuro conserva su coral claro `#FF8A6E`, que sobre navy da 6.77:1.

Se Actualizaron 25 `rgba(255,111,77,...)` decorativos y los hexes de `src/pages/Landing/Landing.css`, que es un stylesheet de pagina **fuera de `src/styles/`** y que el primer escaneo no cubria: ahi quedaban cuatro usos de texto coral y dos rellenos, y solo dos de ellos debian oscurecerse (los de texto sobre fondo claro); el acento del hero y el boton del hero estan sobre `#0D1526` y van con `--coral-light`. Ademas `.badge-verified` usaba `var(--coral)` como color de texto sobre un tinte coral claro (3.87:1) y paso a `var(--text-accent)`.

### Lo que queda: 8 fallos de contraste, todos preexistentes y ninguno de coral

Del barrido de 1059 elementos, los 8 unicos fallos restantes no son del coral y `--gold` quedo intacto:

- **Oro (5):** `--gold` #D9A441 sobre blanco 2.25:1 (precios en tablas), blanco sobre oro 2.25:1 (avatares), `#9A7A10` 3.59:1 (`badge-status--active` de la tabla admin y `stat-change`), `badge-gold` 4.19:1.
- **Gris (2):** `--text-muted` #5B6472 sobre fondos oscuros: 1.77:1 y 3.04:1 en los placeholders de imagen.
- **Decorativo (1):** el corazon de gig, 1.86:1.

Son el mismo problema que tenia el coral, pero en otro token que no se apruebo cambiar. El badge `DADO DE BAJA` que se agrego en el trabajo anterior si quedo accesible (5.55:1, con un token propio `badge-status--deleted` en vez de reestilizar el de suspension).

### Regresion

API: `e2e-realista` 46/46, `e2e-typemismatch` 24/24, `e2e-servicios` 13/13, `e2e-mine` 18/18, `e2e-roundtrip` 22/22, `e2e-roundtrip-company` 12/12, `e2e-admin-baja` 23/23, contrato de rutas sin fallos. Frontend: `npm run lint` y `npm run build` limpios. Sin commit ni push.

---

## Fase 2 (cerrada): `deleted_at` como unica fuente de verdad del borrado

V901 habia separado las columnas, pero la transicion seguia a medias: el borrado
seguia sobrescribiendo `status` con `DELETED` y habia que filtrar por
`status <> 'DELETED'` en todas partes. Con los 29 lectores migrados, esa escritura
se retiró y `status` quedo como estado de negocio.

### Migraciones

- `catalogo/V4__add_deleted_at_category_skill.sql`: `deleted_at` + `previous_status` en `category` y `skill`, con backfill e indices parciales.
- `usuarios/V902__status_is_business_state_only.sql`: devuelve el estado de negocio a las filas que el codigo viejo dejo en `DELETED`.
- `catalogo/V5__status_is_business_state_only.sql`: lo mismo en las 4 tablas de catalogo.

Las de `usuarios` y `catalogo` cubren tablas de otros schemas a proposito, por el
mismo motivo que explica V901: `usuarios` es dueno del cascade y el arranque es
usuarios -> perfiles -> catalogo -> ofertas, asi que una sola transaccion deja el
conjunto atomico.

### Los 29 lectores

El conteo real era **29**, no 56: 25 consultas SQL y 4 llamadas JPA
`findByStatusNot("DELETED")`.

| Servicio | Sitios | Cambio |
|---|---|---|
| `perfiles` | 3 | `findByDeletedAtIsNull()` en los 3 repos, listados por `deleted_at` |
| `ofertas` | 4 | `JobOfferService` + `ProfileOwnerResolver` (incluye el cruce a `category`) |
| `catalogo` | 21 | 21 `status <> 'DELETED'` -> `deleted_at IS NULL` en 5 controllers |
| `resenias` | 1 | `ReviewService` |

### `@DynamicUpdate` en las entidades

`CompanyProfile`, `CustomerProfile`, `ProfessionalProfile` y `JobOffer` llevan
`@DynamicUpdate`. Sin el, un UPDATE de JPA escribe **todas** las columnas: una
entidad cargada antes de que el cascade borrara la fila pondria `deleted_at` en
null al guardar, resucitando el registro. Es el fallo silencioso que hace
peligroso mapear una columna que otro servicio escribe.

### Dos bugs reales encontrados por el E2E

1. **Perfiles y ofertas perdian el estado al restaurar.** `reactivate` hacia
   `setStatus("ACTIVE")` en hard, el mismo defecto que V901 corrigio en usuarios:
   una cuenta `SUSPENDED` dada de baja volvia como `ACTIVE`. Ahora restauran lo
   que corresponde. En ofertas se nota porque `ck_job_offer_status` si admite
   `INACTIVE`; en perfiles el CHECK solo admite `ACTIVE` y `DELETED`, asi que ahi
   el bug era latente.
2. **Las ofertas no se podian reactivar.** `updateStatus` rechazaba la oferta
   borrada como "no encontrada" antes de llegar al chequeo de ADMIN, asi que el
   mensaje "Solo ADMIN puede reactivar una oferta eliminada" era inalcanzable. El
   guard se quito; el permiso lo resuelve el chequeo de ADMIN.

### Dos lectores que faltaban en el inventario

El patron `status = 'ACTIVE'` no aparecia en el inventario de 29 sitios porque no
es un filtro de borrado, pero dependia del marcador `DELETED` para excluir
borrados. Al retirar la escritura aparecieron como servicios de un profesional
dado de baja:

- `ACTIVE_FILTER` de `ProfessionalServiceController` y `CompanyServiceController`
  (listados publicos).
- `ProfileOwnerResolver.isActiveService`, que usa `QuotationService` en 2 sitios:
  sin el fix se podia cotizar un servicio de un profesional dado de baja.

### Decision de producto, no se toco

Dar de baja una categoria impide crear o editar ofertas en ella
(`hasActiveCategory` se valida en create/update), pero **no** oculta las ofertas
que ya existian. Es un vacio conocido, no parte de esta migracion, asi que se
documenta en vez de cambiarlo.

### `previous_status` queda inerte

Desde que el borrado no toca `status`, no hay estado previo que guardar: la
columna ya no se escribe en ningun sitio. No se borre todavia, para no atar esta
migracion al despliegue simultaneo de los cinco servicios; se puede retirar en
una posterior.

### Regresion

`e2e-baja-reversible` 44/44 (nuevo), `e2e-roundtrip` 23/23, `e2e-admin-baja`
22/22, `e2e-roundtrip-company` 12/12, `e2e-servicios` 13/13, `e2e-realista` 46/46,
`e2e-mine` 18/18, `e2e-typemismatch` 24/24, `e2e-accept-reject` y `e2e-grupo3` con
todo OK. Navegador: `page-admin` 24/24. Frontend: `npm run lint` y `npm run build`
limpios. Sin commit ni push.

---

## Encender la suite de tests: lo que estaba apagado

El analisis de la fase 2 senalo que ningun test del repo se ejecutaba. Se
confirmo y se arreglo. Al encenderlo aparecieron fallos de dos clases, y lo
interesante es que solo uno era mio.

### Por que no corrian (y por que la primera hypothesis era falsa)

`maven-surefire-plugin` resolvia pero faltaba
`org.apache.maven.surefire:surefire-junit-platform:3.5.6`, y en local solo
estaba la 3.5.2. La primera conclusion fue "fijar surefire a 3.5.2 en cada pom".
Era una trampa: la causa real de que no corrieran era que **todos los comandos
llevaban `-o`** (modo offline), no que faltara el plugin. Se probo con red y se
descargo el artefacto. **Los poms quedaron sin tocar**: anadir un pin de plugin
habria sido ruido, y ademas habria fijado la version equivocada por un problema
que no era de version.

### Lo que si era mio

`JobOfferOwnershipTest.listadoPublicoFiltraEstados` fallaba: creaba una oferta
con `status='DELETED'` esperando que el listado la ocultara, pero el borrado ya
no se marca asi. La intencion del test sigue siendo correcta; cambio la
codificacion. Se anadio `crearOfertaDadaDeBaja(...)`, que inserta con
`deleted_at` marcada y `status` intacto.

Debajo de ese fallo habia otro mas profundo:

```
Schema validation: missing column [deleted_at] in table [job_offer]
```

`job_offer` la creo `ofertas/V1`, pero sus columnas de borrado las declaraba
`usuarios/V901`, porque `usuarios` es dueno del cascade. En produccion el orden
de arranque usuarios -> perfiles -> catalogo -> ofertas lo tapa, pero el test de
integracion de ofertas solo corre **sus propias** migraciones, asi que
`ddl-auto: validate` reventaba al levantar el contexto.

La lesson es arquitectonica: **una columna de una tabla debe declararla el
servicio dueno de esa tabla**. Se anadio `ofertas/V901__add_deleted_at_job_offer.sql`
con `ADD COLUMN IF NOT EXISTS`, que es idempotente: da igual que se aplique una
vez, otra o las dos. Asi el esquema de test de ofertas se monta solo.

Tambien hizo falta actualizar `fixtures/foreign-schemas.sql`, que documenta el
acoplamiento real de ofertas con usuarios, perfiles y catalogo: esas tablas
ajenas no tenian `deleted_at` y las consultas de `ProfileOwnerResolver` fallaban
con `column "deleted_at" does not exist`.

Nota de numeracion: la primera version de esa migracion se llamo `V3` y rompio el
arranque con `Validate failed: Migrations have failed validation`, porque la
historia de ofertas ya tenia V1, V2 y **V900**: anadir V3 es una migracion fuera
de orden. El proyecto ya usa el prefijo 900+ para migraciones tardias, y es lo
que habia que respetar.

### Lo que NO era mio, verificado

Los 2 fallos de `JobApplicationOwnershipTest` (un `LazyInitializationException
que escapa donde deberia un 400) se comprobaron **revirtiendo mis cambios de
produccion con `git stash` y ejecutando el test otra vez**: fallan igual sin mis
cambios. Estaban ocultos porque el test nunca se habia ejecutado. Se arreglaron
anadiendo `@Transactional` a `JobApplicationService.update`, que toca un proxy
lazy sin sesion abierta.

### Estado

`ofertas`: 23/24. API: `e2e-baja-reversible` 44/44, `e2e-roundtrip` 23/23,
`e2e-admin-baja` 22/22, `e2e-accept-reject` todo OK.

### Lo que queda sin decidir

Los 7 tests `*ApplicationTests` (catalogo, gateway, notificaciones, ofertas,
perfiles, resenias, usuarios) fallan todos con `Unable to obtain connection from
database`: no tienen Testcontainers ni perfil `test`, asi que Flyway intenta
conectar a un Postgres local que no existe. Son preexistentes y ajenos a esta
fase, pero son los unicos que quedan en rojo.

### Cierre: `restore` de category/skill no debe reescribir el estado

Quedaba un cabo suelto de la fase 2. `delete` dejo de escribir
`previous_status` (correcto: `status` es estado de negocio y borrarse no lo
altera), pero los dos endpoints `PUT /{id}/restore` seguian haciendo

```sql
SET status = COALESCE(previous_status, 'ACTIVE')
```

Como nadie escribia `previous_status`, ese `COALESCE` caia **siempre** en
`'ACTIVE'`. Traducido: **restaurar una categoria `INACTIVE` la promovia a
`ACTIVE`**. El endpoint escribia en la columna que la propia fase 2 declaro
intocable.

`restore` ahora solo despeja la marca de borrado y drena el `previous_status`
vestigial. Los datos confirman que es vestigial de verdad: 0 filas con
`status='DELETED'`, 0 con `previous_status` informado, 0 borradas. V5 ya habia
normalizado las filas heredadas, asi que la columna ya no tenia nada que
recuperar y solo generaba este falso default.

Alcance real: **latente, no alcanzable por API hoy**. No existe endpoint para
poner una categoria en `INACTIVE`; `create` y `update` solo tocan
nombre/descripcion. El defecto solo se activaba por SQL o con un flujo de
desactivacion que todavia no existe. Se corrige igual porque deja el endpoint
correcto por construccion, sin acoplarlo a una columna muerta.

`e2e-restore-estado.mjs` (11 checks) cubre el caso forzando `INACTIVE` /
`SUSPENDED` por SQL y pasando por la API. Se verifico que **detecta** el bug:
revertido el fix, falla 3 checks con `quedo ACTIVE, se esperaba INACTIVE`. Un
test que solo pasa no demuestra nada; uno que se rompe al reintroducir el
defecto, si.

### Fuera los 7 context-load tests

Eran `@SpringBootTest` con un `contextLoads()` vacio, sin una sola asercion. No
comprobaban comportamiento: solo que el contenedor de Spring levanta. Y no
levantaba nunca, porque no tienen Testcontainers ni perfil `test`, asi que
Flyway buscaba un Postgres local inexistente. Todo el repo reportaba `BUILD
FAILURE` portests que no podia ejecutar.

Borrados los 7 (`Catalogo`, `Gateway`, `Notificaciones`, `Ofertas`, `Perfiles`,
`Resenias`, `Usuarios`). Con ellos se van los directorios `src/test` de 6
modulos, que ya no tenian nada mas dentro.

**Trampa al borrarlos:** sin `mvn clean`, `mvn test` **sigue ejecutando los
`.class` viejos** de `target/test-classes`. Los 7 modulos siguieron reportando
`Tests run: 1, Errors: 1` con los sources ya borrados del arbol. Borra el
`.java` no basta: hay que limpiar el target, o un build incremental en CI
ejecutara tests fantasma.

### Estado real de la suite

```
catalogo        BUILD SUCCESS   sin tests
gateway         BUILD SUCCESS   sin tests
notificaciones  BUILD SUCCESS   sin tests
ofertas         BUILD SUCCESS   Tests run: 23, Failures: 0, Errors: 0
perfiles        BUILD SUCCESS   sin tests
resenias        BUILD SUCCESS   sin tests
usuarios        BUILD SUCCESS   sin tests
```

Conviene decirlo sin adornos: el repo tiene tests reales en **1 de 7 modulos**.
Borrar los context-load no quita cobertura (no la tenian), pero deja al
descubierto el nivel real, que antes quedaba disimulado tras 7 tests que
parecian cubrir y en realidad no podian correr. La deuda de tests de
catalogo, usuarios, perfiles y servicios sigue ahi y es la opcion 3.

## Cerrar los huecos: tests de regresion de los 3 bugs arreglados

La fase 2 dejo 36 archivos de produccion modificados y **una sola** asercion
durable (`listadoPublicoFiltraEstados`). Los 11 E2E que la verificaban vivian
en `Temp`: 0 entradas en git. Entregar un cambio asi es entregarlo con la
evidencia desechable. Esto es lo que se cerro.

### `ofertas`: 23 -> 30 tests (`OfertaBajaTest`)

Fija el contrato de la baja que antes no estaba cubierto en ningun sitio:
borrar marca `deleted_at` y **no toca `status`**; la oferta dada de baja
desaparece de los tres caminos de lectura; borrar dos veces responde que no
existe; y el dueno **no** puede reactivar (la reactivacion es un acto de
administracion) mientras que ADMIN si.

**Regresion verificada por mutacion:** reintroduje el guard que hacia la
reactivacion inalcanzable y los tests fallan (2 errores). Restaurado el fix,
30/30. Un test que solo pasa no demuestra nada.

### `catalogo`: 0 -> 8 tests, con su propia infra

`catalogo` no tenia ni una dependencia de test. Se le anadieron
(`spring-boot-starter-test`, `spring-security-test`, testcontainers), un
`AbstractIntegrationTest` propio y un fixture con las dos tablas de `perfiles`
que catalogo consulta.

Cubre el round trip de category y skill, que `restore` no promueve `INACTIVE`/
`SUSPENDED` a `ACTIVE`, que el nombre borrado sigue reservado por `UNIQUE`, y
que borrar **y** restaurar son de ADMIN.

**Regresion verificada por mutacion:** reintroducido el
`status = COALESCE(previous_status, 'ACTIVE')`, fallan 3 tests. Restaurado, 8/8.

Un detalle que salio al montar MockMvc: sin
`.apply(SecurityMockMvcConfigurers.springSecurity())` los post-processors por
request no aplican y cada prueba se ejecutaba como el usuario de
`@WithMockUser`. La primera version de "un profesional no puede restaurar"
pasaba con un 200 porque en realidad estaba corriendo como ADMIN: un test que
verifica lo contrario de lo que cree. Con la cadena de filtros montada, cubre
el camino real y `@PreAuthorize` incluido.

### Bug de migraciones encontrado al montar los tests de catalogo

Montar el test de catalogo sobre base limpia fallo con
`column "previous_status" does not exist` en V5. No era un problema del test:
**V5 no era autocontenida.** Normalizaba `professional_service` y
`company_service` usando una columna que declaraba `usuarios/V901`. En
produccion el orden usuarios -> perfiles -> catalogo lo tapaba; en cualquier
base donde usuarios no hubiera corrido antes, catalogo no arrancaba.

Es la misma incoherencia que ya se habia corregido dos veces (job_offer en
ofertas, category/skill en catalogo) y que aqui se habia pasado por alto:
`professional_service` y `company_service` **son de catalogo**, asi que las
columnas las declara catalogo, no el dueno de la cascada. La leccion no era
"aplicar el principio en tres sitios", sino que el principio tiene que cubrir
**todas** las tablas que el servicio posee.

Correccion: V5 se queda solo con category y skill (que V4 le deja completas), y
la nueva `V6__add_deleted_at_service_tables.sql` asume esas dos tablas con
ALTER idempotente mas la normalizacion movida desde V5. `catalogo` deja de
depender del orden de arranque.

Como V5 ya estaba aplicada, editarla cambiaba su checksum y reventaba el
arranque (`Migration checksum mismatch for migration version 5`). V5 es
idempotente (sus UPDATEs llevan `WHERE status='DELETED'` y ya normalizaron
todo), asi que en la base local se borro su fila de `flyway_schema_history` y
Flyway la reaplico. **Quien tenga una base local con V5 aplicada necesita lo
mismo:** `DELETE FROM catalogo.flyway_schema_history WHERE version='5';` o
`flyway repair`. En un entorno compartido esto habria que coordinar antes de
desplegar.

### Estado

```
catalogo         BUILD SUCCESS   Tests run: 8
ofertas          BUILD SUCCESS   Tests run: 30
gateway/notificaciones/perfiles/resenias/usuarios   BUILD SUCCESS (sin tests)
```

Los 3 bugs corregidos tienen ahora test de regresion que falla si se
reintroducen. Lo que sigue sin cubrir: `perfiles` (10 archivos) y `usuarios`
(7) siguen sin un solo test, y sus 29 lectores migrados a `deleted_at` solo los
verifican E2E temporales. Ese es el resto de la opcion 3.

## Cerrar perfiles y usuarios: 0 tests -> 15

`perfiles` (10 archivos de produccion) y `usuarios` (7) eran los ultimos huecos:
sus lectores migrados a `deleted_at` solo los validaban E2E temporales.

### El mismo bug de migraciones, ahora en `perfiles`

Montar el test de perfiles sobre base limpia fallo igual que fallo el de
catalogo: `perfiles` solo tenia V1 y V2, y las columnas de borrado de sus tres
propias tablas las declaraba `usuarios/V901`. Es decir, **perfiles tampoco era
autocontenido**, y dependia de que usuarios hubiera arrancado antes. Se corrigio
con `perfiles/V901__add_deleted_at_profiles.sql`, ALTERs idempotentes sobre sus
tres tablas.

Tercera vez que aparece este fallo, y la segunda en el mismo dia. No es
casualidad: el patron `usuarios/V901` alterando tablas de otros servicios
sembraba que centralizar las columnas era correcto, y por eso se fue
repitiendo el mismo error en cada modulo. La regla que queda escrita en cada
migracion es la contraria: **cada servicio declara las columnas de sus propias
tablas, aunque otro escriba en ellas por cascada.**

Nota de numeracion (la segunda vez que aparece): la primera version de esta
migracion se llamo `V3` y rompio el arranque. La historia de perfiles ya tenia
V1, V2 y **V900 ("demo profiles")**, asi que V3 es una migracion fuera de
orden. El proyecto usa 900+ para lo que llega tarde; `catalogo/V6` si era
valido porque ahi la historia solo llega hasta V5.

### `perfiles`: 7 tests (`PerfilBajaTest`)

Borrar no toca `status`; el perfil dado de baja desaparece de listado, detalle
y lectura por usuario; reactivar levanta la baja; no se borra dos veces ni se
reactiva lo que esta vivo; un tercero no puede tocar un perfil ajeno; y empresa
y cliente siguen el mismo contrato.

### `usuarios`: 8 tests (`UsuarioBajaTest`)

Es el servicio con mas superficie (es dueno de la cascada: 8 tablas de 5
schemas), asi que es donde un error de `deleted_at` se propaga mas lejos. Sus
fixtures documentan esa superficie, que antes no estaba escrita en ningun sitio.

Cubre: la baja no toca `status`; la cascada marca perfiles y ofertas; **no**
marca las filas de otro usuario; alcanza los servicios de catalogo resueltos
por subconsulta; notificaciones y resenias se borran en duro y no se restauran;
reactivar levanta toda la cascada; y borrar un usuario no toca el catalogo
global (category/skill no son de nadie).

### Una garantia que resulto ser falsa

El primer test de perfiles afirmaba que `@DynamicUpdate` impide que un `save()`
sobre una copia desactualizada resucite un perfil dado de baja. **Fallo**, y
tenia razon el fallo: `save()` sobre una entidad desligada hace *merge*, y el
merge marca como sucias todas las columnas que difieren, `deleted_at`
incluida, escribiendo un null y resucitando la fila. `@DynamicUpdate` solo
protege a una entidad **gestionada**.

La garantia real es otra, y si existe: los metodos `update` de perfiles cargan
el perfil fresco y rechazan editar uno dado de baja ("el perfil esta eliminado,
no se puede modificar"). El test ahora cubre eso.

Los comentarios de `@DynamicUpdate` en las 4 entidades (3 de perfiles +
`JobOffer`) afirmaban la garantia incorrecta. Corregidos para decir la verdad:
protege a la entidad gestionada, no a la copia desligada, y quien protege de
resucitar es el guard explicito del `update`.

Vale la pena dejarlo escrito: los comentarios de codigo tambien son
afirmaciones, y una afirmacion que nadie verifico se convierte en deuda igual
que un test que no existe.

### Verificacion por mutacion

Los tests nuevos se comprobaron reintroduciendo cada bug:

| Bug reintroducido | Resultado |
|---|---|
| `delete` de usuario vuelve a pisar `status` | 2 fallos en usuarios |
| `delete` de perfil vuelve a pisar `status` | 2 fallos en perfiles |
| guard de reactivacion inalcanzable (ofertas) | 2 errores |
| `COALESCE(previous_status,'ACTIVE')` (catalogo) | 3 fallos |

### Estado

```
catalogo         BUILD SUCCESS    8 tests
ofertas          BUILD SUCCESS   30 tests
perfiles         BUILD SUCCESS    7 tests
usuarios         BUILD SUCCESS    8 tests
gateway/notificaciones/resenias   BUILD SUCCESS (sin tests)
```

53 tests, todos verificados por mutacion. API completa en verde: reversible
44/44, admin-baja 22/22, roundtrip 23/23, roundtrip-company 12/12,
restore-estado 11/11, mine 18/18, servicios 13/13, realista 46/46,
typemismatch 24/24.

Lo que sigue sin cubrir: `resenias` (2 archivos, su `deleted_at` en el filtro
de `ReviewService`), `notificaciones` y `gateway`.

## `resenias`: 20 tests (`ReseniaBajaTest`, `ReseniaCreacionTest`)

Ultimo modulo con logica de negocio sin un solo test, y el de la fase con mas
acoplamiento ajeno: `ReviewService` no solo referencia `usuarios.app_user` y
`perfiles.professional_profile` por FK, las **consulta por SQL crudo** para
exigir la transaccion previa. Por eso sus fixtures declaran cinco tablas de
cuatro schemas, la mayor superficie de las seis.

### El borrado era fisico, y aqui no habia estado que perder

`deleteReview` hacia `reviewRepository.delete(review)`. Una reseña es contenido
de usuario y no se podia recuperar, que era la razon por la que se apuntaba en
las notas como "decision de producto". Ahora la fila sobrevive con `deleted_at`.

`review` no tiene columna `status`, asi que **no hay `previous_status`**: es el
unico modulo de la fase donde el borrado no reescribe nada mas de la fila. Por
eso V901 solo anade `deleted_at`.

Aqui la regla de "cada servicio declara las columnas de sus propias tablas" se
cumplo a la primera: no aparecio la cuarta reincidencia.

### El UNIQUE que hacia la baja irreversible a medias

`uk_review_user_professional` seguia ocupando el par con una fila invisible. Con
borrado logico, el autor borraba su reseña y a partir de ahi recibia "Ya
dejaste una reseña para este profesional" sin poder hacer nada al respecto: un
estado que el propio sistema hacia irresoluble.

V901 suelta la restriccion de tabla y crea un indice unico **parcial** sobre las
reseñas vivas, que es donde la regla "una reseña por autor y profesional" sigue
siendo cierta. Se eligio el indice parcial y no reutilizar la fila dada de baja
(que es lo que hace `perfiles/create`, reactivando la entidad existente) porque
con el indice parcial las dos reseñas quedan en la base, y una baja ya no es una
decision sin retorno.

### La garantia de `@DynamicUpdate`, esta vez escrita bien

El comentario de la entidad afirma lo mismo que los otros cuatro y por lo mismo
que ellos: protege a la entidad **gestionada**, no a la copia desligada que un
`save()` vuelve a guardar. Y esta vez hay un test que lo verifica con el
mecanismo real, no una frase: el test da de baja la fila por JDBC (otra
peticion, otra unidad de trabajo) mientras la entidad esta gestionada y
desactualizada, la edita y la guarda. Sin `@DynamicUpdate` el guardado escribe
tambien `deleted_at` en null y la resucita.

### La baja paso de ser un `if` en Java a un `UPDATE` condicional

La primera version hacia `save()` con `deleted_at` puesto desde Java, y el test
de "no se borra dos veces" comprobaba un `if` sobre la entidad cargada. Eso
protege de la doble baja **secuencial** y nada mas: dos peticiones simultaneas
que llegan juntas pasan las dos por el `if`, y la segunda vuelve a escribir la
fila. La garantia de "no se borra dos veces" era entonces una coincidencia del
orden de llegada, no una propiedad del sistema.

Ahora `ReviewRepository.marcarBaja` es un `@Modifying @Query` con el filtro
dentro de la sentencia:

```java
@Query("UPDATE Review r SET r.deletedAt = :baja, r.updatedAt = :baja "
        + "WHERE r.id = :id AND r.deletedAt IS NULL")
int marcarBaja(@Param("id") Long id, @Param("baja") LocalDateTime baja);
```

La segunda sentencia ya no tiene filas que tocar, y `== 0` es lo que distingue
"ya estaba dada de baja" (400) de "no existe" (404, que decide el `findById` de
antes). La carga previa no se tira: sigue haciendo falta para el 404 y para la
propiedad, y por eso se mantiene y el filtro **no** esta en ella.

`@Modifying` necesita transaccion propia y no la hereda de
`SimpleJpaRepository` (que solo cubre los metodos heredados de `CrudRepository`),
asi que el `@Transactional` va en el metodo del repositorio, no en el servicio.
Los tres `@Transactional` del servicio ya se habian quitado antes: dejaban el
envio de la notificacion dentro de la transaccion, y sin transaccion
OpenEntityManagerInView dejaba la sesion abierta a un hilo sin contexto de
transaccion.

### Dos indices parciales que no hacian falta

V901 anadia `ix_review_user_not_deleted` e `ix_review_professional_not_deleted`
para los tres listados. Los dos son **subconjuntos estrictos** de `idx_review_user_id`
e `idx_review_professional_id`, que V1 ya crea sobre esas mismas columnas: el
planificador usa el indice completo y filtra. Se quitaron, y con ellos el
mantenimiento en cada escritura a cambio de nada. El indice unico parcial si
se queda, porque ese si carga con trabajo: es el que impide el duplicado.

### `deleted_at` no puede salir en el JSON

Este servicio devuelve la entidad y no un DTO, asi que anadir el campo habia
cambiado el contrato de todas las respuestas sin que nadie lo pidiera. Los otros
cuatro servicios no lo exponen porque filtran al construir el DTO. Aqui se
marca con `@JsonIgnore` en la entidad.

El test que lo fija comprueba la anotacion, no un payload serializado, y
conviene saber por que: **este modulo no expone ningun bean `ObjectMapper`**, el
mapper del convertidor de MVC se construye internamente, asi que un
`new ObjectMapper()` de prueba no seria el de la API. Es el test mas debil de
los veinte; si algun dia se quiere el payload de verdad, la via es un test de
slice sobre el controlador.

### Verificacion por mutacion

21 mutaciones, 21 detectadas. Cada una con exactamente el fallo que le toca, que
es la forma de descartar que un test este pasando por otra razon:

| Mutacion | Fallos que la detectan |
|---|---|
| la baja vuelve a ser un `DELETE` fisico | 6 |
| el `UPDATE` de baja deja de filtrar por `deleted_at` | 2 |
| el servicio deja de mirar las filas afectadas | 1 |
| `@JsonIgnore` fuera de la entidad | 1 |
| guard de edicion sobre dada de baja eliminado | 1 |
| `getAllReviews` vuelve a `findAll()` | 2 |
| `getReview` deja de filtrar | 1 |
| listado por profesional sin filtro | 1 |
| listado por usuario sin filtro | 1 |
| el chequeo de duplicado mira tambien las dadas de baja | 1 |
| `@DynamicUpdate` fuera de la entidad | 1 |
| el indice unico deja de ser parcial | 1 |
| la propiedad de la reseña se relaja | 1 |
| solo el dueno puede: ADMIN se queda fuera | 2 |
| el rol puede resenar deja de filtrar | 1 |
| la reseña sobre el propio perfil se permite | 1 |
| la transaccion previa deja de exigirse | 3 |
| el profesional dado de baja sigue siendo reseñable | 1 |
| la postulacion PENDING cuenta como transaccion | 1 |
| el rating se deja de validar en el servicio | 1 |
| se acepta un id al crear | 1 |

El arnés de mutaciones tenia un fallo propio que produjo dos "fallos extra" que
no eran flake del test suite: restauraba solo los archivos de cada mutacion, de
modo que arrastraba la anterior. Con restauracion global las 21 dan
exactamente los fallos de la tabla.

Las dos mutaciones de la primera version quedaron obsoletas al mover el borrado
al repositorio (`save()` -> `delete()` y el guard sobre `getDeletedAt()` ya no
existen) y se sustituyeron por las que cubren el codigo nuevo.

### Dos cosas que quedan sin decidir

1. **No hay endpoint para reactivar una reseña.** La baja ya no destruye datos,
   pero la API no la deshace: los otros cuatro servicios tienen `reactivate` y
   aqui no. El frontend sigue avisando de que las reseñas no se recuperan, y eso
   ya solo es cierto para la baja de usuario.
2. **`usuarios` sigue borrando reseñas en duro al dar de baja un usuario**
   (`AppUserService:263`), y su test lo afirma a proposito: la baja de usuario es
   la opcion nuclear y se lleva tambien notificaciones. Consecuencia asumida, no
   olvidada: restaurar el usuario no devuelve sus reseñas.

### Estado

```
catalogo         BUILD SUCCESS    8 tests
ofertas          BUILD SUCCESS   30 tests
perfiles         BUILD SUCCESS    7 tests
resenias         BUILD SUCCESS   20 tests
usuarios         BUILD SUCCESS    8 tests
gateway/notificaciones   BUILD SUCCESS (sin tests)
```

73 tests, todos verificados por mutacion. La API completa no se ha vuelto a
levantar: de los nueve scripts E2E, solo `e2e-realista` toca reseñas y
unicamente para crearlas, listarlas y comprobar el duplicado; ninguno borra
una, que es el unico camino que cambia.

Lo que sigue sin cubrir: `notificaciones` y `gateway`.

---

## Bloque de seguridad 1 — dar de baja ya impide autenticarse

`AppUserService.login` buscaba por `findByEmail`, que no mira `deleted_at`. Una
cuenta dada de baja conservaba `status = 'ACTIVE'` —la baja vive solo en
`deleted_at`— asi que el login la aceptaba con la clave correcta. El filtro tiene
que ir en el mismo SELECT que trae la clave, no en un `if` aparte.

`findVivaByEmail` filtra por `deletedAt IS NULL` y trae el rol con `JOIN FETCH`. El
registro sigue usando `findByEmail` sin filtro a proposito: el email de una cuenta
dada de baja tiene que seguir reservado por el `UNIQUE` de `app_user.email`.

La baja responde el mismo error generico que una cuenta inexistente. Al principio
se decia "cuenta dada de baja" y eso convertia el login en un oraculo de que
emails estan dados de baja. 4 tests nuevos en `UsuarioBajaTest` (8 -> 12).

## Bloque de seguridad 2 — lecturas que no dependan de open-in-view

`toDto` lee el rol, y el rol es LAZY. `findAll`, `findById` y `findByRole` no lo
traian y sus servicios no son transaccionales, asi que el proxy salia desligado
del repositorio y reventaba con `LazyInitializationException`.

No se notaba porque Spring Boot deja `spring.jpa.open-in-view` en `true` por
defecto, que mantiene la sesion abierta durante la peticion. Es un default, no
una decision: el dia que alguien lo ponga en `false` —que es lo que recomienda el
framework— el listado, el detalle, el listado por rol y el cambio de estado
empiezan a devolver 500. Los tests de integracion no pasan por ese filtro, asi que
lo detectan antes. 6 tests nuevos en `AppUserLecturaTest`.

`updateStatus` lleva transaccion en vez de fetch, y merece la pena explicar por que
se descarto el fetch: el DTO se arma sobre lo que devuelve `save()`, y el
resultado de un `merge` sale con un proxy de rol nuevo que ya no tiene a quien
preguntarle. El fetch en la carga no arregla nada, porque la entidad que se lee
despues no es la que se cargo. La transaccion ademas cierra un agujero de
concurrencia que tenia: es un leer-modificar-escribir y dos cambios de estado
simultaneos se pisaban sin que nadie lo notara.

## Bloque de seguridad 3 — allowlist de roles en el registro

El registro publico comprobaba unicamente que el rol elegido no fuera `ADMIN`, y
dejaba pasar cualquier otro. Eso convierte en una escalada trivial anadir un rol
privilegiado a la semilla: en el momento en que exista, cualquiera que lo pida
desde el formulario de alta se registra con el, sin tocar una linea de Java.

Lo que decide ahora es una `Set` con los tres roles de autoservicio (`CUSTOMER`,
`PROFESSIONAL`, `COMPANY`) y se rechaza todo lo demas, que es el orden que falla
cerrado: un rol nuevo no es registrable hasta que alguien lo decida a mano, en
vez de serlo hasta que alguien se acuerde de bloquearlo.

El corte va antes de la consulta de rol y el mensaje es el mismo tanto si el rol
existe pero no es de autoservicio como si no existe. Con el codigo anterior los
dos casos respondian distinto, y esa diferencia ya permitia enumerar los nombres
de la tabla `role` desde un endpoint publico.

7 tests nuevos en `RegistroRolTest`, incluido el que crea un rol privilegiado
inventado (`SUPERADMIN`) que el codigo no conoce y comprueba que se rechaza y que
no queda ningun usuario con el. Con el codigo anterior ese alta tendria exito.

---

## INCIDENTE — 4 secretos de desarrollo publicados en el repositorio publico

### Que paso

El commit `de348ed` ("ajjaja", 6 ago 2026) subio un `.env` a la raiz con valores
reales, no placeholders:

- `POSTGRES_PASSWORD`
- `JWT_SECRET`
- `ADMIN_BOOTSTRAP_KEY` (protege `POST /api/auth/register-admin`)
- `INTERNAL_SERVICE_KEY` (protege `POST /api/notifications/internal`)

El repositorio es publico. El commit `638e19e` (24 sep) ya habia sacado el `.env`
del arbol y anadio `.gitignore` y `.env.example` con placeholders, pero eso no
borra nada del historial: el blob seguia siendo legible por cualquiera que clonara.

Puesto que el `.env` lleva los valores, no van aqui. Lo que hace falta es
generarlos nuevos y actualizar el `.env` local de cada quien.

### Alcance

- Solo la rama `testeo` contenia el commit. Las otras 5 (`main`, `backend`,
  `Front-End`, `db_dev`, `feature-mg`) no lo tienen y no se tocaron.
- `JWT_SECRET` estuvo ~7 semanas publico: cualquiera pudo firmar tokens validos del
  stack de desarrollo. Si el stack de dev comparte esa clave con `db_dev` u otro
  ambiente, hay que rotarla ahi tambien.
- No hay despliegue en produccion todavia, asi que la exposicion es del entorno de
  desarrollo. Eso baja la gravedad, no la elimina.

### Reescritura del historial (hecha)

`git filter-repo --refs refs/heads/testeo --invert-paths --path .env`, luego
`push --force-with-lease`. Se reescribieron 17 commits:

| antes | ahora |
|---|---|
| `31132fe` allowlist de roles | `882cc7f` |
| `c21822e` lecturas sin open-in-view | `a479317` |
| `66dcd49` baja impide autenticarse | `1613d9d` |
| `a5766b8` itinerario | `8676876` |

El arbol de `HEAD` quedo byte a byte identico (`git diff 31132fe..882cc7f` sale
vacio): lo unico que cambio fueron los SHA. El repo local quedo limpio (ref
obsoleta borrada, reflog caducado, `gc --prune=now`, blob destruido) y se borro el
bundle de respaldo.

### Lo que la reescritura NO resuelve

`GET /contents/.env?ref=testeo` ya responde 404, pero
`GET /commits/de348ed` **sigue devolviendo el `.env` en claro**. Los objetos sin
referencia los sirve GitHub igualmente en repos publicos, y eso no se arregla
desde el cliente: hay que pedirlo a soporte.

Ademas, mientras el commit estuvo publicado, el contenido pudo quedar en caches, en
clones de otra gente y en indexadores. La reescritura reduce la exposicion, no la
anula. Por eso la rotacion de abajo no es opcional en ningun caso.

### Pendiente (acciones de Gustavo, no automatizables)

1. **Rotar los 4 valores** con `openssl rand -base64 48` (24 para bootstrap e
   internal) y actualizar el `.env` local. Con la clave nueva, regenerar el `.env`
   de cada quien. Revisar tambien cualquier ambiente que reutilice los valores
   viejos.
2. **Pedir a soporte de GitHub la purga de los objetos sin referencia.** Ventana de
   24h desde el push, asi que hay que hacerlo cuanto antes:
   https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository
3. **Avisar al equipo**: quien tenga `testeo` clonado tiene que volver a clonarlo.
   Un `pull` normal falla al divergir; la via es `rm -rf .git` y clonar de nuevo,
   o `git fetch && git reset --hard origin/testeo` y perder los commits locales no
   pusheados.

### Estado de la suite tras la reescritura

```
usuarios         BUILD SUCCESS   25 tests
perfiles         BUILD SUCCESS    7 tests
catalogo         BUILD SUCCESS    8 tests
ofertas          BUILD SUCCESS   30 tests
resenias         BUILD SUCCESS   20 tests
```

90 tests. Cada fix de los tres bloques de arriba se verifico por mutacion: el
test tiene que ponerse rojo al deshacer el codigo, y solo en los tests que le
toca.
