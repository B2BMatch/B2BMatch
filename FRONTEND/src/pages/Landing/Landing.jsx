import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getOfertas } from '../../services/ofertasService';
import perfilesService from '../../services/perfilesService';
import catalogoService from '../../services/catalogoService';
import './Landing.css';

const PARTNERS = [
    'TechCorp',
    'DataCloud',
    'FinScale',
    'LogisticsB2B',
    'CyberSec',
    'CloudNet',
    'InnovateLab',
];

const POPULAR_CATEGORIES = [
    { name: 'Desarrollo', icon: '💻' },
    { name: 'Diseño', icon: '🎨' },
    { name: 'Marketing', icon: '📈' },
    { name: 'Datos', icon: '📊' },
    { name: 'Escritura', icon: '✍️' },
    { name: 'Finanzas', icon: '💰' },
    { name: 'Soporte', icon: '🤝' },
    { name: 'Video', icon: '🎬' },
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
    const [featuredJobs, setFeaturedJobs] = useState([]);
    const [companies, setCompanies] = useState([]);
    const [services, setServices] = useState([]);
    const [professionals, setProfessionals] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadLandingData = async () => {
            try {
                const [jobs, comps, proServices, profs] = await Promise.all([
                    getOfertas(),
                    perfilesService.getCompanyProfiles(),
                    catalogoService.getProfessionalServices(),
                    perfilesService.getProfessionalProfiles(),
                ]);
                setFeaturedJobs((jobs || []).slice(0, 6));
                setCompanies(comps || []);
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

    const getCompanyName = (companyId) => {
        return companies.find((company) => company.id === companyId)?.companyName || `Empresa ${companyId ?? ''}`.trim();
    };

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
        <div className="landing-container">
            {/* HERO */}
            <section className="hero-section">
                <div className="hero-content">
                    <span className="hero-badge">⚡ La plataforma B2B #1 de Talent Matching</span>
                    <h1 className="hero-title">
                        El talento que tu empresa necesita, <span className="highlight-text">al alcance de un búsqueda</span>
                    </h1>
                    <p className="hero-subtitle">
                        Contratá servicios profesionales verificados o encontrá la oferta ideal para tu carrera.
                        Dos flujos, una sola plataforma.
                    </p>

                    <form className="hero-search" onSubmit={handleSearch} role="search">
                        <span className="hero-search-icon">🔍</span>
                        <input
                            type="text"
                            placeholder="¿Qué servicio profesional estás buscando?"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            aria-label="Buscar servicios"
                        />
                        <button type="submit" className="btn-b2b-primary hero-search-btn">
                            Buscar
                        </button>
                    </form>

                    <div className="hero-chips">
                        {POPULAR_CATEGORIES.map((category) => (
                            <Link key={category.name} to="/servicios" className="hero-chip">
                                <span className="hero-chip-icon">{category.icon}</span>
                                {category.name}
                            </Link>
                        ))}
                    </div>

                    <div className="hero-cta-group">
                        <Link to="/servicios" className="btn-b2b-primary btn-large">🔍 Explorar servicios</Link>
                        <Link to="/empleos" className="btn-b2b-outline btn-large">Explorar ofertas de empleo</Link>
                    </div>

                    <div className="hero-stats">
                        <div className="stat-item">
                            <h3>+500</h3>
                            <p>Empresas registradas</p>
                        </div>
                        <div className="stat-divider" />
                        <div className="stat-item">
                            <h3>+12,000</h3>
                            <p>Profesionales activos</p>
                        </div>
                        <div className="stat-divider" />
                        <div className="stat-item">
                            <h3>94%</h3>
                            <p>Tasa de contratación</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* BARRA DE CONFIANZA */}
            <section className="partners-section">
                <p className="partners-title">Confían en nosotros las empresas líderes de la industria</p>
                <div className="marquee">
                    <div className="marquee-content">
                        {PARTNERS.concat(PARTNERS).map((partner, index) => (
                            <div key={index} className="partner-item">
                                <span className="partner-logo">{partner}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* SERVICIOS POPULARES */}
            <section className="popular-section">
                <div className="section-header">
                    <div>
                        <h2>Servicios populares</h2>
                        <p>Profesionales verificados listos para cotizarte hoy.</p>
                    </div>
                    <Link to="/servicios" className="btn-b2b-accent btn-growth">📈 Ver crecimiento</Link>
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
                    <div className="gig-track">
                        {services.map((service) => {
                            const providerName = getProviderName(service.professional_id);
                            return (
                                <article key={service.id} className="gig-card gig-card--compact">
                                    <div className="gig-cover">
                                        <span className="badge-gold">Top</span>
                                    </div>
                                    <div className="gig-body">
                                        <div className="gig-provider">
                                            <span className={`gig-avatar ${service.professional_id % 2 ? 'gig-avatar--navy' : 'gig-avatar--gold'}`}>
                                                {getInitials(providerName)}
                                            </span>
                                            <div style={{ minWidth: 0 }}>
                                                <p className="gig-provider-name">{providerName}</p>
                                                <span className="badge-verified">✓ Profesional verificado</span>
                                            </div>
                                        </div>
                                        <h3 className="gig-title">{service.title}</h3>
                                        <p className="gig-desc">{service.description || 'Sin descripción.'}</p>
                                        <div className="gig-meta">
                                            {service.rating ? (
                                                <span className="gig-rating">★ {service.rating}</span>
                                            ) : (
                                                <span className="gig-rating gig-rating--gold">★ 4.9</span>
                                            )}
                                            <span className="gig-price">
                                                <small>Desde</small>
                                                {service.price != null ? `$${service.price}` : 'A convenir'}
                                            </span>
                                        </div>
                                        <Link to="/servicios" className="btn-pill">Solicitar cotización</Link>
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                )}
            </section>

            {/* OFERTAS DESTACADAS */}
            <section className="featured-section">
                <div className="section-header">
                    <div>
                        <h2>Ofertas destacadas de la semana</h2>
                        <p>Oportunidades seleccionadas con presupuestos competitivos.</p>
                    </div>
                    <Link to="/empleos" className="btn-b2b-outline btn-more">Ver todas</Link>
                </div>

                {loading ? (
                    <div className="card-b2b">
                        <p style={{ margin: 0 }}>Cargando ofertas...</p>
                    </div>
                ) : featuredJobs.length === 0 ? (
                    <div className="card-b2b">
                        <p style={{ margin: 0, color: 'var(--text-muted)' }}>No hay ofertas destacadas disponibles.</p>
                    </div>
                ) : (
                    <div className="jobs-grid">
                        {featuredJobs.map((job) => {
                            const companyName = getCompanyName(job.companyId);
                            return (
                                <article key={job.id} className="gig-card">
                                    <div className="gig-cover">
                                        <span className="badge-gold">
                                            {job.status || 'ACTIVO'}
                                        </span>
                                    </div>
                                    <div className="gig-body">
                                        <div className="gig-provider">
                                            <span className="gig-avatar gig-avatar--navy">{getInitials(companyName)}</span>
                                            <div style={{ minWidth: 0 }}>
                                                <p className="gig-provider-name">{companyName}</p>
                                                <span className="badge-verified">✓ Empresa verificada</span>
                                            </div>
                                        </div>
                                        <h3 className="gig-title">{job.title || 'Oferta sin título'}</h3>
                                        <p className="gig-desc">{job.description || 'Sin descripción.'}</p>
                                        <div className="gig-meta">
                                            <span className="badge-neutral">📍 Categoría {job.categoryId ?? '—'}</span>
                                            <span className="gig-price">
                                                <small>Salario</small>
                                                {job.budget ? `$${job.budget}` : 'A convenir'}
                                            </span>
                                        </div>
                                        <Link to="/empleos" className="btn-pill">Aplicar ahora</Link>
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                )}
            </section>

            {/* FEATURES */}
            <section className="features-grid-section">
                <h2>Diseñado para agilizar la contratación B2B</h2>
                <div className="features-grid">
                    <div className="feature-card">
                        <div className="feature-icon">🎯</div>
                        <h3>Algoritmo de Match</h3>
                        <p>Conectamos perfiles técnicos exactamente con los requerimientos específicos de tu stack tecnológico.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">🔒</div>
                        <h3>Empresas Verificadas</h3>
                        <p>Todas las organizaciones en nuestra red pasan por un filtro de validación corporativa antes de publicar.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">⚡</div>
                        <h3>Proceso 3x más rápido</h3>
                        <p>Reduce el tiempo medio de contratación de 45 días a solo 14 días gracias a la postulación directa.</p>
                    </div>
                </div>
            </section>

            {/* CTA DE CRECIMIENTO */}
            <section className="cta-band">
                <div className="cta-band-inner">
                    <h2>¿Querés hacer crecer tu organización?</h2>
                    <p>Publicá servicios o abrí ofertas: el talento adecuado te está esperando.</p>
                    <Link to="/register" className="btn-b2b-accent btn-large">🚀 Ver crecimiento</Link>
                </div>
            </section>
        </div>
    );
};

export default Landing;