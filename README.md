# B2BMatch — Marketplace B2B de Servicios Profesionales

Plataforma web que conecta **empresas** (publican ofertas de trabajo), **profesionales** (se postulan y ofrecen servicios) y **administradores** (moderan la plataforma).

---

## Stack

| Capa | Tecnología |
|---|---|
| **Frontend** | React 19 + Vite 8 + React Router 7 + Axios |
| **Backend** | Java 21 + Spring Boot 4 (6 microservicios) + Maven |
| **Base de datos** | PostgreSQL 16 (esquema por microservicio) |
| **Infraestructura** | Docker Compose (BD + microservicios) |

## Microservicios

| Microservicio | Puerto | Esquema | Estado |
|---|---|---|---|
| `usuarios` | 8081 | `usuarios` | Autenticación (login/registro BCrypt), roles |
| `perfiles` | 8082 | `perfiles` | Perfiles de empresa / profesional / cliente |
| `catalogo` | 8083 | `catalogo` | Categorías, skills y servicios |
| `ofertas` | 8084 | `ofertas` | Ofertas de trabajo, postulaciones, cotizaciones |
| `resenias` | 8085 | `resenias` | Reseñas de profesionales |
| `notificaciones` | 8086 | `notificaciones` | Notificaciones de usuarios |

El frontend (Vite) expone un proxy inverso: `/api/*` se reparte hacia los puertos 8081–8086.

## Estructura

```
b2breact/
├── FRONTEND/            # React + Vite
│   └── src/
│       ├── components/  # Navbar, Footer, Cards, Modales...
│       ├── context/     # AuthContext (sesión y roles)
│       ├── layouts/     # MainLayout
│       ├── pages/       # Landing, Auth, User, Company, Admin, Servicios
│       ├── services/    # Clientes API (axios)
│       └── styles/      # CSS propio (variables, temas claro/oscuro)
├── BACKEND/             # Microservicios Spring Boot
│   ├── usuarios/        # 8081
│   ├── perfiles/        # 8082
│   ├── catalogo/        # 8083
│   ├── ofertas/         # 8084
│   ├── resenias/        # 8085
│   ├── notificaciones/  # 8086
│   ├── docker-compose.yml
│   └── init/            # Script que ejecuta las migraciones SQL
└── DATABASE/            # Scripts SQL históricos (referencia; el schema real lo arma Flyway de cada microservicio)
```

## Roles

| Rol | Registro | Puede hacer |
|---|---|---|
| **ADMIN** | No (solo seed) | Dashboard, aprobar/bloquear empresas, suspender usuarios, bajar ofertas |
| **PROFESSIONAL** | Sí ("Postulante") | Ver ofertas, completar perfil profesional, postularse, ver postulaciones, publicar servicios y ver cotizaciones recibidas |
| **COMPANY** | Sí ("Empresa") | Completar perfil de empresa, crear/editar/eliminar ofertas, ver postulantes de sus ofertas y dejarles reseñas |
| **CUSTOMER** | Sí | Consulta de ofertas |

## Usuarios demo (seed)

| Email | Contraseña | Rol |
|---|---|---|
| `admin@test.com` | `Admin123` | ADMIN |
| `webtester@example.com` | `Webtester1` | PROFESSIONAL |
| `companytester@example.com` | `Company123` | COMPANY |

> **Datos demo precargados** (para ver el proyecto "vivo"): el profesional `webtester` tiene publicados servicios en `/servicios`, el usuario `companytester` tiene una cotización solicitada y dejó una reseña a `webtester` (visible en su perfil). Si querés probar desde cero, los endpoints de borrado están disponibles para reseñas/cotizaciones.

## Cómo correr el proyecto

### Base de datos + microservicios (Docker)

```bash
cd BACKEND
# Primera vez: crear las variables locales (nunca comitear el .env real)
cp .env.example .env
docker-compose up --build
```

La primera vez, `init/00-run-migrations.sh` crea los esquemas, tablas y datos de prueba.

> Si cambiás el código de un microservicio, reconstruí la imagen:
> `docker-compose up --build <servicio>` o `docker-compose up --build` para todos.

### Frontend

```bash
cd FRONTEND
npm install
npm run dev
# http://localhost:5173
```

> Si el servidor de desarrollo deja de responder, relanzalo con `npm run dev`.

## Notas

- Autenticación por **JWT** (HS384): `POST /api/users/login` y `POST /api/users/register` devuelven un token que el frontend envía como `Authorization: Bearer <token>` (interceptor de Axios). Los 6 microservicios validan el token con un secreto compartido (`app.jwt.secret`, configurable vía `JWT_SECRET`).
- Rutas públicas (sin token): login/registro, listado de ofertas, catálogo (categorías/skills/servicios), perfiles de empresa/profesional y reseñas. El resto exige JWT válido (401 en caso contrario).
- Las ofertas de empresa (`job_offer`) aparecen en `/empleos` (público) y en `/admin/ofertas`.
- **Reseñas**: las empresas las dejan a los postulantes desde `/empresa/ofertas` (botón "Ver Postulantes") y el profesional las ve en su perfil `/perfil`.
- **Cotizaciones**: cualquier usuario logueado puede solicitar una cotización de un servicio desde `/servicios`; el profesional ve las solicitudes recibidas en su perfil.

---

## Flujo de modificaciones

Registro cronológico de los cambios realizados sobre el proyecto:

### 1. Correcciones iniciales y seed
- Se creó el **seed** con usuarios demo, categorías, skills, perfiles y ofertas de ejemplo.
- Se corrigió el **NPE en el catálogo** (carga de categorías/skills) y se compiló/verificó el arranque.

### 2. Unificación del modelo de ofertas
- Se unificó el modelo **`job_offer`** como entidad única para ofertas de empleo, usada por el listado público `/empleos` y por el panel de la empresa.

### 3. Módulo de notificaciones
- Microservicio `notificaciones` + campanita con badge en la Navbar (dropdown, marcar leída / todas leídas).

### 4. Módulo de administración
- Dashboard de admin con tarjetas de acceso a **Empresas**, **Ofertas** y **Usuarios**.
- Suspend/re-activar usuarios y cambio de estado de ofertas (PATCH).

### 5. Autenticación JWT (los 6 microservicios)
- `JwtUtil` + `JwtAuthFilter` + `SecurityConfig` stateless replicados en `usuarios`, `perfiles`, `catalogo`, `ofertas`, `resenias` y `notificaciones`.
- Login/registro devuelven un token; rutas públicas definidas por servicio; 401 JSON si falta el token.
- Frontend: token en `localStorage`, interceptor Axios que agrega `Authorization: Bearer` y que en 401 limpia la sesión y redirige a `/login`.

### 6. UI de notificaciones y modal
- Campanita con dropdown de notificaciones (estilos en `navbar.css`).
- Modal rediseñado tipo carta (`modal.css`: animación, línea superior de acento, botón circular de cierre).

### 7. Arreglo de las tarjetas (contenido "en el aire")
- **Causa raíz**: `cards.css` solo se importaba en componentes que no se usaban, por lo que `.card-b2b` no tenía estilos y el contenido de admin/empresa/postulaciones se veía suelto.
- **Solución**: import global de `cards.css` en `main.jsx`, refinamiento del hover (sin salto en tablas grandes) y variante `.card-b2b-lift` para tarjetas clicables.

### 8. Conexión de reseñas (antes solo existía el backend)
- **Perfil profesional** `/perfil`: nueva sección "Reseñas de clientes" que lista las valoraciones recibidas.
- **Mis ofertas** `/empresa/ofertas`: botón "Ver Postulantes" por oferta; la empresa ve las postulaciones y puede **dejar una reseña** (1–5 estrellas + comentario) y ver las reseñas existentes del postulante.

### 9. Conexión de cotizaciones (antes solo existía el backend)
- Nueva página pública **`/servicios`**: listado de servicios profesionales con buscador (`SearchBar`), filtro por categoría (`Sidebar` + `SidebarFilters`) y botón **"Solicitar Cotización"** para usuarios logueados. También muestra **"Mis Cotizaciones"** del usuario actual.
- **Perfil profesional** `/perfil`: sección **"Mis Servicios"** para publicar servicios y ver las **cotizaciones recibidas** por servicio.

### 10. Componentes conectados / limpieza
- Conectados: `Button` (nuevos formularios), `SearchBar` y `Sidebar`/`SidebarFilters` (página `/servicios`), `UserCard` (postulantes en `/empresa/ofertas`).
- Eliminados (demos con datos hardcodeados sin uso): `JobCard.jsx` y `CompanyCard.jsx`.
- Se agregó la ruta `/servicios` y su enlace en la Navbar.

### 11. Verificación final
- `npm run build` correcto en el frontend y compilación `mvn -q compile -o` de los 6 microservicios.
- Flujo verificado en vivo vía proxy (`localhost:5173/api`): login con JWT, creación de servicio profesional, solicitud de cotización, publicación de reseña, listados públicos y protección 401 en rutas privadas.

### 12. Validación de propiedad (ownership) en el backend
- **Regla**: cada usuario solo puede crear/modificar/eliminar **sus propios** recursos (o ADMIN por bypass). Antes, el backend confiaba en los IDs enviados por body.
- `usuarios`: `POST /api/users/register` **no permite registrarse como ADMIN** (solo PROFESSIONAL, COMPANY o CUSTOMER); `POST /api/roles` y la gestión de usuarios quedan solo ADMIN.
- `perfiles` (Company/Professional/Customer): `create` fuerza `userId` = usuario autenticado (salvo admin) y exige que el rol del token coincida con el tipo de perfil (COMPANY/PROFESSIONAL/CUSTOMER); `update`/`delete` requieren ser el dueño del perfil o admin.
- `resenias`: `createReview` fuerza `customerId` del JWT, valida que el profesional exista (cross-schema `perfiles`), prohíbe auto-reseña (self-review) y duplicados por (customerId, professionalId); `update`/`delete` requieren el autor o admin.
- `catalogo`: escrituras de categorías y skills → solo ADMIN. `professional-service`/`company-service`: `create` exige rol PROFESSIONAL/COMPANY y un perfil propio (verificado contra `perfiles`); `update`/`delete` solo del dueño o admin.
- `ofertas` (JobOffer / JobApplication / Quotation): el esquema desplegado (migraciones Flyway `V1__init_schema.sql` de cada servicio) SÍ tiene columnas `user_id` (FK a `usuarios.app_user`) en `job_offer`, `application_table` y `quotation`, y el ownership se valida con esas columnas directas (`offer.getUserId()`), además de joins cross-schema donde hace falta (p. ej. `professional_id` de un servicio → `perfiles`). `create` de ofertas exige COMPANY y empresa propia; postulaciones exigen PROFESSIONAL, perfil propio y prohíben postularse a ofertas de la propia empresa; cotizaciones fuerzan el `user_id` del JWT y prohíben solicitudes sobre un servicio propio. `update`/`delete`/`accept`/`reject` y los reads sensibles (postulaciones por oferta, cotizaciones por servicio/usuario, etc.) verifican el dueño real o ADMIN.
- **Importante** (origen del modelo): la carpeta `DATABASE/` es el esquema **histórico** del proyecto (numeración 01-18) y hoy **no se ejecuta**: el schema en runtime lo generan las migraciones **Flyway** embebidas en cada microservicio (`BACKEND/<servicio>/src/main/resources/db/migration`, V1 init + V2 seed), que son la fuente de verdad. `DATABASE/ofertas/` se actualizó para reflejarlo (columnas `user_id`).
- Verificado en vivo: editar/borrar oferta ajena → 403; ver postulaciones de oferta ajena → 403; ver cotizaciones de otro → 403; editar reseña de otro → 403; editar perfil de otro → 403; `GET /api/users` con rol no-admin → 403; y los casos legítimos (dueño) siguen funcionando (200/201).

### 13. Endurecimiento adicional (severidad media)
- **Canal interno de notificaciones**: `POST /api/notifications/internal` validado con header `X-Internal-Service-Key` (variable `INTERNAL_SERVICE_KEY`, de la que hoy dependen otros microservicios); `POST /api/notifications` directo queda solo para ADMIN y el `message` se limita a 500 caracteres.
- **PII en perfiles públicos**: los `GET` de `company-profiles` y `professional-profiles` siguen siendo públicos (marketplace) pero enmascaran los datos sensibles (`email`, `phone`, `address`, `tax_id`, `userId`, `portfolio_url`, `linkedin_url`, `github_url`) salvo para el dueño o ADMIN (token opcional en el GET). El frontend solo consume `companyName`/`firstName`/`lastName`/`city` por esas rutas.
- **Gateway**: las rutas públicas ahora son por método y exactas (`POST /api/auth/login`, `POST /api/users/register`) más los `GET` públicos del marketplace (`job-offers`, `reviews`, `catalogo`, `company-profiles`, `professional-profiles`); ya no se abre por prefijo ajeno.
- **Login anti fuerza bruta**: en `usuarios`, 5 intentos fallidos por email → bloqueo temporal de 15 minutos (en memoria), se limpia al loguear bien.
- **Ofertas**: nuevo `PATCH /api/job-offers/{id}/status` (dueño de la empresa o ADMIN; `ACTIVE|INACTIVE|SUSPENDED|DELETED`); `accept()` de postulación revalida que la oferta siga `ACTIVE`; una cotización solo se puede editar en estado `PENDING`.
- **Baja de usuario**: `DELETE /api/users/{id}` (soft delete) además desactiva en cascada sus perfiles y ofertas de su empresa y elimina sus notificaciones (mismo esquema, cross-schema), evitando recursos activos huérfanos.

### 14. Bajo (A) + CORS
- **`catalogo` (categories/skills)**: los endpoints de escritura ya no castean ciegamente el body (`(String) body.get(...)`); un `name`/`description` de tipo incorrecto devuelve **400** en vez de **500**. Se agregaron `PUT` y `DELETE` de skills (solo ADMIN), con `404`/`409` correctos.
- **CORS**: se configuró `http://localhost:5173` y `http://127.0.0.1:5173` en los 6 microservicios (bean `CorsConfigurationSource`) y en el gateway (`spring.cloud.gateway.globalcors`). El proxy del frontend sigue siendo el camino habitual del dev.

### 15. Rutas `/me` (alias por JWT)
- `GET /api/professional-profiles/me` (_perfiles_), `GET /api/reviews/me` (_resenias_) y `GET /api/notifications/me` + `/me/unread` (_notificaciones_) resuelven el `userId` desde el token y devuelven **tus** datos sin pasar ids en la URL.
- En `perfiles` y `resenias` se agregó un matcher `.authenticated()` específico para `/me` **antes** del `permitAll` de su familia de rutas (así `/me` exige token y los listados públicos siguen abiertos). En `notificaciones` ya estaba cubierto por `anyRequest().authenticated()`.
- Sin cambios en el frontend (sigue usando `/user/{userId}` desde `localStorage`).
