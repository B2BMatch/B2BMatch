import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import catalogoService from '../../services/catalogoService';
import perfilesService from '../../services/perfilesService';
import { createOferta } from '../../services/ofertasService';

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
      companyId: companyProfileId,
      categoryId: categoryId,
      title: formData.title,
      description: formData.description,
      budget: formData.budget ? parseFloat(formData.budget) : 0,
      deadline: formData.deadline,
      status: 'ACTIVE',
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
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Crear Nueva Oferta</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Completa la información del puesto de trabajo</p>

      <div className="card-b2b" style={{ padding: '32px' }}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {loadingProfile ? (
            <p style={{ color: 'var(--text-muted)' }}>Cargando perfil de empresa...</p>
          ) : profileError ? (
            <div style={{ padding: '16px', background: '#f9ecec', color: '#B91C1C', borderRadius: '12px' }}>
              {profileError}
            </div>
          ) : null}

          <div>
            <label style={labelStyle}>Título de la Vacante</label>
            <input
              type="text"
              name="title"
              placeholder="Ej: Senior React Developer"
              value={formData.title}
              onChange={handleChange}
              required
              style={inputStyle}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Presupuesto (USD)</label>
              <input
                type="number"
                step="0.01"
                name="budget"
                placeholder="Ej: 3800"
                value={formData.budget}
                onChange={handleChange}
                required
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>Fecha Límite</label>
              <input
                type="date"
                name="deadline"
                value={formData.deadline}
                onChange={handleChange}
                required
                style={inputStyle}
              />
            </div>
          </div>

          <div>
            <label style={labelStyle}>Categoría</label>
            <select value={categoryId || ''} onChange={(e) => setCategoryId(Number(e.target.value))} style={{ ...inputStyle, color: 'var(--text-main)' }}>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label style={labelStyle}>Descripción del Puesto</label>
            <textarea
              name="description"
              rows="4"
              placeholder="Detalla las responsabilidades clave..."
              value={formData.description}
              onChange={handleChange}
              required
              style={{ ...inputStyle, resize: 'vertical' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
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
