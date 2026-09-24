import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ImagePlaceholder from '../../components/ImagePlaceholder';
import perfilesService from '../../services/perfilesService';
import catalogoService from '../../services/catalogoService';
import './Landing.css';

const CATEGORIES = [
    { name: 'Programación & Tech', icon: '💻', accent: 'coral' },
    { name: 'Diseño Gráfico', icon: '🎨', accent: 'gold' },
    { name: 'Marketing Digital', icon: '📈', accent: 'navy' },
    { name: 'Escritura & Traducción', icon: '✍️', accent: 'coral' },
    { name: 'Video & Animación', icon: '🎬', accent: 'gold' },
    { name: 'IA & Automatización', icon: '🤖', accent: 'navy' },
    { name: 'Música & Audio', icon: '🎵', accent: 'coral' },
    { name: 'Negocios & Finanzas', icon: '💼', accent: 'gold' },
    { name: 'Consultoría', icon: '🧑‍💼', accent: 'navy' },
];

const POPULAR_SERVICES = [
    'Desarrollo Web',
    'Edición de Video',
    'Diseño de Logos',
    'Marketing en Redes',
    'Análisis de Datos',
    'Redacción SEO',
    'Automatización IA',
    'Ciberseguridad',
    'Desarrollo de Software',
    'Diseño UX/UI',
    'Community Management',
    'Crecimiento y Ventas',
];

const HERO_POPULAR = [
    'Desarrollo Web',
    'Edición de Video',
    'Diseño de Logos',
    'Marketing Digital',
    'Automatización IA',
];

const TRUSTED_COMPANIES = [
    'Logo Empresa A',
    'Logo Empresa B',
    'Logo Empresa C',
    'Logo Empresa D',
    'Logo Empresa E',
];

const USP_ITEMS = [
    {
        icon: '🌐',
        title: 'Accedé a un pool de talento experto',
        subtitle: 'Más de 30 categorías con profesionales verificados y listos para trabajar.',
    },
    {
        icon: '⚡',
        title: 'Una experiencia de match simple',
        subtitle: 'Buscá, compará y conectá en minutos con el perfil ideal para tu proyecto.',
    },
    {
        icon: '💎',
        title: 'Calidad, rapidez y presupuesto',
        subtitle: 'Trabajo de calidad entregado a tiempo y dentro del presupuesto pactado.',
    },
    {
        icon: '🔒',
        title: 'Solo pagás cuando estás satisfecho',
        subtitle: 'Fondos en resguardo hasta que confirmes que el resultado te encanta.',
    },
];

const SOURCING_BENEFITS = [
    'Trabajá con expertos que buscan, entrevistan y evalúan talento por vos.',
    'Recibí un informe con recomendaciones claras y fundamentadas.',
    'Contratá talento verificado con total confianza.',
];

const GIG_TOP_PROVIDERS = [
    { initials: 'TR', tone: 'coral' },
    { initials: 'MC', tone: 'navy' },
    { initials: 'LS', tone: 'gold' },
    { initials: 'DP', tone: 'coral' },
];

const GUIDES = [
    { title: 'Empezá un negocio paralelo' },
    { title: 'Ideas de negocio de ecommerce' },
    { title: 'Trabajá desde casa con un negocio online' },
    { title: 'Construí una web desde cero' },
    { title: 'Hacé crecer tu negocio con IA' },
    { title: 'Creá un logo para tu negocio' },
];

const getInitials = (name = '') => {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    const first = parts[0]?.[0] || '';
    const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
    return (first + last).toUpperCase() || 'B2B';
};

export const Landing = () => {
    const navigate = useNavigate();
    const [search, setSearch] = useState('');
    const [services, setServices] = useState([]);
    const [professionals, setProfessionals] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadLandingData = async () => {
            try {
                const [proServices, profs] = await Promise.all([
                    catalogoService.getProfessionalServices(),
                    perfilesService.getProfessionalProfiles(),
                ]);
                setServices((proServices || []).slice(0, 8));
                setProfessionals(profs || []);
            } catch (err) {
                console.error('Error cargando datos del landing:', err);
            } finally {
                setLoading(false);
            }
        };

        loadLandingData();
    }, []);

    const getProviderName = (professionalId) => {
        const prof = professionals.find((p) => p.id === professionalId);
        if (prof?.firstName || prof?.lastName) return `${prof.firstName || ''} ${prof.lastName || ''}`.trim();
        return `Profesional #${professionalId}`;
    };

    const handleSearch = (e) => {
        e.preventDefault();
        navigate(search.trim() ? `/servicios?q=${encodeURIComponent(search.trim())}` : '/servicios');
    };

    return (
        <div className="fv-home">
            {/* =====================================================
                HERO — banda oscura con headline, buscador y chips
            ===================================================== */}
            <section className="fv-hero">
                <div className="fv-hero-layout">
                    <div className="fv-hero-copy">
                        <h1 className="fv-hero-title">
                            Nuestros freelancers
                            <br />
                            <span className="fv-hero-title-accent">se encargan del resto</span>
                        </h1>

                        <form className="fv-hero-search" onSubmit={handleSearch} role="search">
                            <svg
                                className="fv-hero-search-icon"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                aria-hidden="true"
                            >
                                <circle cx="11" cy="11" r="7" />
                                <path d="m21 21-4.3-4.3" />
                            </svg>
                            <input
                                type="text"
                                placeholder="Buscalo: desarrollo web, diseño, marketing…"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                aria-label="Buscar servicios"
                            />
                            <button type="submit" className="fv-hero-search-btn">
                                Buscar
                            </button>
                        </form>

                        <div className="fv-hero-popular">
                            {HERO_POPULAR.map((service, i) => (
                                <Link
                                    key={service}
                                    to={`/servicios?q=${encodeURIComponent(service)}`}
                                    className={`fv-chip fv-chip--hero ${i === 0 ? 'fv-chip--hot' : ''}`}
                                >
                                    {i === 0 && <span aria-hidden="true">🔥</span>}
                                    {service}
                                </Link>
                            ))}
                        </div>
                    </div>

                    <div className="fv-hero-media">
                        <ImagePlaceholder
                            label="Imagen del hero"
                            hint="Ilustración de la plataforma / profesionales"
                            className="img-placeholder--dark"
                            style={{ minHeight: 360 }}
                        />
                    </div>
                </div>

                {/* Confianza */}
                <div className="fv-trusted">
                    <p className="fv-trusted-label">Con la confianza de:</p>
                    <div className="fv-trusted-logos">
                        {TRUSTED_COMPANIES.map((company) => (
                            <div key={company} className="fv-trusted-logo">
                                <ImagePlaceholder
                                    label={company}
                                    hint="Logo"
                                    className="img-placeholder--dark img-placeholder--logo"
                                    style={{ minHeight: 56, minWidth: 140 }}
                                />
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* =====================================================
                CATEGORÍAS — grid de tarjetas con ícono
            ===================================================== */}
            <section className="fv-categories">
                <div className="fv-container">
                    <div className="fv-category-grid">
                        {CATEGORIES.map((category) => (
                            <Link key={category.name} to="/servicios" className="fv-category-card">
                                <span className="fv-category-icon">{category.icon}</span>
                                <span className="fv-category-name">{category.name}</span>
                            </Link>
                        ))}
                    </div>
                </div>
            </section>

            {/* =====================================================
                SERVICIOS POPULARES — rail de chips
            ===================================================== */}
            <section className="fv-section">
                <div className="fv-container">
                    <h2 className="fv-section-title">Servicios populares</h2>
                    <div className="fv-chip-rail">
                        {POPULAR_SERVICES.map((service, i) => (
                            <Link
                                key={service}
                                to={`/servicios?q=${encodeURIComponent(service)}`}
                                className={`fv-chip ${i === 0 ? 'fv-chip--hot' : ''}`}
                            >
                                {i === 0 && <span aria-hidden="true">🔥</span>}
                                {service}
                            </Link>
                        ))}
                    </div>
                </div>
            </section>

            {/* =====================================================
                SERVICIOS DESTACADOS — gig cards (dinámico)
            ===================================================== */}
            <section className="fv-section">
                <div className="fv-container">
                    <div className="fv-section-header">
                        <div>
                            <h2 className="fv-section-title">Servicios destacados</h2>
                            <p className="fv-section-subtitle">Profesionales verificados listos para cotizarte hoy.</p>
                        </div>
                        <Link to="/servicios" className="btn-b2b-outline fv-see-all">
                            Ver todos los servicios
                        </Link>
                    </div>

                    {loading ? (
                        <div className="card-b2b">
                            <p style={{ margin: 0 }}>Cargando servicios...</p>
                        </div>
                    ) : services.length === 0 ? (
                        <div className="card-b2b">
                            <p style={{ margin: 0, color: 'var(--text-muted)' }}>No hay servicios publicados todavía.</p>
                        </div>
                    ) : (
                        <div className="fv-gig-grid">
                            {services.map((service, index) => {
                                const providerName = getProviderName(service.professional_id);
                                const tone = GIG_TOP_PROVIDERS[index % GIG_TOP_PROVIDERS.length];
                                return (
                                    <article key={service.id} className="fv-gig">
                                        <ImagePlaceholder
                                            label="Portada del servicio"
                                            hint="Imagen del trabajo"
                                            className="img-placeholder--square"
                                        />
                                        <div className="fv-gig-body">
                                            <div className="fv-gig-provider">
                                                <span className={`fv-gig-avatar fv-gig-avatar--${tone.tone}`}>
                                                    {getInitials(providerName)}
                                                </span>
                                                <div className="fv-gig-provider-meta">
                                                    <p className="fv-gig-provider-name">{providerName}</p>
                                                    <span className="badge-verified">✓ Profesional verificado</span>
                                                </div>
                                                <span className="fv-gig-heart" aria-hidden="true">♥</span>
                                            </div>
                                            <h3 className="fv-gig-title">{service.title}</h3>
                                            <p className="fv-gig-desc">{service.description || 'Sin descripción.'}</p>
                                            <div className="fv-gig-bottom">
                                                <div className="fv-gig-rating">
                                                    <svg
                                                        viewBox="0 0 24 24"
                                                        fill="currentColor"
                                                        aria-hidden="true"
                                                        className="fv-star"
                                                    >
                                                        <path d="M12 2l2.9 6.3 6.9.8-5.1 4.7 1.4 6.8L12 17.3 5.9 20.6l1.4-6.8L2.2 9.1l6.9-.8L12 2z" />
                                                    </svg>
                                                    <span>5.0</span>
                                                    <span className="fv-gig-reviews">(12)</span>
                                                </div>
                                                <div className="fv-gig-price">
                                                    <small>Desde</small>
                                                    <span>{service.price != null ? `$${service.price}` : 'A convenir'}</span>
                                                </div>
                                            </div>
                                            <Link to="/servicios" className="btn-pill">Solicitar cotización</Link>
                                        </div>
                                    </article>
                                );
                            })}
                        </div>
                    )}
                </div>
            </section>

            {/* =====================================================
                USP — "Hacé que todo suceda con freelancers"
            ===================================================== */}
            <section className="fv-usp">
                <div className="fv-container fv-usp-layout">
                    <div className="fv-usp-copy">
                        <h2 className="fv-usp-title">Hacé que todo suceda con freelancers</h2>
                        <div className="fv-usp-list">
                            {USP_ITEMS.map((item) => (
                                <div key={item.title} className="fv-usp-item">
                                    <span className="fv-usp-item-icon" aria-hidden="true">{item.icon}</span>
                                    <div>
                                        <h4>{item.title}</h4>
                                        <p>{item.subtitle}</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <Link to="/register" className="btn-b2b-accent fv-usp-cta">
                            Únete ahora
                        </Link>
                    </div>
                    <div className="fv-usp-media">
                        <ImagePlaceholder label="Imagen USP" hint="Profesionales alcanzando sus objetivos" />
                    </div>
                </div>
            </section>

            {/* =====================================================
                SELECCIÓN DE EXPERTOS
            ===================================================== */}
            <section className="fv-sourcing">
                <div className="fv-container fv-sourcing-layout">
                    <div className="fv-sourcing-media">
                        <ImagePlaceholder label="Imagen selección de expertos" hint="Equipo de expertos evaluando talento" />
                    </div>
                    <div className="fv-sourcing-copy">
                        <span className="fv-eyebrow">Selección de expertos</span>
                        <h2 className="fv-sourcing-title">
                            Dejá que los expertos encuentren el talento adecuado para vos
                        </h2>
                        <ul className="fv-sourcing-list">
                            {SOURCING_BENEFITS.map((benefit) => (
                                <li key={benefit}>
                                    <span className="fv-check" aria-hidden="true">✓</span>
                                    {benefit}
                                </li>
                            ))}
                        </ul>
                        <Link to="/register" className="btn-b2b-primary fv-sourcing-cta">
                            Descubrí la selección de expertos
                        </Link>
                    </div>
                </div>
            </section>

            {/* =====================================================
                GARANTÍA
            ===================================================== */}
            <section className="fv-guarantee">
                <div className="fv-container fv-guarantee-layout">
                    <ImagePlaceholder
                        label="Imagen garantía"
                        hint="Escudo de garantía"
                        className="img-placeholder--shield"
                        style={{ minWidth: 120, minHeight: 120, maxWidth: 160 }}
                    />
                    <div className="fv-guarantee-copy">
                        <h3>Garantía de reembolso del 100%</h3>
                        <p>Satisfacción asegurada o te devolvemos tu dinero. Tu tranquilidad es nuestra prioridad.</p>
                    </div>
                </div>
            </section>

            {/* =====================================================
                HISTORIA DE ÉXITO
            ===================================================== */}
            <section className="fv-success">
                <div className="fv-container fv-success-layout">
                    <div className="fv-success-media">
                        <ImagePlaceholder label="Imagen historia de éxito" hint="Empresa cliente" />
                    </div>
                    <div className="fv-success-copy">
                        <span className="fv-eyebrow">Historias de éxito</span>
                        <h2 className="fv-success-title">Así se ve el éxito en B2BMatch</h2>
                        <p className="fv-success-text">
                            Empresas de todo el mundo confían en nuestros profesionales para hacer realidad su visión.
                        </p>
                        <Link to="/servicios" className="btn-b2b-outline fv-success-cta">
                            Ver historias de éxito
                        </Link>
                    </div>
                </div>
            </section>

            {/* =====================================================
                GUÍAS PARA CRECER
            ===================================================== */}
            <section className="fv-section">
                <div className="fv-container">
                    <div className="fv-section-header">
                        <h2 className="fv-section-title">Guías para hacer crecer tu negocio</h2>
                        <Link to="/register" className="fv-see-more">
                            Ver más guías <span aria-hidden="true">→</span>
                        </Link>
                    </div>

                    <div className="fv-guides-grid">
                        {GUIDES.map((guide) => (
                            <article key={guide.title} className="fv-guide">
                                <ImagePlaceholder
                                    label={`Imagen: ${guide.title}`}
                                    hint="Miniatura de la guía"
                                    className="img-placeholder--square"
                                />
                                <div className="fv-guide-body">
                                    <h4>{guide.title}</h4>
                                </div>
                            </article>
                        ))}
                    </div>
                </div>
            </section>

            {/* =====================================================
                CTA FINAL — banda oscura
            ===================================================== */}
            <section className="fv-final-cta">
                <div className="fv-container fv-final-cta-inner">
                    <h2>Servicios profesionales a tu alcance</h2>
                    <p>Empezá hoy: encontrá el talento ideal o comenzá a ofrecer tus servicios.</p>
                    <Link to="/register" className="btn-b2b-primary fv-final-cta-btn">
                        Únete a B2BMatch
                    </Link>
                </div>
            </section>
        </div>
    );
};

export default Landing;