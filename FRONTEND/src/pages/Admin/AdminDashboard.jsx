import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import usersService from '../../services/usersService';
import perfilesService from '../../services/perfilesService';
import { getOfertas } from '../../services/ofertasService';
import applicationsService from '../../services/applicationsService';

export const AdminDashboard = () => {
  const [stats, setStats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadStats = async () => {
      try {
        const [users, companies, offers, applications] = await Promise.all([
          usersService.getUsers(),
          perfilesService.getCompanyProfiles(),
          getOfertas(),
          applicationsService.getApplications()
        ]);

        setStats([
          { label: 'Usuarios Totales', value: users.length, change: '+12% este mes' },
          { label: 'Empresas Registradas', value: companies.length, change: '+5 este mes' },
          { label: 'Ofertas Activas', value: offers.length, change: '+18%' },
          { label: 'Postulaciones Totales', value: applications.length, change: '+25%' }
        ]);
      } catch (err) {
        console.error('Error cargando métricas de administrador', err);
        setError('No se pudieron cargar las métricas de administración.');
      } finally {
        setLoading(false);
      }
    };

    loadStats();
  }, []);

  if (loading) {
    return <p style={{ padding: '20px' }}>Cargando métricas de administración...</p>;
  }

  if (error) {
    return <p style={{ padding: '20px', color: 'red' }}>{error}</p>;
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Panel de Administración</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Métricas y gestión global del ecosistema B2B</p>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '40px' }}>
        {stats.map((stat, idx) => (
          <div key={idx} className="card-b2b" style={{ padding: '20px' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{stat.label}</span>
            <h2 style={{ fontSize: '2rem', margin: '8px 0 4px', fontWeight: '700' }}>{stat.value}</h2>
            <span style={{ color: 'var(--text-accent)', fontSize: '0.8rem' }}>{stat.change}</span>
          </div>
        ))}
      </div>

      <h2 style={{ fontSize: '1.4rem', marginBottom: '16px' }}>Módulos de Gestión</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
        <Link to="/admin/empresas" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="card-b2b card-b2b-lift" style={{ padding: '24px', cursor: 'pointer' }}>
            <h3 style={{ marginBottom: '8px' }}>🏢 Gestión de Empresas</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Aprobar solicitudes, verificar y suspender cuentas de empresas.</p>
          </div>
        </Link>

        <Link to="/admin/ofertas" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="card-b2b card-b2b-lift" style={{ padding: '24px', cursor: 'pointer' }}>
            <h3 style={{ marginBottom: '8px' }}>💼 Gestión de Ofertas</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Supervisar convocatorias activas, moderación y reportes.</p>
          </div>
        </Link>

        <Link to="/admin/usuarios" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="card-b2b card-b2b-lift" style={{ padding: '24px', cursor: 'pointer' }}>
            <h3 style={{ marginBottom: '8px' }}>👨‍💻 Gestión de Usuarios</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Administración de candidatos, roles y credenciales.</p>
          </div>
        </Link>
      </div>
    </div>
  );
};

export default AdminDashboard;