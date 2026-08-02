import React, { useEffect, useState } from 'react';
import perfilesService from '../../services/perfilesService';
import usersService from '../../services/usersService';

export const Companies = () => {
  const [companiesList, setCompaniesList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const data = await perfilesService.getCompanyProfiles();
      setCompaniesList(data || []);
      setError('');
    } catch (err) {
      console.error('Error cargando empresas', err);
      setError('No se pudieron cargar las empresas.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleStatusChange = async (profile, status, actionLabel) => {
    const confirmed = window.confirm(`¿${actionLabel} la cuenta de "${profile.companyName}"?`);
    if (!confirmed) return;
    try {
      await usersService.updateUserStatus(profile.userId, status);
      alert(`Cuenta ${status === 'ACTIVE' ? 'aprobada' : 'bloqueada'}.`);
    } catch (err) {
      console.error(err);
      alert('No se pudo actualizar el estado de la cuenta.');
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando empresas...</p>;

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Administrar Empresas</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Listado de empresas registradas y su estado de verificación</p>

      {error && <p style={{ color: 'red', marginBottom: '16px' }}>{error}</p>}

      <div className="card-b2b" style={{ padding: '0', overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
              <th style={{ padding: '16px 20px' }}>Empresa</th>
              <th style={{ padding: '16px 20px' }}>Correo</th>
              <th style={{ padding: '16px 20px' }}>Estado</th>
              <th style={{ padding: '16px 20px' }}>Registro</th>
              <th style={{ padding: '16px 20px', textAlign: 'right' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {companiesList.map((comp) => (
              <tr key={comp.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                <td style={{ padding: '16px 20px', fontWeight: '600' }}>{comp.companyName || comp.name}</td>
                <td style={{ padding: '16px 20px', color: 'var(--text-muted)' }}>{comp.email}</td>
                <td style={{ padding: '16px 20px' }}>
                  <span className="badge-tag">Verificada</span>
                </td>
                <td style={{ padding: '16px 20px', color: 'var(--text-muted)' }}>{comp.createdAt ? new Date(comp.createdAt).toLocaleDateString() : ''}</td>
                <td style={{ padding: '16px 20px', textAlign: 'right' }}>
                  <button className="btn-b2b-outline" style={{ padding: '4px 12px', fontSize: '0.8rem', marginRight: '8px' }} onClick={() => handleStatusChange(comp, 'ACTIVE', 'Aprobar')}>
                    Aprobar
                  </button>
                  <button className="btn-b2b-outline" style={{ padding: '4px 12px', fontSize: '0.8rem', color: '#EF4444', borderColor: '#EF4444' }} onClick={() => handleStatusChange(comp, 'SUSPENDED', 'Bloquear')}>
                    Bloquear
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Companies;
