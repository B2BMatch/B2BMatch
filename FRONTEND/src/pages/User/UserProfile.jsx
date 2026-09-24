import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import perfilesService from '../../services/perfilesService';
import reviewsService from '../../services/reviewsService';
import catalogoService from '../../services/catalogoService';
import quotationsService from '../../services/quotationsService';
import '../../styles/catalog.css';
import '../../styles/profile.css';

const getInitials = (firstName = '', lastName = '') => {
    const first = firstName.trim()[0] || '';
    const last = lastName.trim()[0] || '';
    return (first + last).toUpperCase() || 'P';
};

export const UserProfile = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    biography: '',
    experienceYears: '',
    hourlyRate: '',
    portfolioUrl: '',
    linkedinUrl: '',
    githubUrl: '',
    city: '',
    country: '',
  });
  const [profileId, setProfileId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reviews, setReviews] = useState([]);
  const [categories, setCategories] = useState([]);
  const [myServices, setMyServices] = useState([]);
  const [quotationsByService, setQuotationsByService] = useState({});
  const [serviceForm, setServiceForm] = useState({ title: '', description: '', price: '', category_id: '' });
  const [savingService, setSavingService] = useState(false);
  const [serviceError, setServiceError] = useState('');

  useEffect(() => {
    const load = async () => {
      if (!user) return;
      try {
        const data = await perfilesService.getProfessionalProfileByUser(user.id);
        if (data) {
          setProfile({
            firstName: data.firstName || '',
            lastName: data.lastName || '',
            phone: data.phone || '',
            biography: data.biography || '',
            experienceYears: data.experienceYears ?? '',
            hourlyRate: data.hourlyRate ?? '',
            portfolioUrl: data.portfolioUrl || '',
            linkedinUrl: data.linkedinUrl || '',
            githubUrl: data.githubUrl || '',
            city: data.city || '',
            country: data.country || '',
          });
          setProfileId(data.id || null);

          try {
            const reviewList = await reviewsService.getReviewsByProfessional(data.id);
            setReviews(Array.isArray(reviewList) ? reviewList : []);
          } catch (err) {
            console.error('Error cargando reseñas', err);
            setReviews([]);
          }
        }
      } catch (err) {
        console.error('Error cargando perfil', err);
        setError('No se pudo cargar tu perfil.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [user]);

  useEffect(() => {
    const loadServices = async () => {
      if (!profileId) return;
      try {
        const [cats, servicesList] = await Promise.all([
          catalogoService.getCategories(),
          catalogoService.getProfessionalServices(),
        ]);
        setCategories(Array.isArray(cats) ? cats : []);
        const own = (Array.isArray(servicesList) ? servicesList : []).filter(
          (s) => String(s.professional_id) === String(profileId)
        );
        setMyServices(own);

        for (const service of own) {
          try {
            const quotes = await quotationsService.getQuotationsByService(service.id);
            setQuotationsByService((prev) => ({ ...prev, [service.id]: Array.isArray(quotes) ? quotes : [] }));
          } catch (err) {
            console.error('Error cargando cotizaciones del servicio', err);
            setQuotationsByService((prev) => ({ ...prev, [service.id]: [] }));
          }
        }
      } catch (err) {
        console.error('Error cargando servicios', err);
        setServiceError('No se pudieron cargar tus servicios.');
      }
    };
    loadServices();
  }, [profileId]);

  const handleServiceChange = (e) => {
    setServiceForm({ ...serviceForm, [e.target.name]: e.target.value });
  };

  const handleCreateService = async () => {
    if (!serviceForm.title.trim() || !serviceForm.category_id) {
      alert('Completa el título y selecciona una categoría.');
      return;
    }
    setSavingService(true);
    setServiceError('');
    try {
      await catalogoService.createProfessionalService({
        professional_id: profileId,
        category_id: Number(serviceForm.category_id),
        title: serviceForm.title.trim(),
        description: serviceForm.description?.trim() || null,
        price: serviceForm.price ? Number(serviceForm.price) : null,
      });
      setServiceForm({ title: '', description: '', price: '', category_id: '' });
      const servicesList = await catalogoService.getProfessionalServices();
      const own = (Array.isArray(servicesList) ? servicesList : []).filter(
        (s) => String(s.professional_id) === String(profileId)
      );
      setMyServices(own);
      alert('Servicio publicado.');
    } catch (err) {
      console.error('Error creando servicio', err);
      setServiceError('No se pudo publicar el servicio.');
    } finally {
      setSavingService(false);
    }
  };

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    try {
      const payload = {
        userId: user.id,
        firstName: profile.firstName,
        lastName: profile.lastName,
        phone: profile.phone || null,
        biography: profile.biography || null,
        experienceYears: profile.experienceYears ? Number(profile.experienceYears) : null,
        hourlyRate: profile.hourlyRate ? Number(profile.hourlyRate) : null,
        portfolioUrl: profile.portfolioUrl || null,
        linkedinUrl: profile.linkedinUrl || null,
        githubUrl: profile.githubUrl || null,
        city: profile.city || null,
        country: profile.country || null,
      };
      if (profileId) {
        await perfilesService.updateProfessionalProfile(profileId, payload);
      } else {
        await perfilesService.createProfessionalProfile(payload);
      }
      alert('Perfil guardado');
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Error al guardar perfil');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <p style={{ padding: '20px' }}>Cargando perfil...</p>;
  }

  const displayName = `${profile.firstName} ${profile.lastName}`.trim() || user?.name || user?.email || 'Profesional';
  const avgRating =
    reviews.length > 0
      ? (reviews.reduce((acc, r) => acc + (r.rating || 0), 0) / reviews.length).toFixed(1)
      : null;

  return (
    <div className="profile-page">
      <header className="page-head" style={{ padding: '0 0 24px' }}>
        <span className="badge-gold">Perfil profesional</span>
        <h1>Mi Perfil</h1>
        <p>Administrá tu información personal, tus reseñas y los servicios que publicás.</p>
      </header>

      {error && (
        <div style={{ marginBottom: '20px', padding: '16px', background: '#fdecea', color: '#b91c1c', borderRadius: '12px' }}>
          {error}
        </div>
      )}

      <section className="profile-hero">
        <div className={`profile-avatar-lg ${avgRating ? 'profile-avatar-lg--gold' : 'profile-avatar-lg--coral'}`}>
          {getInitials(profile.firstName, profile.lastName) || 'P'}
        </div>
        <div className="profile-hero-info">
          <div className="profile-hero-name">
            {displayName}
            <span className="badge-verified">✓ Profesional verificado</span>
          </div>
          <p className="profile-hero-sub">
            {profile.biography ? profile.biography.slice(0, 140) : 'Tu bio aparecerá aquí — completá tu perfil abajo.'}
          </p>
          <div className="profile-hero-chips">
            {profile.experienceYears != null && profile.experienceYears !== '' && (
              <span className="profile-hero-chip">🎓 {profile.experienceYears} años de experiencia</span>
            )}
            {profile.hourlyRate != null && profile.hourlyRate !== '' && (
              <span className="profile-hero-chip">💰 ${profile.hourlyRate}/h</span>
            )}
            {profile.city && (
              <span className="profile-hero-chip">📍 {profile.city}{profile.country ? `, ${profile.country}` : ''}</span>
            )}
            <span className="profile-hero-chip">★ {avgRating || '—'} ({reviews.length} reseñas)</span>
          </div>
        </div>
      </section>

      <section className="section-card">
        <h2>Información personal</h2>
        <p className="section-sub">Estos datos se muestran en tu perfil público</p>
        <form onSubmit={(e) => e.preventDefault()} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Nombre</label>
              <input type="text" className="form-input" name="firstName" value={profile.firstName} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Apellido</label>
              <input type="text" className="form-input" name="lastName" value={profile.lastName} onChange={handleChange} />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Teléfono</label>
              <input type="text" className="form-input" name="phone" value={profile.phone} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Años de Experiencia</label>
              <input type="number" className="form-input" name="experienceYears" value={profile.experienceYears} onChange={handleChange} />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Tarifa por Hora (USD)</label>
              <input type="number" step="0.01" className="form-input" name="hourlyRate" value={profile.hourlyRate} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Ciudad</label>
              <input type="text" className="form-input" name="city" value={profile.city} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">País</label>
            <input type="text" className="form-input" name="country" value={profile.country} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label className="form-label">Biografía / Sobre mí</label>
            <textarea className="form-textarea" name="biography" rows="4" value={profile.biography} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label className="form-label">URL de Portafolio</label>
            <input type="url" className="form-input" name="portfolioUrl" value={profile.portfolioUrl} onChange={handleChange} />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">LinkedIn</label>
              <input type="url" className="form-input" name="linkedinUrl" value={profile.linkedinUrl} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">GitHub</label>
              <input type="url" className="form-input" name="githubUrl" value={profile.githubUrl} onChange={handleChange} />
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn-b2b-primary" onClick={handleSave} disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar Cambios'}
            </button>
          </div>
        </form>
      </section>

      <section className="section-card">
        <h2>Reseñas de clientes</h2>
        <p className="section-sub">Valoraciones que las empresas dejan sobre tu trabajo</p>
        {reviews.length === 0 ? (
          <p style={{ margin: 0, color: 'var(--text-muted)' }}>
            Todavía no tenés reseñas. Cuando una empresa evalúe tu trabajo aparecerán aquí.
          </p>
        ) : (
          <div className="item-list">
            {reviews.map((review) => (
              <div key={review.id} className="review-item">
                <div className="review-item-header">
                  <span className="review-stars">
                    {'★'.repeat(review.rating || 0)}{'☆'.repeat(Math.max(0, 5 - (review.rating || 0)))}
                  </span>
                  <small>{review.createdAt ? new Date(review.createdAt).toLocaleDateString() : 'sin fecha'}</small>
                </div>
                {review.comment && <p>{review.comment}</p>}
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="section-card">
        <h2>Mis Servicios</h2>
        <p className="section-sub">Publicá servicios que ofrecés; los clientes podrán solicitarte cotizaciones</p>

        {serviceError && <p style={{ color: 'red', marginBottom: '12px' }}>{serviceError}</p>}

        <div className="gig-row" style={{ marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Título del servicio</label>
              <input type="text" className="form-input" name="title" value={serviceForm.title} onChange={handleServiceChange} placeholder="Ej: Desarrollo de sitios web" />
            </div>
            <div className="form-group">
              <label className="form-label">Categoría</label>
              <select className="form-select" name="category_id" value={serviceForm.category_id} onChange={handleServiceChange}>
                <option value="">Seleccionar...</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Precio (USD)</label>
              <input type="number" className="form-input" name="price" value={serviceForm.price} onChange={handleServiceChange} placeholder="Ej: 1500" />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Descripción</label>
            <textarea className="form-textarea" name="description" rows="2" value={serviceForm.description} onChange={handleServiceChange} placeholder="Detallá qué incluye el servicio..." />
          </div>
          <div className="form-actions">
            <button type="button" className="btn-b2b-primary" onClick={handleCreateService} disabled={savingService}>
              {savingService ? 'Publicando...' : 'Publicar Servicio'}
            </button>
          </div>
        </div>

        {myServices.length === 0 ? (
          <p style={{ margin: 0, color: 'var(--text-muted)' }}>
            Todavía no publicaste servicios. Usá el formulario de arriba para crear el primero.
          </p>
        ) : (
          <div className="item-list">
            {myServices.map((service) => {
              const quotes = quotationsByService[service.id] || [];
              return (
                <div key={service.id} className="gig-row">
                  <div className="gig-row-header">
                    <h3>{service.title}</h3>
                    <span className="badge-gold">{categories.find((c) => c.id === service.category_id)?.name || 'General'}</span>
                  </div>
                  <p style={{ margin: '4px 0', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                    {service.description || 'Sin descripción.'}
                  </p>
                  <p style={{ margin: '0 0 12px', fontWeight: '700' }}>
                    {service.price != null ? `$${service.price}` : 'Precio a convenir'}
                  </p>

                  <p style={{ margin: '0 0 8px', fontWeight: '600', fontSize: '0.9rem' }}>Cotizaciones recibidas ({quotes.length})</p>
                  {quotes.length === 0 ? (
                    <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.88rem' }}>
                      Aún no hay solicitudes de cotización para este servicio.
                    </p>
                  ) : (
                    <div className="item-list">
                      {quotes.map((quote) => (
                        <div key={quote.id} className="item-row">
                          <div className="item-row-header">
                            <span>Solicitante #{quote.userId}</span>
                            <span className="badge-gold">{quote.status || 'PENDING'}</span>
                          </div>
                          <p>{quote.message}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
};

export default UserProfile;