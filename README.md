# ExpertConnect — Marketplace B2B de Servicios Profesionales

Plataforma web que conecta empresas, trabajadores independientes y usuarios para la contratación de servicios profesionales.

---

## Tecnologías Usadas

| Tecnología | Versión | Uso |
|---|---|---|
| **HTML5** | — | Estructura de todas las páginas |
| **CSS3** | — | Estilos globales y específicos |
| **JavaScript** | Vanilla ES6 | Lógica frontend (validación, persistencia, routing) |
| **Bootstrap** | 5.3.x | Layout responsivo, modales, cards, grid |
| **Google Fonts** | Fira Sans, Fira Mono | Tipografía principal |
| **Material Symbols** | — | Iconografía |
| **localStorage** | Web API | Persistencia de datos simulada |
| **CSS Custom Properties** | — | Sistema de variables de diseño |
| **CSS Grid / Flexbox** | — | Layout responsivo |
| **Media Queries** | CSS3 | 3 breakpoints (980px, 900px, 768px, 600px, 480px) |
| **Git / GitHub** | — | Control de versiones y repositorio remoto |

> **Nota:** No hay backend real. Toda la persistencia es simulada con `localStorage`.

---

## Arquitectura del Proyecto

```
Proyecto_Integrador_Marketplace/
│
├── index.html                 # Landing page (hero, buscador IA, cómo funciona, registro CTA)
├── login.html                 # Login y registro combinados (toggle)
├── registro.html              # Redirige a login.html?mode=register
│
├── assets/
│   ├── css/
│   │   └── style.css          # Estilos globales (variables, nav, hero, footer, responsive)
│   └── js/
│       ├── script.js          # Buscador IA, datos mock, manejo de sesión en index
│       └── login.js           # Lógica de login/registro, validación, roles, redirect
│
├── usuario/                   # Rol: usuario (solo navega y contrata)
│   ├── perfil.html            # Perfil básico (nombre, rol)
│   ├── ofertas.html           # Ofertas de trabajo para postular
│   └── estilos.css            # Estilos de tarjetas de ofertas
│
├── trabajador/                # Rol: trabajador (navega, contrata y ofrece servicios)
│   ├── perfil.html            # Perfil con nombre, rol, rubro, habilidades, descripción
│   ├── ofertas.html           # Ofertas de trabajo para postular
│   └── estilos.css            # Estilos de tarjetas de ofertas
│
├── empresa/                   # Rol: empresa (oferta servicios B2B)
│   ├── perfil.html            # Perfil de empresa con mini-tarjetas de servicios editables
│   ├── ofertas-publicadas.html # Ofertas B2B con cotización y modal de detalle
│   ├── perfilEmpresa.css      # Estilos del perfil empresa
│   ├── ofertasEmpresa.css     # Estilos de ofertas B2B
│   └── perfilEmpresa.js       # (reservado para lógica futura)
│
└── admin/                     # Rol: admin (gestión de plataforma)
    ├── editor-empresas.html   # Listado de empresas con filtros y buscador
    ├── editor-trabajadores.html # Listado de trabajadores con filtros y buscador
    └── editor-usuarios.html   # Listado de usuarios con filtros y buscador
```

---

## Flujo de Navegación

```
[Index] ──→ [Login] ──→ [Según rol:]
                │              │
                │              ├── admin      → admin/editor-empresas.html
                │              ├── usuario    → usuario/perfil.html
                │              ├── trabajador → trabajador/perfil.html
                │              └── empresa    → empresa/perfil.html
                │
                └── Registro (toggle)
                     │
                     └── Elegir rol: Usuario | Trabajador | Empresa
```

### Diagrama de flujo de autenticación

```
   Usuario                    login.html                     localStorage
     │                           │                              │
     │  Ingresa email+pass       │                              │
     │ ─────────────────────────→│                              │
     │                           │  Busca en expertConnect_users │
     │                           │ ─────────────────────────────→│
     │                           │  ←─── usuario encontrado ─────│
     │                           │                              │
     │                           │  Guarda sesión:              │
     │                           │  • expertConnect_isLoggedIn   │
     │                           │  • expertConnect_userEmail    │
     │                           │  • expertConnect_userName     │
     │                           │  • userRole                   │
     │                           │  • userName                   │
     │                           │                              │
     │  Redirige según rol       │                              │
     │ ←─────────────────────────│                              │
```

---

## Sistema de Roles

| Rol | Email demo | Contraseña | Puede navegar | Puede contratar | Puede ofrecer servicios |
|---|---|---|---|---|---|
  | **admin** | `admin@test.com` | `admin123` | ✅ | ❌ | ❌ (gestiona) |
  | **usuario** | `test@example.com` | `123456` | ✅ | ✅ | ❌ |
  | **trabajador** | `trabajador@test.com` | `123456` | ✅ | ✅ | ✅ |
  | **empresa** | `empresa@test.com` | `123456` | ✅ | ✅ | ✅ (B2B) |

### Mapa de redirección

| Rol | Ruta |
|---|---|
| `admin` | `admin/editor-empresas.html` |
| `usuario` | `usuario/perfil.html` |
| `trabajador` | `trabajador/perfil.html` |
| `empresa` | `empresa/perfil.html` |

---

## Registro

El formulario de registro permite elegir entre 3 tipos de cuenta:

- **Usuario** — Solo puede navegar y contratar servicios
- **Trabajador / Freelance** — Puede navegar, contratar y ofrecer servicios
- **Empresa** — Puede navegar, contratar y ofrecer servicios B2B

> El rol **admin** no está disponible en el registro. Solo existe como usuario semilla en el código.

---

## Persistencia (localStorage)

| Clave | Descripción |
|---|---|
| `expertConnect_users` | Array de usuarios registrados `[{name, email, password, role}]` |
| `expertConnect_isLoggedIn` | `"true"` si hay sesión activa |
| `expertConnect_userEmail` | Email del usuario logueado |
| `expertConnect_userName` | Nombre del usuario logueado |
| `userName` | Alias para compatibilidad con perfiles |
| `userRole` | Rol del usuario (`admin`, `usuario`, `trabajador`, `empresa`) |
| `userRubro` | Rubro profesional (trabajador) |
| `userHabilidades` | Habilidades (trabajador) |
| `userDescripcion` | Sobre mí (trabajador) |
| `userOtros` | Información adicional (empresa) |

---

## Estados del Admin

Cada panel de administración tiene 4 filtros de estado:

- **Activo** — Usuario verificados y activos
- **Pendiente** — Pendientes de revisión
- **Suspendido** — Suspendidos temporalmente
- **Todos** — Sin filtro

Además incluye buscador por nombre o correo y estadísticas numéricas.

---

---

## Responsive Design

La plataforma se adapta a 3 tamaños de pantalla con 5 breakpoints:

| Breakpoint | Target | Cambios principales |
|---|---|---|
| `≤ 980px` | Tablet horizontal | Estadísticas admin → 2 columnas |
| `≤ 900px` | Tablet vertical | Nav colapsa, menú hamburguesa visible, hero se achica, footer → 2 columnas, grid de pasos → 1 columna |
| `≤ 768px` | Tablet chica / Mobile grande | Tarjetas de ofertas → columna (stack vertical), botones `width: 100%` |
| `≤ 600px` | Mobile mediano | Hero title 34px, footer → 1 columna, estadísticas → 1 columna |
| `≤ 480px` | Mobile chico | Márgenes reducidos, padding ajustado, fuentes más pequeñas |

### Menú Hamburguesa

En **pantallas ≤ 900px** la navegación se oculta y aparece un botón `☰`. Al pulsarlo, el menú se despliega con una animación de `translateY` + `opacity`. Está implementado en **11 páginas** (index, login, usuario, trabajador, empresa, admin).

- **CSS:** `style.css` — `.hamburger` con `display:none` en desktop, menú posicionado absoluto debajo del nav
- **JS:** `script.js` y `login.js` — toggle de clase `.open` en `.nav-links` (sin dependencia de DOMContentLoaded)
- **Requisito:** `<button class="hamburger">☰</button>` dentro de cada `.nav`

## Funcionalidades Clave

- **Buscador IA simulado** en index.html con sugerencias (chips) y filtros
- **Login/Registro** con validación en tiempo real y toggle de modo
- **Roles diferenciados** con redirección por tipo de cuenta
- **Perfiles editables** con persistencia en localStorage
- **Ofertas de trabajo** con botón de postulación (feedback visual)
- **Ofertas B2B** con solicitud de cotización y modal de detalle
- **Paneles admin** con listados, filtros, búsqueda y estadísticas
- **Mini-tarjetas de servicios** editables (empresa)
- **Responsive design** adaptable a móvil/tablet/desktop con 5 breakpoints
- **Menú hamburguesa** en todas las páginas para navegación mobile
- **Breakpoint extra (480px)** en sub-páginas para pantallas muy pequeñas
- **Botones adaptables** con `min-width` + `width: 100%` en mobile
- **Cierre de sesión** con limpieza completa de datos
