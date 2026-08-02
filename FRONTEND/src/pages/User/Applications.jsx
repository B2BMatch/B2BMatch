import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import applicationsService from '../../services/applicationsService';
import perfilesService from '../../services/perfilesService';
import { getOfertas } from '../../services/ofertasService';

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
          data = await applicationsService.getApplicationsByProfessionalId(profile.id);
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
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Mis Postulaciones</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Estado de tus postulaciones actuales</p>

      {applications.length === 0 ? (
        <div className="card-b2b" style={{ padding: '24px' }}>
          <p style={{ margin: 0, color: 'var(--text-muted)' }}>
            No hay postulaciones registradas todavía.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {applications.map((app) => (
            <div key={app.id} className="card-b2b" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
              <div>
                <h3 style={{ fontSize: '1.1rem', marginBottom: '4px' }}>{getOfferTitle(app.jobOfferId || app.job_offer_id)}</h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                  {app.proposal || 'Sin propuesta adjunta.'}
                </p>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  Postulado el {app.createdAt ? new Date(app.createdAt).toLocaleDateString() : app.created_at ? new Date(app.created_at).toLocaleDateString() : 'sin fecha'}
                </p>
              </div>
              <div>
                <span style={{
                  padding: '6px 14px',
                  borderRadius: '20px',
                  fontSize: '0.85rem',
                  fontWeight: '600',
                  background: 'rgba(59, 130, 246, 0.15)',
                  color: '#3B82F6',
                  border: '1px solid #3B82F6'
                }}>
                  {app.status || app.state || 'Pendiente'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Applications;
