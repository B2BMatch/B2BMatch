import React, { useState, useEffect } from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './layouts/MainLayout';
import Modal from './components/Modal';

// Páginas
import Landing from './pages/Landing/Landing';
import { Login } from './pages/Auth/Login';
import Register from './pages/Auth/Register';
import OfertasPage from './pages/OfertasPage';
import ServicesPage from './pages/ServicesPage';
import Applications from './pages/User/Applications';
import UserProfile from './pages/User/UserProfile';
import CompanyProfile from './pages/Company/CompanyProfile';
import CompanyOffers from './pages/Company/CompanyOffers';
import CreateOffer from './pages/Company/CreateOffer';
import EditOffer from './pages/Company/EditOffer';
import AdminDashboard from './pages/Admin/AdminDashboard';
import Companies from './pages/Admin/Companies_new';
import Offers from './pages/Admin/Offers_new';
import UserAdmin from './pages/Admin/User';

import logoB2B from './assets/img/logo.png'; 

export function App() {
  const [theme, setTheme] = useState('dark');
  const [modalState, setModalState] = useState({ isOpen: false, type: null });
  
  // Obtenemos el usuario actual del AuthContext
  const { currentUser } = useAuth();

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const handleToggleTheme = () => setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  const handleOpenModal = (type) => setModalState({ isOpen: true, type });
  const handleCloseModal = () => setModalState({ isOpen: false, type: null });

  return (
    <>
      <Routes>
        <Route element={
          <MainLayout 
            logo={logoB2B} 
            theme={theme} 
            onToggleTheme={handleToggleTheme} 
            onOpenModal={handleOpenModal} 
          />
        }>
          {/* Rutas Públicas */}
          <Route path="/" element={<Landing />} />
          <Route path="/empleos" element={<OfertasPage />} />
          <Route path="/servicios" element={<ServicesPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* 👨‍💻 RUTAS DE CANDIDATO Y ADMIN */}
          <Route 
            element={
              <ProtectedRoute 
                isAllowed={!!currentUser && (currentUser.role === 'candidate' || currentUser.role === 'admin')} 
                redirectTo="/login" 
              />
            }
          >
            <Route path="/mis-postulaciones" element={<Applications />} />
            <Route path="/perfil" element={<UserProfile />} />
          </Route>

          {/* 🏢 RUTAS DE EMPRESA Y ADMIN */}
          <Route 
            element={
              <ProtectedRoute 
                isAllowed={!!currentUser && (currentUser.role === 'company' || currentUser.role === 'admin')} 
                redirectTo="/login" 
              />
            }
          >
            <Route path="/empresa/perfil" element={<CompanyProfile />} />
            <Route path="/empresa/ofertas" element={<CompanyOffers />} />
            <Route path="/empresa/crear-oferta" element={<CreateOffer />} />
            <Route path="/empresa/editar-oferta/:id" element={<EditOffer />} />
          </Route>

          {/* 🛡️ RUTAS EXCLUSIVAS DE ADMINISTRADOR */}
          <Route 
            element={
              <ProtectedRoute 
                isAllowed={!!currentUser && currentUser.role === 'admin'} 
                redirectTo="/" 
              />
            }
          >
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/empresas" element={<Companies />} />
            <Route path="/admin/ofertas" element={<Offers />} />
            <Route path="/admin/usuarios" element={<UserAdmin />} />
          </Route>

          {/* 404 */}
          <Route path="*" element={
            <div style={{ padding: '80px 20px', textAlign: 'center' }}>
              <h1>404</h1>
              <p>Página no encontrada o acceso denegado.</p>
              <Link to="/" className="btn-b2b-primary">Volver al Inicio</Link>
            </div>
          }/>
        </Route>
      </Routes>

      <Modal isOpen={modalState.isOpen} onClose={handleCloseModal}>
        {modalState.type === 'login' && (
          <Login onSwitchToRegister={() => handleOpenModal('register')} onSuccess={handleCloseModal} />
        )}
        {modalState.type === 'register' && (
          <Register onSwitchToLogin={() => handleOpenModal('login')} onSuccess={handleCloseModal} />
        )}
      </Modal>
    </>
  );
}

export default App;