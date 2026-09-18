import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import usersService from '../../services/usersService';
import perfilesService from '../../services/perfilesService';
import { getOfertas } from '../../services/ofertasService';
import applicationsService from '../../services/applicationsService';
import '../../styles/admin.css';

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
          { label: 'Usuarios Totales', value: users.length, change: '+12% este mes', accent: 'coral' },
          { label: 'Empresas Registradas', value: companies.length, change: '+5 este mes', accent: 'gold' },
          { label: 'Ofertas Activas', value: offers.length, change: '+18%', accent: 'navy' },
          { label: 'Postulaciones Totales', value: applications.length, change: '+25%', accent: 'gold' }
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
    <div className="admin-page">
      <header className="admin-head">
        <span className="badge-gold">Consola admin</span>
        <h1>Panel de Administración</h1>
        <p>Métricas y gestión global del ecosistema B2B</p>
      </header>

      <div className="stat-grid">
        {stats.map((stat, idx) => (
          <div key={idx} className={`stat-card stat-card--${stat.accent}`}>
            <span className="stat-label">{stat.label}</span>
            <div className="stat-value">{stat.value}</div>
            <span className="stat-change">{stat.change}</span>
          </div>
        ))}
      </div>

      <h2 style={{ fontSize: '1.25rem', margin: '0 0 16px', color: 'var(--text-main)' }}>Módulos de Gestión</h2>
      <div className="module-grid">
        <Link to="/admin/empresas" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="module-card">
            <h3>🏢 Gestión de Empresas</h3>
            <p>Aprobar solicitudes, verificar y suspender cuentas de empresas.</p>
            <span className="module-cta">Ir al módulo →</span>
          </div>
        </Link>

        <Link to="/admin/ofertas" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="module-card">
            <h3>💼 Gestión de Ofertas</h3>
            <p>Supervisar convocatorias activas, moderación y reportes.</p>
            <span className="module-cta">Ir al módulo →</span>
          </div>
        </Link>

        <Link to="/admin/usuarios" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="module-card">
            <h3>👨‍💻 Gestión de Usuarios</h3>
            <p>Administración de candidatos, roles y credenciales.</p>
            <span className="module-cta">Ir al módulo →</span>
          </div>
        </Link>
      </div>
    </div>
  );
};

export default AdminDashboard;