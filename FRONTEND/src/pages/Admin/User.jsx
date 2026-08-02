import React, { useEffect, useState } from 'react';
import usersService from '../../services/usersService';

export const User = () => {
  const [usersList, setUsersList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const data = await usersService.getUsers();
      setUsersList(data || []);
      setError('');
    } catch (err) {
      console.error('Error cargando usuarios', err);
      setError('No se pudieron cargar los usuarios.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleSuspend = async (id) => {
    const confirmed = window.confirm('¿Suspender este usuario? Podrás reactivarlo luego.');
    if (!confirmed) return;
    try {
      await usersService.updateUserStatus(id, 'SUSPENDED');
      alert('Usuario suspendido.');
      load();
    } catch (err) {
      console.error(err);
      alert('No se pudo suspender el usuario.');
    }
  };

  const handleReactivate = async (id) => {
    const confirmed = window.confirm('¿Reactivar este usuario?');
    if (!confirmed) return;
    try {
      await usersService.updateUserStatus(id, 'ACTIVE');
      alert('Usuario reactivado.');
      load();
    } catch (err) {
      console.error(err);
      alert('No se pudo reactivar el usuario.');
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando usuarios...</p>;

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Administrar Usuarios</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Gestión de cuentas de postulantes y perfiles profesionales</p>

      {error && <p style={{ color: 'red', marginBottom: '16px' }}>{error}</p>}

      <div className="card-b2b" style={{ padding: '0', overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
              <th style={{ padding: '16px 20px' }}>ID</th>
              <th style={{ padding: '16px 20px' }}>Correo</th>
              <th style={{ padding: '16px 20px' }}>Rol</th>
              <th style={{ padding: '16px 20px' }}>Estado</th>
              <th style={{ padding: '16px 20px', textAlign: 'right' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {usersList.map((usr) => {
              const isSuspended = usr.status === 'SUSPENDED';
              return (
                <tr key={usr.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '16px 20px', fontWeight: '600' }}>{usr.id}</td>
                  <td style={{ padding: '16px 20px', color: 'var(--text-muted)' }}>{usr.email}</td>
                  <td style={{ padding: '16px 20px' }}>{usr.roleName || usr.role}</td>
                  <td style={{ padding: '16px 20px' }}>
                    <span className="badge-tag">{usr.status}</span>
                  </td>
                  <td style={{ padding: '16px 20px', textAlign: 'right' }}>
                    {isSuspended ? (
                      <button className="btn-b2b-outline" style={{ padding: '4px 12px', fontSize: '0.8rem' }} onClick={() => handleReactivate(usr.id)}>
                        Reactivar
                      </button>
                    ) : (
                      <button className="btn-b2b-outline" style={{ padding: '4px 12px', fontSize: '0.8rem', color: '#EF4444', borderColor: '#EF4444' }} onClick={() => handleSuspend(usr.id)}>
                        Suspender
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

export default User;
