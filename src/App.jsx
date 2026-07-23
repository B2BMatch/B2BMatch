import React, { useState, useEffect } from 'react';
import './styles/global.css';
import logo from './assets/img/logo.png';

// Importa el componente Navbar que creamos previamente
import Navbar from './components/Navbar'; 
import JobCard from './components/JobCard';
import CompanyCard from './components/CompanyCard';
import UserCard from './components/UserCard';

function App() {
  const [activeModal, setActiveModal] = useState(null);
  
  // Estado para el tema (Claro u Oscuro)
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'dark';
  });

  // Efecto para aplicar el atributo en <html> y guardar en localStorage
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prevTheme) => (prevTheme === 'dark' ? 'light' : 'dark'));
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      
      {/* 1. NAVBAR REUTILIZABLE (AQUÍ ESTÁ EL CAMBIO) */}
      <Navbar 
        logo={logo}
        theme={theme}
        onToggleTheme={toggleTheme}
        onOpenModal={(type) => setActiveModal(type)}
      />

      {/* 2. HERO SECTION */}
      <header style={{ textAlign: 'center', padding: '70px 20px 50px', maxWidth: '850px', margin: '0 auto' }}>
        <span className="badge-tag">✨ Portal B2B Interactivo</span>
        
        <h1 style={{ fontSize: '3rem', fontWeight: '800', margin: '18px 0 14px', color: 'var(--text-main)' }}>
          Conecta tu Empresa con el <span style={{ color: 'var(--red-glow)' }}>Mejor Talento</span>
        </h1>
        
        <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem', marginBottom: '28px' }}>
          La red líder para conectar profesionales, negocios y oportunidades laborales en tiempo real.
        </p>

        {/* BUSCADOR DUAL */}
        <div style={{
          background: 'var(--bg-card)',
          padding: '8px 12px',
          borderRadius: '14px',
          border: '1px solid var(--border-color)',
          display: 'flex',
          gap: '12px',
          boxShadow: 'var(--shadow-card)'
        }}>
          <input 
            type="text" 
            placeholder="¿Qué puesto o empresa estás buscando?..." 
            className="input-b2b"
            style={{ border: 'none', background: 'transparent' }}
          />
          <button className="btn-b2b-primary">Buscar</button>
        </div>
      </header>

      {/* 3. SECCIONES CON TARJETAS DUALES */}
      <main style={{ flex: 1, maxWidth: '1200px', width: '100%', margin: '0 auto', padding: '0 20px 60px' }}>
        
        {/* EMPLEOS */}
        <section id="empleos" style={{ marginBottom: '50px' }}>
          <h2 style={{ fontSize: '1.5rem', marginBottom: '20px', color: 'var(--text-main)' }}>💼 Ofertas Destacadas</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '24px' }}>
            <JobCard 
              title="Líder Frontend React"
              company="TechCorp Chile"
              location="Santiago (Híbrido)"
              salary="$3.200.000 CLP"
              tags={['Full-time', 'React', 'Senior']}
              onApply={() => alert('Postulación enviada')}
            />
            <JobCard 
              title="Consultor B2B & Process Manager"
              company="Logística Global SpA"
              location="Valparaíso (Remoto)"
              salary="$2.400.000 CLP"
              tags={['Consultoría', 'B2B']}
              onApply={() => alert('Postulación enviada')}
            />
          </div>
        </section>

        {/* EMPRESAS */}
        <section id="empresas" style={{ marginBottom: '50px' }}>
          <h2 style={{ fontSize: '1.5rem', marginBottom: '20px', color: 'var(--text-main)' }}>🏢 Empresas Aliadas</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '24px' }}>
            <CompanyCard 
              name="Inversiones Andes SpA"
              industry="Fintech & SaaS"
              employees="100-250"
              description="Infraestructura de pagos digitales para pymes en Latinoamérica."
              onContact={() => alert('Viendo perfil')}
            />
            <CompanyCard 
              name="Global Freight"
              industry="Logística Internacional"
              employees="500+"
              description="Líderes en cadena de suministro e integración B2B regional."
              onContact={() => alert('Viendo perfil')}
            />
          </div>
        </section>

        {/* TALENTO */}
        <section id="talento">
          <h2 style={{ fontSize: '1.5rem', marginBottom: '20px', color: 'var(--text-main)' }}>👤 Talento Destacado</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '24px' }}>
            <UserCard 
              name="Camila Valenzuela"
              role="Product Designer Senior"
              email="c.valenzuela@ejemplo.com"
              skills={['Figma', 'UI/UX', 'Design Systems']}
              onProfile={() => alert('Ver portafolio')}
            />
            <UserCard 
              name="Gonzalo Morales"
              role="DevOps & Cloud Engineer"
              email="g.morales@ejemplo.com"
              skills={['AWS', 'Docker', 'Kubernetes']}
              onProfile={() => alert('Ver portafolio')}
            />
          </div>
        </section>

      </main>

      {/* 4. MODAL INTERACTIVA */}
      {activeModal && (
        <div className="modal-overlay" onClick={() => setActiveModal(null)}>
          <div className="modal-content-b2b" onClick={(e) => e.stopPropagation()}>
            <h2 style={{ fontSize: '1.5rem', marginBottom: '8px', color: 'var(--text-main)' }}>
              {activeModal === 'login' ? 'Iniciar Sesión' : 'Crear Cuenta B2B'}
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '24px' }}>
              {activeModal === 'login' ? 'Ingresa tus credenciales para continuar.' : 'Únete a la red empresarial de B2BMatch.'}
            </p>

            <form onSubmit={(e) => { e.preventDefault(); setActiveModal(null); }} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                  Correo Electrónico
                </label>
                <input type="email" placeholder="nombre@empresa.com" className="input-b2b" required />
              </div>

              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                  Contraseña
                </label>
                <input type="password" placeholder="••••••••" className="input-b2b" required />
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '12px' }}>
                <button type="button" className="btn-b2b-outline" style={{ flex: 1 }} onClick={() => setActiveModal(null)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-b2b-primary" style={{ flex: 1 }}>
                  {activeModal === 'login' ? 'Ingresar' : 'Registrarme'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 5. FOOTER */}
      <footer style={{ 
        borderTop: '1px solid var(--border-color)', 
        padding: '28px 20px', 
        textAlign: 'center', 
        color: 'var(--text-muted)',
        fontSize: '0.9rem',
        background: 'var(--bg-card)'
      }}>
        <p>© {new Date().getFullYear()} B2BMatch. Todos los derechos reservados.</p>
      </footer>

    </div>
  );
}

export default App;