import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import applicationsService from '../../services/applicationsService';
import perfilesService from '../../services/perfilesService';
import { getOfertas } from '../../services/ofertasService';
import '../../styles/catalog.css';
import '../../styles/admin.css';

const statusInfo = (status) => {
  const raw = status || 'Pendiente';
  const s = String(status || '').toUpperCase();
  if (s.includes('REJECT') || s.includes('CANCEL') || s.includes('SUSPEND') || s.includes('BLOCK')) {
    return { text: raw, cls: 'badge-status--suspended' };
  }
  if (s.includes('ACCEPT') || s.includes('HIRE') || s.includes('APPROV') || s.includes('ACTIVE')) {
    return { text: raw, cls: 'badge-status--active' };
  }
  return { text: raw, cls: 'badge-status--neutral' };
};

export const Applications = () => {
  const { user } = useAuth();
  const [applications, setApplications] = useState([]);
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadApplications = async () => {
      if (!user) return;

      try {
        let data = [];

        if (user.role === 'candidate') {
          const profile = await perfilesService.getProfessionalProfileByUser(user.id);
          if (!profile?.id) {
            setError('Completa tu perfil profesional para ver tus postulaciones.');
            return;
          }
          data = await applicationsService.getApplicationsByUserId(user.id);
        } else {
          data = await applicationsService.getApplications();
        }

        setApplications(data || []);

        const jobs = await getOfertas();
        setOffers(jobs || []);
      } catch (err) {
        console.error('Error cargando postulaciones', err);
        setError('No se pudieron cargar las postulaciones desde el servidor.');
      } finally {
        setLoading(false);
      }
    };

    loadApplications();
  }, [user]);

  if (loading) {
    return <p style={{ padding: '20px' }}>Cargando postulaciones...</p>;
  }

  if (error) {
    return <p style={{ padding: '20px', color: 'red' }}>{error}</p>;
  }

  const getOfferTitle = (jobOfferId) => {
    const offer = offers.find((o) => o.id === jobOfferId);
    return offer?.title || `Oferta #${jobOfferId}`;
  };

  return (
    <div className="list-page">
      <header className="page-title">
        <span className="badge-gold">Historial laboral</span>
        <h1>Mis Postulaciones</h1>
        <p>Estado de tus postulaciones actuales</p>
      </header>

      {applications.length === 0 ? (
        <div className="empty-state">
          <strong>Sin postulaciones registradas</strong>
          <span>Explorá el listado de ofertas y postulate a la vacante que encaje con vos.</span>
          <a href="/empleos" style={{ color: 'var(--text-accent)', fontWeight: '600' }}>Ver ofertas disponibles →</a>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {applications.map((app) => {
            const status = statusInfo(app.status || app.state);
            return (
              <div key={app.id} className="app-card">
                <div style={{ minWidth: 0, flex: 1 }}>
                  <h3>{getOfferTitle(app.jobOfferId || app.job_offer_id)}</h3>
                  {app.proposal && <p className="app-meta">{app.proposal}</p>}
                  <p className="app-extra">
                    {app.expectedPrice != null && <span><strong style={{ color: 'var(--gold)' }}>${app.expectedPrice}</strong> · </span>}
                    Postulado el{' '}
                    {app.createdAt
                      ? new Date(app.createdAt).toLocaleDateString()
                      : app.created_at
                        ? new Date(app.created_at).toLocaleDateString()
                        : 'sin fecha'}
                  </p>
                </div>
                <span className={`badge-status ${status.cls}`}>{status.text}</span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Applications;