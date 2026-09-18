import React, { useEffect, useState } from 'react';
import usersService from '../../services/usersService';
import '../../styles/admin.css';

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
    <div className="admin-page">
      <header className="admin-head">
        <span className="badge-gold">Admin · Usuarios</span>
        <h1>Administrar Usuarios</h1>
        <p>Gestión de cuentas de postulantes y perfiles profesionales</p>
      </header>

      {error && <p style={{ color: 'red', marginBottom: '16px' }}>{error}</p>}

      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Correo</th>
              <th>Rol</th>
              <th>Estado</th>
              <th className="cell-actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {usersList.map((usr) => {
              const isSuspended = usr.status === 'SUSPENDED';
              return (
                <tr key={usr.id}>
                  <td className="cell-strong">{usr.id}</td>
                  <td className="cell-muted">{usr.email}</td>
                  <td>
                    <span className="badge-status badge-status--neutral">{usr.roleName || usr.role}</span>
                  </td>
                  <td>
                    <span className={`badge-status ${isSuspended ? 'badge-status--suspended' : 'badge-status--active'}`}>
                      {usr.status}
                    </span>
                  </td>
                  <td className="cell-actions">
                    {isSuspended ? (
                      <button className="btn-b2b-outline" onClick={() => handleReactivate(usr.id)}>
                        Reactivar
                      </button>
                    ) : (
                      <button className="btn-b2b-outline btn-danger" onClick={() => handleSuspend(usr.id)}>
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