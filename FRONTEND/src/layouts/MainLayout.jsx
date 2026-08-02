import React from 'react';
import { Outlet } from 'react-router-dom'; // 👈 ¡SÚPER IMPORTANTE!
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

const MainLayout = ({ logo, theme, onToggleTheme, onOpenModal }) => {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', background: 'var(--bg-main)' }}>
      {/* Navbar en la parte superior */}
      <Navbar 
        logo={logo} 
        theme={theme} 
        onToggleTheme={onToggleTheme} 
        onOpenModal={onOpenModal} 
      />

      {/* AQUÍ ES DONDE SE RENDERIZAN LAS PÁGINAS (Landing, Login, etc.) */}
      <main style={{ flex: 1 }}>
        <Outlet /> {/* 👈 Si falta esto, la pantalla se queda vacía/azul */}
      </main>

      {/* Footer al final */}
      <Footer logo={logo} />
    </div>
  );
};

export default MainLayout;