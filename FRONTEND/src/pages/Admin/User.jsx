import { useEffect, useState } from 'react';
import usersService from '../../services/usersService';
import '../../styles/admin.css';

export const User = () => {
  const [usersList, setUsersList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await usersService.getUsers({ includeDeleted: true });
        setUsersList(Array.isArray(data) ? data : []);
        setError('');
      } catch (err) {
        console.error('Error cargando usuarios', err);
        setError('No se pudieron cargar los usuarios.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [reloadKey]);

  const handleSuspend = async (id) => {
    const confirmed = window.confirm('¿Suspender este usuario? Podrás reactivarlo luego.');
    if (!confirmed) return;
    try {
      await usersService.updateUserStatus(id, 'SUSPENDED');
      alert('Usuario suspendido.');
      setReloadKey((k) => k + 1);
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
      setReloadKey((k) => k + 1);
    } catch (err) {
      console.error(err);
      alert('No se pudo reactivar el usuario.');
    }
  };

  const handleDelete = async (usr) => {
    const confirmed = window.confirm(
      `¿Dar de baja la cuenta de ${usr.email}?\n\n` +
        'Se ocultarán su perfil, sus ofertas y sus servicios, y quedarán suspendidas las ' +
        'postulaciones y cotizaciones que había recibido.\n\n' +
        'Las notificaciones y las reseñas se eliminan de forma permanente y no se pueden ' +
        'recuperar. El resto se puede restaurar con "Restaurar cuenta".'
    );
    if (!confirmed) return;
    try {
      await usersService.deleteUser(usr.id);
      alert('Cuenta dada de baja. Queda disponible para restaurarla.');
      setReloadKey((k) => k + 1);
    } catch (err) {
      console.error(err);
      alert('No se pudo dar de baja la cuenta.');
    }
  };

  const handleRestore = async (usr) => {
    const confirmed = window.confirm(
      `¿Restaurar la cuenta de ${usr.email}?\n\n` +
        'Vuelve a su estado anterior, con su perfil, sus ofertas y sus servicios intactos. ' +
        'Las notificaciones y las reseñas no se recuperan.'
    );
    if (!confirmed) return;
    try {
      const restored = await usersService.reactivateUser(usr.id);
      alert(`Cuenta restaurada en estado ${restored?.status || 'ACTIVE'}.`);
      setReloadKey((k) => k + 1);
    } catch (err) {
      console.error(err);
      alert('No se pudo restaurar la cuenta.');
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando usuarios...</p>;

  // `deletedAt` es la unica fuente de verdad sobre el borrado; `status` es solo
  // estado de negocio. Por eso una cuenta dada de baja conserva su estado.
  const isDeleted = (u) => Boolean(u.deletedAt);
  const deletedUsers = usersList.filter(isDeleted);
  const activeUsers = usersList.filter((u) => !isDeleted(u));

  const statusBadge = (usr) => {
    if (isDeleted(usr)) return { cls: 'badge-status--deleted', label: 'DADO DE BAJA' };
    if (usr.status === 'SUSPENDED') return { cls: 'badge-status--suspended', label: 'SUSPENDIDO' };
    if (usr.status === 'INACTIVE') return { cls: 'badge-status--neutral', label: 'INACTIVO' };
    return { cls: 'badge-status--active', label: 'ACTIVO' };
  };

  const actionsFor = (usr) => {
    if (isDeleted(usr)) {
      return (
        <button className="btn-b2b-primary btn-xs" onClick={() => handleRestore(usr)}>
          Restaurar cuenta
        </button>
      );
    }
    if (usr.status === 'SUSPENDED') {
      return (
        <button className="btn-b2b-outline btn-xs" onClick={() => handleReactivate(usr.id)}>
          Reactivar
        </button>
      );
    }
    return (
      <button className="btn-b2b-outline btn-xs" onClick={() => handleSuspend(usr.id)}>
        Suspender
      </button>
    );
  };

  const rowFor = (usr) => {
    const badge = statusBadge(usr);
    return (
      <tr key={usr.id}>
        <td className="cell-strong">{usr.id}</td>
        <td className="cell-muted">{usr.email}</td>
        <td>
          <span className="badge-status badge-status--neutral">{usr.roleName || usr.role}</span>
        </td>
        <td>
          <span className={`badge-status ${badge.cls}`}>{badge.label}</span>
          {isDeleted(usr) && usr.deletedAt && (
            <div className="cell-muted" style={{ fontSize: '0.78rem', marginTop: '2px' }}>
              {new Date(usr.deletedAt).toLocaleString()}
            </div>
          )}
        </td>
        <td className="cell-actions">
          {actionsFor(usr)}
          {!isDeleted(usr) && (
            <button className="btn-b2b-outline btn-danger btn-xs" onClick={() => handleDelete(usr)}>
              Dar de baja
            </button>
          )}
        </td>
      </tr>
    );
  };

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
          <tbody>{activeUsers.map(rowFor)}</tbody>
        </table>
      </div>

      {deletedUsers.length > 0 && (
        <div style={{ marginTop: '32px' }}>
          <h2 style={{ fontSize: '1.1rem', marginBottom: '4px' }}>Cuentas dadas de baja</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '12px', fontSize: '0.88rem' }}>
            {deletedUsers.length} cuenta{deletedUsers.length === 1 ? '' : 's'} oculta
            {deletedUsers.length === 1 ? '' : 's'} del listado público. Al restaurar, vuelven con su
            estado previo: una cuenta suspendida antes de la baja vuelve suspendida.
          </p>
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
              <tbody>{deletedUsers.map(rowFor)}</tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default User;