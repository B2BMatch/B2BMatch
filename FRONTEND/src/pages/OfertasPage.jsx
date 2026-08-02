import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getOfertas } from '../services/ofertasService';
import applicationsService from '../services/applicationsService';
import catalogoService from '../services/catalogoService';
import perfilesService from '../services/perfilesService';

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

  const getCompanyName = (companyId) => {
    return companies.find((company) => company.id === companyId)?.companyName || `Empresa ${companyId ?? ''}`.trim();
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
        professionalId: profile.id,
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
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '24px' }}>Ofertas de Empleo</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
        {ofertas.map((oferta) => (
          <div key={oferta.id} className="card-b2b" style={{ padding: '24px' }}>
            <h3>{oferta.title || 'Oferta sin título'}</h3>
            <p style={{ color: 'var(--text-muted)' }}>{getCompanyName(oferta.companyId)}</p>
            <p style={{ marginTop: '10px', color: 'var(--text-muted)' }}>{oferta.description}</p>
            <p style={{ marginTop: '12px', fontWeight: '600' }}>{oferta.budget ? `$${oferta.budget}` : 'Precio no definido'}</p>
            <span className="badge-tag" style={{ display: 'inline-block', marginBottom: '12px' }}>{getCategoryName(oferta.categoryId)}</span>
            <div>
              <button
                className="btn-b2b-primary"
                onClick={() => handleApply(oferta)}
                disabled={applyingOfferId === oferta.id}
                style={{ opacity: applyingOfferId === oferta.id ? 0.6 : 1 }}
              >
                {applyingOfferId === oferta.id ? 'Postulando...' : 'Postular'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OfertasPage;
