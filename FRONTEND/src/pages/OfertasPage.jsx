import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getOfertas } from '../services/ofertasService';
import applicationsService from '../services/applicationsService';
import catalogoService from '../services/catalogoService';
import perfilesService from '../services/perfilesService';
import '../styles/catalog.css';

const getInitials = (name = '') => {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    const first = parts[0]?.[0] || '';
    const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
    return (first + last).toUpperCase() || 'B2B';
};

const OfertasPage = () => {
  const { currentUser } = useAuth();
  const [ofertas, setOfertas] = useState([]);
  const [categories, setCategories] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [applyingOfferId, setApplyingOfferId] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const [jobs, cats, comps] = await Promise.all([
          getOfertas(),
          catalogoService.getCategories(),
          perfilesService.getCompanyProfiles(),
        ]);
        setOfertas(jobs);
        setCategories(cats);
        setCompanies(comps || []);
      } catch (err) {
        console.error('Error al cargar ofertas', err);
        setError('No se pudieron cargar las ofertas.');
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const getCategoryName = (categoryId) => {
    return categories.find((category) => category.id === categoryId)?.name || 'General';
  };

  const getCompanyName = (userId) => {
    return companies.find((company) => company.userId === userId)?.companyName || `Empresa ${userId ?? ''}`.trim();
  };

  const handleApply = async (offer) => {
    if (!currentUser) {
      alert('Debes iniciar sesión como candidato para postular.');
      return;
    }

    if (currentUser.role !== 'candidate') {
      alert('Solo los candidatos pueden postular a las ofertas.');
      return;
    }

    try {
      setApplyingOfferId(offer.id);
      const profile = await perfilesService.getProfessionalProfileByUser(currentUser.id);
      if (!profile?.id) {
        alert('Completa tu perfil profesional antes de postular.');
        return;
      }

      await applicationsService.createApplication({
        jobOfferId: offer.id,
        proposal: `Estoy interesado en la vacante ${offer.title}.`,
        expectedPrice: offer.budget || 0
      });
      alert('Postulación enviada con éxito.');
    } catch (err) {
      console.error('Error al postular a la oferta', err);
      alert('Error al enviar la postulación. Intenta nuevamente.');
    } finally {
      setApplyingOfferId(null);
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando ofertas...</p>;
  if (error) return <p style={{ padding: '20px', color: 'red' }}>{error}</p>;

  return (
    <div>
      <header className="page-head">
        <span className="badge-gold">Mercado laboral</span>
        <h1>Ofertas de Empleo</h1>
        <p>Encontrá oportunidades de empresas verificadas y postulate de forma directa.</p>
      </header>

      <div className="list-page">
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px' }}>
          {ofertas.length} oferta{ofertas.length === 1 ? '' : 's'} disponible{ofertas.length === 1 ? '' : 's'}
        </p>

        {ofertas.length === 0 ? (
          <div className="empty-state">
            <strong>Sin ofertas publicadas</strong>
            <span>Volvé más tarde: las empresas están cargando nuevas vacantes.</span>
          </div>
        ) : (
          <div className="result-grid">
            {ofertas.map((oferta) => {
              const companyName = getCompanyName(oferta.userId);
              return (
                <article key={oferta.id} className="gig-card">
                  <div className="gig-cover">
                    <span className="badge-gold">{oferta.status || 'ACTIVO'}</span>
                  </div>
                  <div className="gig-body">
                    <div className="gig-provider">
                      <span className="gig-avatar gig-avatar--sm gig-avatar--navy">{getInitials(companyName)}</span>
                      <div style={{ minWidth: 0 }}>
                        <p className="gig-provider-name">{companyName}</p>
                        <span className="badge-verified">✓ Empresa verificada</span>
                      </div>
                    </div>
                    <h3 className="gig-title">{oferta.title || 'Oferta sin título'}</h3>
                    <p className="gig-desc">{oferta.description}</p>
                    <div className="gig-meta">
                      <span className="badge-neutral">{getCategoryName(oferta.categoryId)}</span>
                      <span className="gig-price">
                        <small>Presupuesto</small>
                        {oferta.budget ? `$${oferta.budget}` : 'A convenir'}
                      </span>
                    </div>
                    <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                      {oferta.deadline ? `Cierra el ${new Date(oferta.deadline).toLocaleDateString()}` : 'Oferta abierta'}
                    </p>
                    {currentUser ? (
                      <button
                        className="btn-pill"
                        onClick={() => handleApply(oferta)}
                        disabled={applyingOfferId === oferta.id}
                        style={{ alignSelf: 'flex-start', marginTop: '4px' }}
                      >
                        {applyingOfferId === oferta.id ? 'Postulando...' : 'Aplicar'}
                      </button>
                    ) : (
                      <Link to="/login" className="btn-pill btn-pill--outline" style={{ alignSelf: 'flex-start', marginTop: '4px' }}>
                        Iniciar sesión para postular
                      </Link>
                    )}
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default OfertasPage;