import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import catalogoService from '../../services/catalogoService';
import { getOfertaById, updateOferta } from '../../services/ofertasService';
import '../../styles/catalog.css';

export const EditOffer = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [formData, setFormData] = useState({
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

    if (!id) return;
    loadData();
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

  if (!id) {
    return <p style={{ padding: '20px' }}>ID de oferta inválido.</p>;
  }

  if (loading) {
    return <p style={{ padding: '20px' }}>Cargando oferta...</p>;
  }

  return (
    <div className="list-page" style={{ maxWidth: '820px' }}>
      <header className="page-title">
        <span className="badge-gold">Edición</span>
        <h1>Editar Oferta</h1>
        <p>Modifica la información de la vacante</p>
      </header>

      {error && <div className="alert-banner alert-banner--error">{error}</div>}

      <div className="section-card">
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="form-group">
            <label className="form-label">Título de la Vacante</label>
            <input
              type="text"
              name="title"
              className="form-input"
              value={formData.title}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Categoría</label>
              <select
                name="categoryId"
                className="form-select"
                value={formData.categoryId || ''}
                onChange={handleChange}
                required
              >
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name || category.title || 'Categoría'}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Presupuesto (USD)</label>
              <input
                type="number"
                step="0.01"
                name="budget"
                className="form-input"
                value={formData.budget}
                onChange={handleChange}
                required
              />
            </div>
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

          <div className="form-group">
            <label className="form-label">Descripción del Puesto</label>
            <textarea
              name="description"
              className="form-textarea"
              rows="4"
              value={formData.description}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-actions">
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