import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import perfilesService from '../../services/perfilesService';
import reviewsService from '../../services/reviewsService';
import catalogoService from '../../services/catalogoService';
import quotationsService from '../../services/quotationsService';

const inputStyle = {
  width: '100%',
  padding: '10px 14px',
  borderRadius: '8px',
  border: '1px solid var(--border-color)',
  background: 'var(--bg-input)',
  color: 'var(--text-main)',
  outline: 'none',
};

const labelStyle = {
  display: 'block',
  marginBottom: '6px',
  color: 'var(--text-muted)',
  fontSize: '0.9rem',
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

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Mi Perfil</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Administra tu información personal y profesional</p>

      {error && (
        <div style={{ marginBottom: '20px', padding: '16px', background: '#fdecea', color: '#b91c1c', borderRadius: '12px' }}>
          {error}
        </div>
      )}

      <div className="card-b2b" style={{ padding: '32px' }}>
        <form onSubmit={(e) => e.preventDefault()} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Nombre</label>
              <input type="text" name="firstName" value={profile.firstName} onChange={handleChange} style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>Apellido</label>
              <input type="text" name="lastName" value={profile.lastName} onChange={handleChange} style={inputStyle} />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Teléfono</label>
              <input type="text" name="phone" value={profile.phone} onChange={handleChange} style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>Años de Experiencia</label>
              <input type="number" name="experienceYears" value={profile.experienceYears} onChange={handleChange} style={inputStyle} />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Tarifa por Hora (USD)</label>
              <input type="number" step="0.01" name="hourlyRate" value={profile.hourlyRate} onChange={handleChange} style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>Ciudad</label>
              <input type="text" name="city" value={profile.city} onChange={handleChange} style={inputStyle} />
            </div>
          </div>

          <div>
            <label style={labelStyle}>País</label>
            <input type="text" name="country" value={profile.country} onChange={handleChange} style={inputStyle} />
          </div>

          <div>
            <label style={labelStyle}>Biografía / Sobre mí</label>
            <textarea
              name="biography"
              rows="4"
              value={profile.biography}
              onChange={handleChange}
              style={{ ...inputStyle, resize: 'vertical' }}
            />
          </div>

          <div>
            <label style={labelStyle}>URL de Portafolio</label>
            <input type="url" name="portfolioUrl" value={profile.portfolioUrl} onChange={handleChange} style={inputStyle} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>LinkedIn</label>
              <input type="url" name="linkedinUrl" value={profile.linkedinUrl} onChange={handleChange} style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>GitHub</label>
              <input type="url" name="githubUrl" value={profile.githubUrl} onChange={handleChange} style={inputStyle} />
            </div>
          </div>

          <button type="button" className="btn-b2b-primary" style={{ marginTop: '10px', alignSelf: 'flex-start' }} onClick={handleSave} disabled={saving}>
            {saving ? 'Guardando...' : 'Guardar Cambios'}
          </button>
        </form>
      </div>

      <div className="card-b2b" style={{ padding: '32px', marginTop: '24px' }}>
        <h2 style={{ fontSize: '1.4rem', marginBottom: '6px' }}>Reseñas de clientes</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '20px', fontSize: '0.9rem' }}>
          Valoraciones que las empresas dejan sobre tu trabajo
        </p>
        {reviews.length === 0 ? (
          <p style={{ margin: 0, color: 'var(--text-muted)' }}>
            Todavía no tienes reseñas. Cuando una empresa evalúe tu trabajo aparecerán aquí.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {reviews.map((review) => (
              <div key={review.id} style={{ border: '1px solid var(--border-color)', borderRadius: '12px', padding: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px', marginBottom: '6px' }}>
                  <span style={{ color: '#F59E0B', fontSize: '1.05rem', letterSpacing: '2px' }}>
                    {'★'.repeat(review.rating || 0)}{'☆'.repeat(Math.max(0, 5 - (review.rating || 0)))}
                  </span>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                    {review.createdAt ? new Date(review.createdAt).toLocaleDateString() : 'sin fecha'}
                  </span>
                </div>
                {review.comment && <p style={{ margin: 0, color: 'var(--text-main)', fontSize: '0.95rem' }}>{review.comment}</p>}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="card-b2b" style={{ padding: '32px', marginTop: '24px' }}>
        <h2 style={{ fontSize: '1.4rem', marginBottom: '6px' }}>Mis Servicios</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '20px', fontSize: '0.9rem' }}>
          Publica servicios que ofreces; los clientes podrán solicitarte cotizaciones
        </p>

        {serviceError && <p style={{ color: 'red', marginBottom: '12px' }}>{serviceError}</p>}

        <div className="card-b2b" style={{ padding: '20px', marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
            <div>
              <label style={labelStyle}>Título del servicio</label>
              <input type="text" name="title" value={serviceForm.title} onChange={handleServiceChange} placeholder="Ej: Desarrollo de sitios web" style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>Categoría</label>
              <select name="category_id" value={serviceForm.category_id} onChange={handleServiceChange} style={inputStyle}>
                <option value="">Seleccionar...</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label style={labelStyle}>Precio (USD)</label>
              <input type="number" name="price" value={serviceForm.price} onChange={handleServiceChange} placeholder="Ej: 1500" style={inputStyle} />
            </div>
          </div>
          <div>
            <label style={labelStyle}>Descripción</label>
            <textarea name="description" rows="2" value={serviceForm.description} onChange={handleServiceChange} placeholder="Detalla qué incluye el servicio..." style={{ ...inputStyle, resize: 'vertical' }} />
          </div>
          <button type="button" className="btn-b2b-primary" style={{ alignSelf: 'flex-start' }} onClick={handleCreateService} disabled={savingService}>
            {savingService ? 'Publicando...' : 'Publicar Servicio'}
          </button>
        </div>

        {myServices.length === 0 ? (
          <p style={{ margin: 0, color: 'var(--text-muted)' }}>
            Todavía no publicaste servicios. Usa el formulario de arriba para crear tu primero.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {myServices.map((service) => {
              const quotes = quotationsByService[service.id] || [];
              return (
                <div key={service.id} className="card-b2b" style={{ padding: '20px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px', marginBottom: '6px' }}>
                    <h3 style={{ fontSize: '1.1rem', margin: 0 }}>{service.title}</h3>
                    <span className="badge-tag">{categories.find((c) => c.id === service.category_id)?.name || 'General'}</span>
                  </div>
                  <p style={{ margin: '4px 0', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                    {service.description || 'Sin descripción.'}
                  </p>
                  <p style={{ margin: '0 0 10px', fontWeight: '700' }}>
                    {service.price != null ? `$${service.price}` : 'Precio a convenir'}
                  </p>

                  <p style={{ margin: '0 0 8px', fontWeight: '600', fontSize: '0.9rem' }}>Cotizaciones recibidas ({quotes.length})</p>
                  {quotes.length === 0 ? (
                    <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.88rem' }}>
                      Aún no hay solicitudes de cotización para este servicio.
                    </p>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                      {quotes.map((quote) => (
                        <div key={quote.id} style={{ border: '1px solid var(--border-color)', borderRadius: '10px', padding: '12px' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px' }}>
                            <strong style={{ fontSize: '0.9rem' }}>Solicitante #{quote.customerId}</strong>
                            <span className="badge-tag">{quote.status || 'PENDING'}</span>
                          </div>
                          <p style={{ margin: '6px 0 0', color: 'var(--text-muted)', fontSize: '0.88rem' }}>{quote.message}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default UserProfile;
