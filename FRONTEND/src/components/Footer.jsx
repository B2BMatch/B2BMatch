import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/footer.css';

export const Footer = () => {
  return (
    <footer className="footer-b2b">
      <p style={{ marginBottom: '8px' }}>
        <strong>B2BMatch</strong> — Servicios profesionales y talento B2B verificados.
      </p>
      <p style={{ fontSize: '0.82rem', marginBottom: '10px' }}>
        <Link to="/servicios">Servicios</Link> · <Link to="/empleos">Empleos</Link> ·{' '}
        <Link to="/login">Ingresar</Link>
      </p>
      <p style={{ fontSize: '0.78rem', opacity: 0.7 }}>© 2026 B2BMatch. Todos los derechos reservados.</p>
    </footer>
  );
};

export default Footer;