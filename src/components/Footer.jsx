import React from 'react';

export const Footer = () => {
  return (
    <footer className="footer-b2b">
      <div className="footer-content">
        {/* Columna 1: Info General */}
        <div className="footer-section">
          <h4>Portal B2B</h4>
          <p>
            Plataforma corporativa para la gestión de oportunidades laborales, alianzas de empresas y talento profesional.
          </p>
        </div>

        {/* Columna 2: Enlaces Rápidos */}
        <div className="footer-section">
          <h4>Navegación</h4>
          <ul>
            <li><a href="#jobs">Ofertas de Empleo</a></li>
            <li><a href="#companies">Directorio de Empresas</a></li>
            <li><a href="#talents">Red de Candidatos</a></li>
          </ul>
        </div>

        {/* Columna 3: Soporte */}
        <div className="footer-section">
          <h4>Soporte & Legal</h4>
          <ul>
            <li><a href="#help">Centro de Ayuda</a></li>
            <li><a href="#privacy">Política de Privacidad</a></li>
            <li><a href="#terms">Términos del Servicio</a></li>
          </ul>
        </div>
      </div>

      {/* Copyright Bar */}
      <div className="footer-bottom">
        <p>© {new Date().getFullYear()} Portal B2B. Todos los derechos reservados.</p>
        <p style={{ color: '#64748B' }}>Diseñado con React & CSS B2B System</p>
      </div>
    </footer>
  );
};

export default Footer;