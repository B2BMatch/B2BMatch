import React, { useEffect, useState } from 'react';
import { getOfertas, updateOfertaStatus } from '../../services/ofertasService';

export const Offers = () => {
  const [offersList, setOffersList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const data = await getOfertas();
      setOffersList(data || []);
      setError('');
    } catch (err) {
      console.error('Error cargando ofertas admin', err);
      setError('No se pudieron cargar las ofertas.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleToggleStatus = async (offer, nextStatus) => {
    const action = nextStatus === 'ACTIVE' ? 'Activar' : 'Bajar';
    const confirmed = window.confirm(`¿${action} la oferta "${offer.title}"?`);
    if (!confirmed) return;
    try {
      await updateOfertaStatus(offer.id, nextStatus);
      alert(`Oferta ${action.toLowerCase()} correctamente.`);
      load();
    } catch (err) {
      console.error(err);
      alert('No se pudo actualizar la oferta.');
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando ofertas...</p>;

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Administrar Ofertas</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Supervisión general de publicaciones en la plataforma</p>

      {error && <p style={{ color: 'red', marginBottom: '16px' }}>{error}</p>}

      <div className="card-b2b" style={{ padding: '0', overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
              <th style={{ padding: '16px 20px' }}>Título de la Oferta</th>
              <th style={{ padding: '16px 20px' }}>Empresa</th>
              <th style={{ padding: '16px 20px' }}>Categoría</th>
              <th style={{ padding: '16px 20px' }}>Presupuesto</th>
              <th style={{ padding: '16px 20px' }}>Estado</th>
              <th style={{ padding: '16px 20px', textAlign: 'right' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {offersList.map((offer) => {
              const isActive = offer.status === 'ACTIVE';
              return (
                <tr key={offer.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '16px 20px', fontWeight: '600' }}>{offer.title || offer.titulo}</td>
                  <td style={{ padding: '16px 20px', color: 'var(--text-muted)' }}>{offer.companyId}</td>
                  <td style={{ padding: '16px 20px' }}>{offer.categoryId}</td>
                  <td style={{ padding: '16px 20px' }}>{offer.budget ? `$${offer.budget}` : '—'}</td>
                  <td style={{ padding: '16px 20px' }}>
                    <span className="badge-tag">{offer.status || 'ACTIVE'}</span>
                  </td>
                  <td style={{ padding: '16px 20px', textAlign: 'right' }}>
                    {isActive ? (
                      <button className="btn-b2b-outline" style={{ padding: '4px 12px', fontSize: '0.8rem', color: '#EF4444', borderColor: '#EF4444' }} onClick={() => handleToggleStatus(offer, 'SUSPENDED')}>
                        Bajar Oferta
                      </button>
                    ) : (
                      <button className="btn-b2b-outline" style={{ padding: '4px 12px', fontSize: '0.8rem' }} onClick={() => handleToggleStatus(offer, 'ACTIVE')}>
                        Activar
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Offers;
