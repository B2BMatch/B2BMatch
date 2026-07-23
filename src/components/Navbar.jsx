import React, { useState } from 'react';
import '../styles/navbar.css';

export const Navbar = ({ logo, theme, onToggleTheme, onOpenModal }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => {
    setIsMenuOpen((prev) => !prev);
  };

  const closeMenu = () => {
    setIsMenuOpen(false);
  };

  return (
    <nav className="navbar-b2b">
      {/* Logo */}
      <div className="navbar-logo">
        <img src={logo} alt="B2BMatch" />
      </div>

      {/* Botón Hamburguesa (solo visible en pantallas < 768px) */}
      <button 
        type="button"
        className={`hamburger-btn ${isMenuOpen ? 'open' : ''}`} 
        onClick={toggleMenu}
        aria-label="Abrir menú"
      >
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
      </button>

      {/* Menú principal / Móvil */}
      <div className={`navbar-content ${isMenuOpen ? 'open' : ''}`}>
        <ul className="navbar-links">
          <li><a href="#empleos" onClick={closeMenu}>Empleos</a></li>
          <li><a href="#empresas" onClick={closeMenu}>Empresas</a></li>
          <li><a href="#talento" onClick={closeMenu}>Talento</a></li>
        </ul>

        <div className="navbar-actions">
          <button className="theme-toggle-btn" onClick={onToggleTheme} type="button">
            {theme === 'dark' ? '☀️ Claro' : '🌙 Oscuro'}
          </button>

          <button 
            className="btn-b2b-outline" 
            onClick={() => { onOpenModal('login'); closeMenu(); }}
            type="button"
          >
            Iniciar Sesión
          </button>
          
          <button 
            className="btn-b2b-primary" 
            onClick={() => { onOpenModal('register'); closeMenu(); }}
            type="button"
          >
            Registrarse
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;