import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getOfertasByCompany, deleteOferta } from '../../services/ofertasService';
import perfilesService from '../../services/perfilesService';
import applicationsService from '../../services/applicationsService';
import reviewsService from '../../services/reviewsService';
import UserCard from '../../components/UserCard';

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
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '32px 20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Mis Ofertas Creadas</h1>
          <p style={{ color: 'var(--text-muted)' }}>Gestiona y publica convocatorias laborales</p>
        </div>
        <Link to="/empresa/crear-oferta" className="btn-b2b-primary" style={{ textDecoration: 'none' }}>
          + Crear Nueva Oferta
        </Link>
      </div>

      {offers.length === 0 ? (
        <div className="card-b2b" style={{ padding: '24px' }}>
          <p style={{ margin: 0, color: 'var(--text-muted)' }}>
            No tienes ofertas creadas aún. Usa el botón "Crear Nueva Oferta" para publicar tu primera vacante.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {offers.map((offer) => (
            <div key={offer.id} className="card-b2b" style={{ padding: '0', overflow: 'hidden' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', padding: '24px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '6px' }}>
                    <h3 style={{ fontSize: '1.1rem' }}>{offer.title}</h3>
                    <span className="badge-tag">{offer.status || 'ACTIVE'}</span>
                  </div>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem' }}>
                    Publicado el {offer.createdAt ? new Date(offer.createdAt).toLocaleDateString() : 'sin fecha'} • Presupuesto: <strong style={{ color: 'var(--text-main)' }}>${offer.budget ?? 0}</strong>
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                  <Link to={`/empresa/editar-oferta/${offer.id}`} className="btn-b2b-outline" style={{ textDecoration: 'none', padding: '6px 16px', fontSize: '0.85rem' }}>
                    Editar
                  </Link>
                  <button onClick={() => handleToggleApplicants(offer.id)} className="btn-b2b-outline" style={{ padding: '6px 16px', fontSize: '0.85rem' }}>
                    {expandedOfferId === offer.id ? 'Ocultar Postulantes' : 'Ver Postulantes'}
                  </button>
                  <button onClick={() => handleDelete(offer.id)} className="btn-b2b-outline" style={{ padding: '6px 16px', fontSize: '0.85rem', color: '#EF4444', borderColor: '#EF4444' }}>
                    Eliminar
                  </button>
                </div>
              </div>

              {expandedOfferId === offer.id && (
                <div style={{ borderTop: '1px solid var(--border-color)', padding: '24px' }}>
                  {loadingApplicants ? (
                    <p style={{ margin: 0, color: 'var(--text-muted)' }}>Cargando postulantes...</p>
                  ) : (applicantsByOffer[offer.id] || []).length === 0 ? (
                    <p style={{ margin: 0, color: 'var(--text-muted)' }}>
                      Aún no hay postulaciones para esta oferta.
                    </p>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                      {(applicantsByOffer[offer.id] || []).map((app) => {
                        const professionalId = app.professionalId;
                        const existingReviews = reviewsByProf[professionalId] || [];
                        const avg =
                          existingReviews.length > 0
                            ? (existingReviews.reduce((acc, r) => acc + (r.rating || 0), 0) / existingReviews.length).toFixed(1)
                            : null;
                        const form = reviewForm[professionalId] || { rating: '5', comment: '' };

                        return (
                          <div key={app.id} className="card-b2b" style={{ padding: '20px' }}>
                            <UserCard
                              name={getApplicantName(professionalId)}
                              role={app.proposal || 'Sin propuesta adjunta'}
                              skills={`Precio esperado: $${app.expectedPrice ?? 0} · ${existingReviews.length} reseña${existingReviews.length === 1 ? '' : 's'}${avg ? ` · Promedio: ${avg}★` : ''}`}
                            />

                            {existingReviews.length > 0 && (
                              <div style={{ marginTop: '14px' }}>
                                <p style={{ margin: '0 0 8px', fontWeight: '600', fontSize: '0.9rem' }}>Reseñas existentes:</p>
                                {existingReviews.map((review) => (
                                  <div key={review.id} style={{ border: '1px solid var(--border-color)', borderRadius: '10px', padding: '12px', marginBottom: '8px' }}>
                                    <div style={{ color: '#F59E0B', fontSize: '0.95rem', letterSpacing: '2px' }}>
                                      {'★'.repeat(review.rating || 0)}{'☆'.repeat(Math.max(0, 5 - (review.rating || 0)))}
                                    </div>
                                    {review.comment && <p style={{ margin: '4px 0 0', color: 'var(--text-muted)', fontSize: '0.88rem' }}>{review.comment}</p>}
                                  </div>
                                ))}
                              </div>
                            )}

                            <div style={{ marginTop: '14px', borderTop: '1px solid var(--border-color)', paddingTop: '14px' }}>
                              <p style={{ margin: '0 0 10px', fontWeight: '600', fontSize: '0.9rem' }}>Dejar una reseña para este profesional</p>
                              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'center' }}>
                                <select
                                  value={form.rating}
                                  onChange={(e) => handleReviewChange(professionalId, 'rating', e.target.value)}
                                  style={{ padding: '8px 12px', borderRadius: '8px', background: 'var(--bg-input)', color: 'var(--text-main)', border: '1px solid var(--border-color)' }}
                                >
                                  {[5, 4, 3, 2, 1].map((n) => (
                                    <option key={n} value={n}>
                                      {n} ★
                                    </option>
                                  ))}
                                </select>
                                <input
                                  type="text"
                                  placeholder="Comentario (opcional)"
                                  value={form.comment || ''}
                                  onChange={(e) => handleReviewChange(professionalId, 'comment', e.target.value)}
                                  style={{ flex: 1, minWidth: '200px', padding: '8px 12px', borderRadius: '8px', background: 'var(--bg-input)', color: 'var(--text-main)', border: '1px solid var(--border-color)', outline: 'none' }}
                                />
                                <button
                                  className="btn-b2b-primary"
                                  onClick={() => handleReviewSubmit(professionalId)}
                                  disabled={sendingReviewId === professionalId}
                                  style={{ padding: '8px 18px', fontSize: '0.85rem' }}
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
          ))}
        </div>
      )}
    </div>
  );
};

export default CompanyOffers;
