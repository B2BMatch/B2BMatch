import React, { useEffect, useState } from 'react';
import perfilesService from '../../services/perfilesService';
import usersService from '../../services/usersService';
import '../../styles/admin.css';
import '../../styles/cards.css';

const getInitials = (name = '') => {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    const first = parts[0]?.[0] || '';
    const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
    return (first + last).toUpperCase() || 'E';
};

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
    <div className="admin-page">
      <header className="admin-head">
        <span className="badge-gold">Admin · Empresas</span>
        <h1>Administrar Empresas</h1>
        <p>Listado de empresas registradas y su estado de verificación</p>
      </header>

      {error && <p style={{ color: 'red', marginBottom: '16px' }}>{error}</p>}

      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Empresa</th>
              <th>Correo</th>
              <th>Estado</th>
              <th>Registro</th>
              <th className="cell-actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {companiesList.map((comp) => {
              const companyName = comp.companyName || comp.name || '';
              return (
                <tr key={comp.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <span className="gig-avatar gig-avatar--sm gig-avatar--navy">{getInitials(companyName)}</span>
                      <span className="cell-strong">{companyName}</span>
                    </div>
                  </td>
                  <td className="cell-muted">{comp.email}</td>
                  <td>
                    <span className="badge-status badge-status--active">Verificada</span>
                  </td>
                  <td className="cell-muted">{comp.createdAt ? new Date(comp.createdAt).toLocaleDateString() : ''}</td>
                  <td className="cell-actions">
                    <button className="btn-b2b-outline" onClick={() => handleStatusChange(comp, 'ACTIVE', 'Aprobar')}>
                      Aprobar
                    </button>
                    <button className="btn-b2b-outline btn-danger" onClick={() => handleStatusChange(comp, 'SUSPENDED', 'Bloquear')}>
                      Bloquear
                    </button>
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

export default Companies;