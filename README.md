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
└── DATABASE/            # Scripts SQL por microservicio
```

## Roles

| Rol | Registro | Puede hacer |
|---|---|---|
| **ADMIN** | No (solo seed) | Dashboard, aprobar/bloquear empresas, suspender usuarios, bajar ofertas |
| **PROFESSIONAL** | Sí ("Postulante") | Ver ofertas, completar perfil profesional, postularse, ver postulaciones, publicar servicios y ver cotizaciones recibidas |
| **COMPANY** | Sí ("Empresa") | Completar perfil de empresa, crear/editar/eliminar ofertas, ver postulantes de sus ofertas y dejarles reseñas |
| **CUSTOMER** | — | Consulta de ofertas |

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
- **Regla**: cada usuario solo puede modificar/eliminar **sus propios** recursos (o ADMIN por bypass). Antes, el backend aceptaba editar cualquier recurso por ID.
- `usuarios` (`AppUserController`): `GET /api/users`, `GET /api/users/role/{role}`, `DELETE`, `reactivate` y `status` → solo ADMIN; `GET /api/users/{id}` → admin o el propio usuario (403 en otro caso).
- `perfiles` (Company/Professional/Customer): `create` fuerza `userId` = usuario autenticado (salvo admin); `update`/`delete` requieren ser el dueño del perfil o admin.
- `resenias` (`ReviewController`): `createReview` fuerza `customerId` = usuario autenticado; `update`/`delete` requieren el autor de la reseña o admin.
- `notificaciones` (`NotificationController`): consultar/marcar/eliminar → solo el dueño o admin; crear sigue siendo público para el sistema.
- `ofertas`: las ofertas ahora guardan el **dueño** (`job_offer.user_id`) y las postulaciones también (`application_table.user_id`); `update`/`delete`/`updateStatus` de ofertas exigen ser el dueño o admin; `JobApplicationController`/`QuotationController` exigen dueño (postulante/empresa dueña de la oferta / solicitante de la cotización) o admin. `create` de ofertas/postulaciones/cotizaciones fuerza el `userId` desde el JWT.
- **Migración SQL aplicada** en la BD en vivo (y en los DDL de `DATABASE/ofertas/`): `ALTER TABLE job_offer ADD COLUMN user_id BIGINT NOT NULL` y `application_table ADD COLUMN user_id BIGINT NOT NULL`, con backfill del dueño seed.
- Se agregó el handler de `ResponseStatusException` en los `GlobalExceptionHandler` de todos los MS para que el 403 se devuelva como 403 real (antes terminaba como 401/404/500 por el re-dispatch de error).
- Verificado en vivo: editar/borrar oferta ajena → 403; ver postulaciones de oferta ajena → 403; ver cotizaciones de otro → 403; editar reseña de otro → 403; editar perfil de otro → 403; `GET /api/users` con rol no-admin → 403; y los casos legítimos (dueño) siguen funcionando (200/201).
