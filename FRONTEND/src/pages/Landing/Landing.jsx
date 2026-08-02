import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getOfertas } from '../../services/ofertasService';
import perfilesService from '../../services/perfilesService';
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

export const Landing = () => {
    const [currentSlide, setCurrentSlide] = useState(0);
    const [featuredJobs, setFeaturedJobs] = useState([]);
    const [companies, setCompanies] = useState([]);
    const [loadingJobs, setLoadingJobs] = useState(true);

    useEffect(() => {
        const timer = setInterval(() => {
            setCurrentSlide((prev) => (prev + 1) % Math.max(featuredJobs.length, 1));
        }, 4000);
        return () => clearInterval(timer);
    }, [featuredJobs.length]);

    useEffect(() => {
        const loadFeaturedJobs = async () => {
            try {
                const [jobs, comps] = await Promise.all([
                    getOfertas(),
                    perfilesService.getCompanyProfiles(),
                ]);
                setFeaturedJobs(jobs.slice(0, 4));
                setCompanies(comps || []);
            } catch (err) {
                console.error('Error cargando ofertas destacadas:', err);
            } finally {
                setLoadingJobs(false);
            }
        };

        loadFeaturedJobs();
    }, []);

    const getCompanyName = (companyId) => {
        return companies.find((company) => company.id === companyId)?.companyName || `Empresa ${companyId ?? ''}`.trim();
    };

    const nextSlide = () => {
        setCurrentSlide((prev) => (prev + 1) % Math.max(featuredJobs.length, 1));
    };

    const prevSlide = () => {
        setCurrentSlide((prev) => (prev === 0 ? Math.max(featuredJobs.length, 1) - 1 : prev - 1));
    };

    return (
        <div className="landing-container">
            <section className="hero-section">
                <div className="hero-content">
                    <span className="hero-badge">⚡ La plataforma B2B #1 de Talent Matching</span>
                    <h1 className="hero-title">
                        Conectamos el <span className="highlight-text">Talento Tech</span> con las mejores Empresas B2B
                    </h1>
                    <p className="hero-subtitle">
                        Encuentra vacantes de alto impacto o contrata talento verificado en tiempo récord. Sin intermediarios innecesarios.
                    </p>
                    <div className="hero-cta-group">
                        <Link to="/empleos" className="btn-b2b-primary btn-large">🔍 Explorar Ofertas</Link>
                        <Link to="/register" className="btn-b2b-outline btn-large">🏢 Publicar Empleo</Link>
                    </div>

                    <div className="hero-stats">
                        <div className="stat-item">
                            <h3>+500</h3>
                            <p>Empresas registradas</p>
                        </div>
                        <div className="stat-divider" />
                        <div className="stat-item">
                            <h3>+12,000</h3>
                            <p>Candidatos activos</p>
                        </div>
                        <div className="stat-divider" />
                        <div className="stat-item">
                            <h3>94%</h3>
                            <p>Tasa de contratación</p>
                        </div>
                    </div>
                </div>
            </section>

            <section className="partners-section">
                <p className="partners-title">Confían en nosotros las empresas líderes de la industria</p>
                <div className="marquee">
                    <div className="marquee-content">
                        {PARTNERS.concat(PARTNERS).map((partner, index) => (
                            <div key={index} className="partner-item">
                                <span className="partner-logo">⚡ {partner}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            <section className="featured-section">
                <div className="section-header">
                    <div>
                        <h2>Ofertas Destacadas de la Semana</h2>
                        <p>Oportunidades seleccionadas con salarios competitivos y flexibilidad laboral.</p>
                    </div>
                    <div className="carousel-controls">
                        <button onClick={prevSlide} className="carousel-btn" aria-label="Anterior">←</button>
                        <button onClick={nextSlide} className="carousel-btn" aria-label="Siguiente">→</button>
                    </div>
                </div>

                <div className="carousel-wrapper">
                    <div className="carousel-track" style={{ transform: `translateX(-${currentSlide * 100}%)` }}>
                        {loadingJobs ? (
                            <div className="carousel-slide">
                                <div className="job-card-featured">
                                    <h3>Cargando ofertas...</h3>
                                </div>
                            </div>
                        ) : featuredJobs.length > 0 ? (
                            featuredJobs.map((job) => (
                                <div key={job.id} className="carousel-slide">
                                    <div className="job-card-featured">
                                        <div className="job-card-header">
                                            <span className="job-badge">{job.status || 'ACTIVO'}</span>
                                            <span className="job-type">{job.deadline ? 'Hasta ' + new Date(job.deadline).toLocaleDateString() : 'Oferta abierta'}</span>
                                        </div>
                                        <h3>{job.title}</h3>
                                        <p className="company-name">🏢 {getCompanyName(job.companyId)}</p>
                                        <div className="job-details">
                                            <span>📍 {job.categoryId ? `Categoría ${job.categoryId}` : 'Sin categoría'}</span>
                                            <span className="salary-text">💰 {job.budget ? `$${job.budget}` : 'Sin presupuesto'}</span>
                                        </div>
                                        <Link to="/empleos" className="btn-b2b-outline btn-full">Ver Detalles</Link>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="carousel-slide">
                                <div className="job-card-featured">
                                    <h3>No hay ofertas destacadas disponibles.</h3>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                <div className="carousel-dots">
                    {featuredJobs.length > 0 ? featuredJobs.map((_, index) => (
                        <button
                            key={index}
                            className={`dot ${currentSlide === index ? 'active' : ''}`}
                            onClick={() => setCurrentSlide(index)}
                        />
                    )) : null}
                </div>
            </section>

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
        </div>
    );
};

export default Landing;
