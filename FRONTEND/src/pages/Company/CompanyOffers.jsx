import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getOfertasByCompany, deleteOferta } from '../../services/ofertasService';
import perfilesService from '../../services/perfilesService';
import applicationsService from '../../services/applicationsService';
import reviewsService from '../../services/reviewsService';
import UserCard from '../../components/UserCard';
import '../../styles/catalog.css';
import '../../styles/admin.css';
import '../../styles/profile.css';

const offerStatusInfo = (status) => {
  if (status === 'ACTIVE') return { cls: 'badge-status--active' };
  if (status === 'SUSPENDED') return { cls: 'badge-status--suspended' };
  return { cls: 'badge-status--neutral' };
};

export const CompanyOffers = () => {
  const { user } = useAuth();
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedOfferId, setExpandedOfferId] = useState(null);
  const [applicantsByOffer, setApplicantsByOffer] = useState({});
  const [profilesByProf, setProfilesByProf] = useState({});
  const [reviewsByProf, setReviewsByProf] = useState({});
  const [reviewForm, setReviewForm] = useState({});
  const [loadingApplicants, setLoadingApplicants] = useState(false);
  const [sendingReviewId, setSendingReviewId] = useState(null);

  useEffect(() => {
    const load = async () => {
      if (!user) return;
      try {
        const profile = await perfilesService.getCompanyProfileByUser(user.id);
        if (!profile?.id) {
          setError('Debes completar tu perfil de empresa para ver tus ofertas.');
          return;
        }
        const data = await getOfertasByCompany(profile.id);
        setOffers(data || []);
      } catch (err) {
        console.error('Error cargando ofertas', err);
        setError('No se pudieron cargar las ofertas de la empresa.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [user]);

  if (loading) {
    return <p style={{ padding: '20px' }}>Cargando tus ofertas...</p>;
  }

  if (error) {
    return <p style={{ padding: '20px', color: 'red' }}>{error}</p>;
  }

  const handleDelete = async (offerId) => {
    const confirmed = window.confirm('¿Eliminar esta oferta? Esta acción no se puede deshacer.');
    if (!confirmed) return;

    try {
      await deleteOferta(offerId);
      setOffers((current) => current.filter((offer) => offer.id !== offerId));
    } catch (err) {
      console.error('Error eliminando oferta', err);
      setError('No se pudo eliminar la oferta. Intenta de nuevo.');
    }
  };

  const handleToggleApplicants = async (offerId) => {
    if (expandedOfferId === offerId) {
      setExpandedOfferId(null);
      return;
    }

    setExpandedOfferId(offerId);
    setLoadingApplicants(true);
    try {
      const list = await applicationsService.getApplicationsByJobOfferId(offerId);
      setApplicantsByOffer((prev) => ({ ...prev, [offerId]: list || [] }));

      for (const app of list || []) {
        if (!app.professionalId) continue;
        if (!profilesByProf[app.professionalId]) {
          try {
            const p = await perfilesService.getProfessionalProfileById(app.professionalId);
            if (p) setProfilesByProf((prev) => ({ ...prev, [app.professionalId]: p }));
          } catch (err) {
            console.error('Error cargando perfil profesional', err);
          }
        }
        if (reviewsByProf[app.professionalId] === undefined) {
          try {
            const r = await reviewsService.getReviewsByProfessional(app.professionalId);
            setReviewsByProf((prev) => ({ ...prev, [app.professionalId]: r || [] }));
          } catch (err) {
            console.error('Error cargando reseñas', err);
            setReviewsByProf((prev) => ({ ...prev, [app.professionalId]: [] }));
          }
        }
      }
    } catch (err) {
      console.error('Error cargando postulantes', err);
      setError('No se pudieron cargar los postulantes.');
    } finally {
      setLoadingApplicants(false);
    }
  };

  const handleReviewChange = (professionalId, field, value) => {
    setReviewForm((prev) => ({ ...prev, [professionalId]: { ...prev[professionalId], [field]: value } }));
  };

  const handleReviewSubmit = async (professionalId) => {
    if (!user?.id) return;
    const form = reviewForm[professionalId] || {};
    const rating = Number(form.rating);

    if (!rating || rating < 1 || rating > 5) {
      alert('Selecciona una calificación de 1 a 5 estrellas.');
      return;
    }

    setSendingReviewId(professionalId);
    try {
      await reviewsService.createReview({
        customerId: user.id,
        professionalId,
        rating,
        comment: form.comment?.trim() || null,
      });
      const r = await reviewsService.getReviewsByProfessional(professionalId);
      setReviewsByProf((prev) => ({ ...prev, [professionalId]: r || [] }));
      setReviewForm((prev) => ({ ...prev, [professionalId]: { rating: '5', comment: '' } }));
      alert('Reseña publicada correctamente.');
    } catch (err) {
      console.error('Error publicando reseña', err);
      alert('No se pudo publicar la reseña. Intenta de nuevo.');
    } finally {
      setSendingReviewId(null);
    }
  };

  const getApplicantName = (professionalId) => {
    const p = profilesByProf[professionalId];
    if (p?.firstName || p?.lastName) return `${p.firstName || ''} ${p.lastName || ''}`.trim();
    return `Profesional #${professionalId}`;
  };

  return (
    <div className="list-page">
      <div className="page-title-row">
        <header className="page-title">
          <span className="badge-gold">Reclutamiento</span>
          <h1>Mis Ofertas Creadas</h1>
          <p>Gestiona y publica convocatorias laborales</p>
        </header>
        <Link to="/empresa/crear-oferta" className="btn-b2b-primary" style={{ textDecoration: 'none', whiteSpace: 'nowrap' }}>
          + Crear Nueva Oferta
        </Link>
      </div>

      {error && <div className="alert-banner alert-banner--error">{error}</div>}

      {offers.length === 0 ? (
        <div className="empty-state">
          <strong>Sin ofertas creadas</strong>
          <span>Usa el botón "Crear Nueva Oferta" para publicar tu primera vacante.</span>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {offers.map((offer) => {
            const status = offerStatusInfo(offer.status || 'ACTIVE');
            const hasApplicants = (applicantsByOffer[offer.id] || []).length > 0;
            return (
              <div key={offer.id} className="offer-card">
                <div className="offer-card-head">
                  <div style={{ minWidth: 0 }}>
                    <div className="offer-card-title">
                      <h3>{offer.title}</h3>
                      <span className={`badge-status ${status.cls}`}>{offer.status || 'ACTIVE'}</span>
                    </div>
                    <p className="offer-card-meta">
                      Publicado el {offer.createdAt ? new Date(offer.createdAt).toLocaleDateString() : 'sin fecha'} · Presupuesto:{' '}
                      <strong>${offer.budget ?? 0}</strong>
                    </p>
                  </div>

                  <div className="offer-card-actions">
                    <Link to={`/empresa/editar-oferta/${offer.id}`} className="btn-b2b-outline btn-xs" style={{ textDecoration: 'none' }}>
                      Editar
                    </Link>
                    <button onClick={() => handleToggleApplicants(offer.id)} className="btn-b2b-outline btn-xs">
                      {expandedOfferId === offer.id ? 'Ocultar Postulantes' : hasApplicants ? `Postulantes (${hasApplicants})` : 'Ver Postulantes'}
                    </button>
                    <button onClick={() => handleDelete(offer.id)} className="btn-b2b-outline btn-xs btn-danger">
                      Eliminar
                    </button>
                  </div>
                </div>

                {expandedOfferId === offer.id && (
                  <div className="offer-card-body">
                    {loadingApplicants ? (
                      <p style={{ margin: 0, color: 'var(--text-muted)' }}>Cargando postulantes...</p>
                    ) : (applicantsByOffer[offer.id] || []).length === 0 ? (
                      <p style={{ margin: 0, color: 'var(--text-muted)' }}>Aún no hay postulaciones para esta oferta.</p>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                        {(applicantsByOffer[offer.id] || []).map((app) => {
                          const professionalId = app.professionalId;
                          const existingReviews = reviewsByProf[professionalId] || [];
                          const avg =
                            existingReviews.length > 0
                              ? (existingReviews.reduce((acc, r) => acc + (r.rating || 0), 0) / existingReviews.length).toFixed(1)
                              : null;
                          const form = reviewForm[professionalId] || { rating: '5', comment: '' };

                          return (
                            <div key={app.id} className="gig-row">
                              <UserCard
                                name={getApplicantName(professionalId)}
                                role={app.proposal || 'Sin propuesta adjunta'}
                                skills={`Precio esperado: $${app.expectedPrice ?? 0} · ${existingReviews.length} reseña${existingReviews.length === 1 ? '' : 's'}${avg ? ` · Promedio: ${avg}★` : ''}`}
                              />

                              {existingReviews.length > 0 && (
                                <div style={{ marginTop: '16px' }}>
                                  <p style={{ margin: '0 0 10px', fontWeight: '600', fontSize: '0.9rem' }}>Reseñas existentes:</p>
                                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                    {existingReviews.map((review) => (
                                      <div key={review.id} className="review-item">
                                        <div className="review-item-header">
                                          <span className="review-stars">
                                            {'★'.repeat(review.rating || 0)}{'☆'.repeat(Math.max(0, 5 - (review.rating || 0)))}
                                          </span>
                                          <small>{review.createdAt ? new Date(review.createdAt).toLocaleDateString() : ''}</small>
                                        </div>
                                        {review.comment && <p>{review.comment}</p>}
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              )}

                              <div style={{ marginTop: '16px', borderTop: '1px solid var(--border-color)', paddingTop: '16px' }}>
                                <p style={{ margin: '0 0 12px', fontWeight: '600', fontSize: '0.9rem' }}>Dejar una reseña para este profesional</p>
                                <div className="form-row" style={{ alignItems: 'end' }}>
                                  <div className="form-group" style={{ maxWidth: '140px' }}>
                                    <label className="form-label">Calificación</label>
                                    <select
                                      className="form-select"
                                      value={form.rating}
                                      onChange={(e) => handleReviewChange(professionalId, 'rating', e.target.value)}
                                    >
                                      {[5, 4, 3, 2, 1].map((n) => (
                                        <option key={n} value={n}>
                                          {n} ★
                                        </option>
                                      ))}
                                    </select>
                                  </div>
                                  <div className="form-group" style={{ flex: 1, minWidth: '180px' }}>
                                    <label className="form-label">Comentario (opcional)</label>
                                    <input
                                      type="text"
                                      className="form-input"
                                      placeholder="Escribí tu experiencia..."
                                      value={form.comment || ''}
                                      onChange={(e) => handleReviewChange(professionalId, 'comment', e.target.value)}
                                    />
                                  </div>
                                  <button
                                    className="btn-b2b-primary btn-xs"
                                    onClick={() => handleReviewSubmit(professionalId)}
                                    disabled={sendingReviewId === professionalId}
                                  >
                                    {sendingReviewId === professionalId ? 'Publicando...' : 'Publicar Reseña'}
                                  </button>
                                </div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default CompanyOffers;