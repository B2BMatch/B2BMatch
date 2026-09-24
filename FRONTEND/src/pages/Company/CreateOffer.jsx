import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import catalogoService from '../../services/catalogoService';
import perfilesService from '../../services/perfilesService';
import { createOferta } from '../../services/ofertasService';
import '../../styles/catalog.css';

export const CreateOffer = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    budget: '',
    deadline: '',
    description: '',
  });

  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState(null);
  const [companyProfileId, setCompanyProfileId] = useState(null);
  const [profileError, setProfileError] = useState('');
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const { user } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!companyProfileId) {
      alert('Necesitas completar tu perfil de empresa antes de crear una oferta.');
      return;
    }
    if (!formData.deadline) {
      alert('Debes indicar una fecha límite para la vacante.');
      return;
    }

    const payload = {
      categoryId: categoryId,
      title: formData.title,
      description: formData.description,
      budget: formData.budget ? parseFloat(formData.budget) : 0,
      deadline: formData.deadline,
    };

    setSubmitting(true);
    createOferta(payload)
      .then(() => navigate('/empresa/ofertas'))
      .catch((err) => {
        console.error('Error creando oferta', err);
        alert('Error al crear la oferta');
      })
      .finally(() => setSubmitting(false));
  };

  useEffect(() => {
    const load = async () => {
      try {
        const cats = await catalogoService.getCategories();
        setCategories(cats);
        if (cats.length) setCategoryId(cats[0].id);
      } catch (err) {
        console.error(err);
      }
    };
    load();
  }, []);

  useEffect(() => {
    const loadCompanyProfile = async () => {
      if (!user) return;
      try {
        const profile = await perfilesService.getCompanyProfileByUser(user.id);
        if (profile?.id) {
          setCompanyProfileId(profile.id);
        } else {
          setProfileError('Debes completar primero tu perfil de empresa para poder publicar ofertas.');
        }
      } catch (err) {
        console.error('Error cargando perfil de empresa', err);
        setProfileError('No se pudo cargar tu perfil de empresa.');
      } finally {
        setLoadingProfile(false);
      }
    };
    loadCompanyProfile();
  }, [user]);

  return (
    <div className="list-page" style={{ maxWidth: '820px' }}>
      <header className="page-title">
        <span className="badge-gold">Publicación</span>
        <h1>Crear Nueva Oferta</h1>
        <p>Completa la información del puesto de trabajo</p>
      </header>

      <div className="section-card">
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {loadingProfile ? (
            <p style={{ color: 'var(--text-muted)' }}>Cargando perfil de empresa...</p>
          ) : profileError ? (
            <div className="alert-banner alert-banner--error">{profileError}</div>
          ) : null}

          <div className="form-group">
            <label className="form-label">Título de la Vacante</label>
            <input
              type="text"
              name="title"
              className="form-input"
              placeholder="Ej: Senior React Developer"
              value={formData.title}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Presupuesto (USD)</label>
              <input
                type="number"
                step="0.01"
                name="budget"
                className="form-input"
                placeholder="Ej: 3800"
                value={formData.budget}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Fecha Límite</label>
              <input
                type="date"
                name="deadline"
                className="form-input"
                value={formData.deadline}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Categoría</label>
            <select className="form-select" value={categoryId || ''} onChange={(e) => setCategoryId(Number(e.target.value))}>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Descripción del Puesto</label>
            <textarea
              name="description"
              className="form-textarea"
              rows="4"
              placeholder="Detalla las responsabilidades clave..."
              value={formData.description}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-b2b-primary" disabled={!companyProfileId || loadingProfile || submitting}>
              {submitting ? 'Publicando...' : 'Publicar Vacante'}
            </button>
            <button type="button" className="btn-b2b-outline" onClick={() => navigate('/empresa/ofertas')}>
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateOffer;