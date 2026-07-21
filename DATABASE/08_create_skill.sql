-- ================================================
-- Tabla: skill
-- Descripción: Catálogo de habilidades y competencias profesionales que pueden poseer los usuarios.
-- Utilizada por: professional_skill
-- ================================================


CREATE TABLE skill (

    id BIGSERIAL PRIMARY KEY,
    
    name VARCHAR(100) NOT NULL UNIQUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);


INSERT INTO skill (name) VALUES

-- Recursos humanos / DO
('Gestión del Cambio Organizacional'),
('Desarrollo Organizacional'),
('Clima Organizacional'),
('Evaluación de Riesgos Psicosociales'),
('Coaching Ejecutivo'),
('Desarrollo de Liderazgo'),
('Gestión por Competencias'),
('Outplacement'),
('Levantamiento de Perfiles'),
('Análisis y Descripción de Cargos'),
('Gestión Documental'),
('Gestión de Licitaciones'),
('Compliance'),
('Normas ISO'),
('Mejora Continua'), -- algo pasa que solo se agrego hasta aca el insert
--recursos humanos / reclutamiento
('Reclutamiento'), 
('Selección de Personal'),
('Adquisición de Talento'),
('Entrevistas por Competencias'),
('Evaluación Psicolaboral'),
('Aplicación de Pruebas Psicométricas'),
('Headhunting'),
('Marca Empleadora'),
('Capacitación de Personal'),
('Evaluación de Desempeño'),
('Gestión del Talento'),
('Administración de Personal'),
('Legislación Laboral'),
('Consultoría en Recursos Humanos'),
('Onboarding'),
-- Consultoría Empresarial
('Consultoría Empresarial'),
('Planificación Estratégica'),
('Gestión de Proyectos'),
('Mejora de Procesos'),
('Gestión del Cambio'),
('Análisis de Negocios'),
('Cumplimiento Normativo'),
('Aseguramiento de Calidad'),
('Capacitación Empresarial'),
('Optimización de Procesos'),

-- Logística
('Gestión de Cadena de Suministro'),
('Gestión de Inventarios'),
('Gestión de Bodegas'),
('Abastecimiento'),
('Gestión de Proveedores'),
('Planificación de Transporte'),
('Gestión de Flotas'),
('Importaciones y Exportaciones'),
('Planificación de la Demanda'),
('Distribución'),
('Control de Calidad'),
('Gestión de Operaciones'),
('Planificación de Producción'),
('Gestión de Riesgos'),
('Logística Internacional'),

-- Marketing
('Investigación de Mercado'),
('Estrategia de Marca'),
('Marketing Digital'),
('Creación de Contenido'),
('Posicionamiento SEO'),
('Publicidad SEM'),
('Gestión de Redes Sociales'),
('Analítica de Marketing'),
('Campañas Publicitarias'),
('SEO'),
('SEM'),
('Google Ads'),
('Meta Ads'),
('Email Marketing'),
('Google Analytics'),
('Content Marketing'),

-- Finanzas y Contabilidad
('Contabilidad General'),
('Análisis Financiero'),
('Preparación de Impuestos'),
('Planificación Presupuestaria'),
('Análisis de Costos'),
('Informes Financieros'),
('Auditoría'),
('Remuneraciones'),
('Gestión de Flujo de Caja'),
('Consultoría Contable'),

-- Diseño
('Diseño Gráfico'),
('Diseño de Interfaces'),
('Investigación UX'),
('Identidad Corporativa'),
('Diseño de Presentaciones'),
('Edición de Video'),
('Fotografía'),
('Ilustración'),
('Animación Digital'),
('Diseño de Contenidos'),

-- Servicio al Cliente
('Atención al Cliente'),
('Soporte Técnico'),
('Gestión de Clientes'),
('Resolución de Conflictos'),
('Consultoría Comercial'),
('Gestión de Cuentas'),
('Éxito del Cliente'),
('Servicio Postventa'),

-- tecnologÍa E IT
('Soporte TI'),
('Administración de Redes'),
('Ciberseguridad'),
('Computación en la Nube'),
('Administración de Bases de Datos'),
('Análisis de Sistemas'),
('Pruebas de Software'),
('Inteligencia de Negocios'),
('Análisis de Datos'),
('Documentación Técnica'),
('Implementación de ERP'),
('Gestión de CRM'),
('Consultoría TI'),
('Transformación Digital'),
('Automatización de Procesos'),

-- Programación
('Java'),
('Spring Boot'),
('Hibernate'),
('JavaScript'),
('TypeScript'),
('Python'),
('C#'),
('PHP'),
('C++'),

-- Frontend
('HTML5'),
('CSS3'),
('React'),
('Angular'),
('Vue.js'),
('Bootstrap'),
('Tailwind CSS'),

-- Bases de datos
('PostgreSQL'),
('MySQL'),
('MongoDB'),
('SQL'),

-- DevOps / Cloud
('Docker'),
('Git'),
('GitHub'),
('AWS'),
('Azure'),

-- Mobile
('Android'),
('Kotlin'),
('Flutter'),
('React Native'),

-- Diseño
('Figma'),
('Adobe XD'),
('Photoshop'),
('Illustrator'),
('Canva'),

-- Gestión
('Scrum'),
('Agile'),
('Project Management'),

-- Finanzas
('Microsoft Excel'),
('Financial Analysis'),
('Bookkeeping'),
('Payroll Management'),

-- Comunicación
('Customer Service'),
('Technical Writing'),
('Public Speaking'),

-- Idiomas
('English'),
('Spanish');