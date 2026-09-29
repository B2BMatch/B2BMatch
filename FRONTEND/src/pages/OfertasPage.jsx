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
  const [companiesByUser, setCompaniesByUser] = useState({});
  const [loading, setLoading] = useState(true);
  const [applyingOfferId, setApplyingOfferId] = useState(null);
  const [appliedOfferIds, setAppliedOfferIds] = useState([]);
  const [applicationForms, setApplicationForms] = useState({});
  const [feedback, setFeedback] = useState({});
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const [jobs, cats] = await Promise.all([
          getOfertas(),
          catalogoService.getCategories(),
        ]);
        setOfertas(Array.isArray(jobs) ? jobs : []);
        setCategories(Array.isArray(cats) ? cats : []);
      } catch (err) {
        console.error('Error al cargar ofertas', err);
        setError('No se pudieron cargar las ofertas.');
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  // El listado público devuelve userId enmascarado (null), así que el nombre de la
  // empresa se resuelve consulta por userId, deduplicado y cacheado.
  useEffect(() => {
    if (ofertas.length === 0) return;
    const resolveCompanies = async () => {
      const userIds = [...new Set(ofertas.map((o) => o.userId).filter((id) => id != null))];
      const entries = await Promise.all(
        userIds.map(async (userId) => {
          try {
            const profile = await perfilesService.getCompanyProfileByUser(userId);
            return [userId, profile?.companyName || null];
          } catch (err) {
            console.error('Error cargando perfil de empresa', err);
            return [userId, null];
          }
        })
      );
      setCompaniesByUser((prev) => ({ ...prev, ...Object.fromEntries(entries) }));
    };
    resolveCompanies();
  }, [ofertas]);

  // Carga las postulaciones del candidato para no ofrecer "Aplicar" donde ya se postuló.
  useEffect(() => {
    if (!currentUser?.id) {
      setAppliedOfferIds([]);
      return;
    }
    const loadApplications = async () => {
      try {
        const list = await applicationsService.getApplicationsByUserId(currentUser.id);
        setAppliedOfferIds((Array.isArray(list) ? list : []).map((a) => a.jobOfferId));
      } catch (err) {
        console.error('Error cargando mis postulaciones', err);
      }
    };
    loadApplications();
  }, [currentUser?.id]);

  const getCategoryName = (categoryId) => {
    return categories.find((category) => category.id === categoryId)?.name || 'General';
  };

  const getCompanyName = (userId) => {
    if (userId == null) return 'Empresa';
    return companiesByUser[userId] || `Empresa #${userId}`;
  };

  const handleFormChange = (offerId, field, value) => {
    setApplicationForms((prev) => ({ ...prev, [offerId]: { ...prev[offerId], [field]: value } }));
  };

  const handleApply = async (offer) => {
    if (!currentUser) {
      setFeedback((prev) => ({ ...prev, [offer.id]: { type: 'error', text: 'Debes iniciar sesión como candidato para postular.' } }));
      return;
    }

    if (currentUser.role !== 'candidate') {
      setFeedback((prev) => ({ ...prev, [offer.id]: { type: 'error', text: 'Solo los candidatos pueden postular a las ofertas.' } }));
      return;
    }

    const form = applicationForms[offer.id] || {};
    const proposal = (form.proposal || '').trim();
    if (!proposal) {
      setFeedback((prev) => ({ ...prev, [offer.id]: { type: 'error', text: 'Escribí una carta de presentación para postular.' } }));
      return;
    }
    const expectedPrice = form.expectedPrice !== undefined && form.expectedPrice !== '' ? Number(form.expectedPrice) : null;
    if (expectedPrice !== null && (Number.isNaN(expectedPrice) || expectedPrice < 0)) {
      setFeedback((prev) => ({ ...prev, [offer.id]: { type: 'error', text: 'El precio esperado debe ser un número válido mayor o igual a 0.' } }));
      return;
    }

    try {
      setApplyingOfferId(offer.id);
      const profile = await perfilesService.getProfessionalProfileByUser(currentUser.id);
      if (!profile?.id) {
        setFeedback((prev) => ({ ...prev, [offer.id]: { type: 'error', text: 'Completa tu perfil profesional antes de postular.' } }));
        return;
      }

      await applicationsService.createApplication({
        jobOfferId: offer.id,
        proposal,
        expectedPrice,
      });
      setAppliedOfferIds((prev) => [...new Set([...prev, offer.id])]);
      setApplicationForms((prev) => ({ ...prev, [offer.id]: { proposal: '', expectedPrice: '' } }));
      setFeedback((prev) => ({ ...prev, [offer.id]: { type: 'success', text: 'Postulación enviada con éxito.' } }));
    } catch (err) {
      console.error('Error al postular a la oferta', err);
      const conflict = err?.response?.status === 409;
      if (conflict) setAppliedOfferIds((prev) => [...new Set([...prev, offer.id])]);
      setFeedback((prev) => ({
        ...prev,
        [offer.id]: {
          type: 'error',
          text: conflict
            ? 'Ya te postulaste a esta oferta.'
            : err?.response?.data?.message || 'No se pudo enviar la postulación. Intenta nuevamente.',
        },
      }));
    } finally {
      setApplyingOfferId(null);
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando ofertas...</p>;
  if (error) return <p style={{ padding: '20px', color: 'red' }}>{error}</p>;

  // El backend solo excluye DELETED/CLOSED/EXPIRED: las ofertas SUSPENDED o INACTIVE
  // igual llegan al listado público, pero no admiten postulaciones.
  const openOffers = ofertas.filter((oferta) => (oferta.status || 'ACTIVE') === 'ACTIVE');

  return (
    <div>
      <header className="page-head">
        <span className="badge-gold">Mercado laboral</span>
        <h1>Ofertas de Empleo</h1>
        <p>Encontrá oportunidades de empresas verificadas y postulate de forma directa.</p>
      </header>

      <div className="list-page">
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px' }}>
          {openOffers.length} oferta{openOffers.length === 1 ? '' : 's'} disponible{openOffers.length === 1 ? '' : 's'}
        </p>

        {openOffers.length === 0 ? (
          <div className="empty-state">
            <strong>Sin ofertas publicadas</strong>
            <span>Volvé más tarde: las empresas están cargando nuevas vacantes.</span>
          </div>
        ) : (
          <div className="result-grid">
            {openOffers.map((oferta) => {
              const companyName = getCompanyName(oferta.userId);
              const alreadyApplied = appliedOfferIds.includes(oferta.id);
              const isApplying = applyingOfferId === oferta.id;
              const form = applicationForms[oferta.id] || {};
              const offerFeedback = feedback[oferta.id];
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

                    {!currentUser ? (
                      <Link to="/login" className="btn-pill btn-pill--outline" style={{ alignSelf: 'flex-start', marginTop: '4px' }}>
                        Iniciar sesión para postular
                      </Link>
                    ) : alreadyApplied ? (
                      <button className="btn-pill" disabled style={{ alignSelf: 'flex-start', marginTop: '4px' }}>
                        Ya te postulaste ✓
                      </button>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '4px' }}>
                        <textarea
                          className="form-textarea"
                          rows="3"
                          placeholder="Contale a la empresa por qué sos el perfil indicado..."
                          value={form.proposal || ''}
                          onChange={(e) => handleFormChange(oferta.id, 'proposal', e.target.value)}
                        />
                        <input
                          type="number"
                          min="0"
                          className="form-input"
                          placeholder="Tu precio esperado (opcional)"
                          value={form.expectedPrice ?? ''}
                          onChange={(e) => handleFormChange(oferta.id, 'expectedPrice', e.target.value)}
                        />
                        <button
                          className="btn-pill"
                          onClick={() => handleApply(oferta)}
                          disabled={isApplying}
                          style={{ alignSelf: 'flex-start' }}
                        >
                          {isApplying ? 'Postulando...' : 'Aplicar'}
                        </button>
                      </div>
                    )}

                    {offerFeedback && (
                      <div
                        className={`alert-banner alert-banner--${offerFeedback.type}`}
                        style={{ marginTop: '10px', padding: '8px 12px', fontSize: '0.85rem' }}
                      >
                        {offerFeedback.text}
                      </div>
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