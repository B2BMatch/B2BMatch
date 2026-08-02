import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import catalogoService from '../../services/catalogoService';
import { getOfertaById, updateOferta } from '../../services/ofertasService';

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

export const EditOffer = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [formData, setFormData] = useState({
    companyId: null,
    categoryId: null,
    title: '',
    budget: '',
    deadline: '',
    description: '',
  });
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [cats, offer] = await Promise.all([
          catalogoService.getCategories(),
          getOfertaById(id),
        ]);

        setCategories(cats || []);
        setFormData({
          companyId: offer.companyId || null,
          categoryId: offer.categoryId || (cats.length > 0 ? cats[0].id : null),
          title: offer.title || '',
          budget: offer.budget ?? '',
          deadline: offer.deadline ? String(offer.deadline).slice(0, 10) : '',
          description: offer.description || '',
        });
      } catch (err) {
        console.error('Error cargando oferta', err);
        setError('No se pudo cargar la oferta para editarla.');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      loadData();
    } else {
      setError('ID de oferta inválido.');
      setLoading(false);
    }
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.categoryId) {
      setError('Selecciona una categoría.');
      return;
    }
    if (!formData.deadline) {
      setError('Debes indicar una fecha límite para la vacante.');
      return;
    }

    setSubmitting(true);
    try {
      await updateOferta(id, {
        companyId: formData.companyId,
        categoryId: Number(formData.categoryId),
        title: formData.title,
        description: formData.description,
        budget: Number(formData.budget),
        deadline: formData.deadline,
      });
      navigate('/empresa/ofertas');
    } catch (err) {
      console.error('Error actualizando oferta', err);
      setError(err?.response?.data?.message || 'No se pudo guardar los cambios de la oferta.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p style={{ padding: '20px' }}>Cargando oferta...</p>;
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Editar Oferta</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Modifica la información de la vacante</p>

      {error && (
        <div style={{ marginBottom: '20px', padding: '16px', background: '#fdecea', color: '#b91c1c', borderRadius: '12px' }}>
          {error}
        </div>
      )}

      <div className="card-b2b" style={{ padding: '32px' }}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label style={labelStyle}>Título de la Vacante</label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              required
              style={inputStyle}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Categoría</label>
              <select
                name="categoryId"
                value={formData.categoryId || ''}
                onChange={handleChange}
                required
                style={inputStyle}
              >
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name || category.title || 'Categoría'}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label style={labelStyle}>Presupuesto (USD)</label>
              <input
                type="number"
                step="0.01"
                name="budget"
                value={formData.budget}
                onChange={handleChange}
                required
                style={inputStyle}
              />
            </div>
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

          <div>
            <label style={labelStyle}>Descripción del Puesto</label>
            <textarea
              name="description"
              rows="4"
              value={formData.description}
              onChange={handleChange}
              required
              style={{ ...inputStyle, resize: 'vertical' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
            <button type="submit" className="btn-b2b-primary" disabled={submitting}>
              {submitting ? 'Guardando...' : 'Actualizar Oferta'}
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

export default EditOffer;
