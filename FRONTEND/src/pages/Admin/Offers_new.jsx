import { useEffect, useState } from 'react';
import { getOfertas, updateOfertaStatus } from '../../services/ofertasService';
import '../../styles/admin.css';

const statusBadgeClass = (status) => {
  if (status === 'ACTIVE') return 'badge-status--active';
  if (status === 'SUSPENDED') return 'badge-status--suspended';
  return 'badge-status--neutral';
};

export const Offers = () => {
  const [offersList, setOffersList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
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
    load();
  }, [reloadKey]);

  const handleToggleStatus = async (offer, nextStatus) => {
    const action = nextStatus === 'ACTIVE' ? 'Activar' : 'Bajar';
    const confirmed = window.confirm(`¿${action} la oferta "${offer.title}"?`);
    if (!confirmed) return;
    try {
      await updateOfertaStatus(offer.id, nextStatus);
      alert(`Oferta ${action.toLowerCase()} correctamente.`);
      setReloadKey((k) => k + 1);
    } catch (err) {
      console.error(err);
      alert('No se pudo actualizar la oferta.');
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando ofertas...</p>;

  return (
    <div className="admin-page">
      <header className="admin-head">
        <span className="badge-gold">Admin · Ofertas</span>
        <h1>Administrar Ofertas</h1>
        <p>Supervisión general de publicaciones en la plataforma</p>
      </header>

      {error && <p style={{ color: 'red', marginBottom: '16px' }}>{error}</p>}

      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Título de la Oferta</th>
              <th>Empresa</th>
              <th>Categoría</th>
              <th>Presupuesto</th>
              <th>Estado</th>
              <th className="cell-actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {offersList.map((offer) => {
              const isActive = offer.status === 'ACTIVE';
              return (
                <tr key={offer.id}>
                  <td className="cell-strong">{offer.title || offer.titulo}</td>
                  <td className="cell-muted">{offer.userId}</td>
                  <td className="cell-muted">{offer.categoryId}</td>
                  <td className="cell-strong" style={{ color: 'var(--gold)' }}>{offer.budget ? `$${offer.budget}` : '—'}</td>
                  <td>
                    <span className={`badge-status ${statusBadgeClass(offer.status)}`}>{offer.status || 'ACTIVE'}</span>
                  </td>
                  <td className="cell-actions">
                    {isActive ? (
                      <button className="btn-b2b-outline btn-danger" onClick={() => handleToggleStatus(offer, 'SUSPENDED')}>
                        Bajar Oferta
                      </button>
                    ) : (
                      <button className="btn-b2b-outline" onClick={() => handleToggleStatus(offer, 'ACTIVE')}>
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