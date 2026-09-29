import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  getNotificationsByUser,
  markAsRead,
} from '../services/notificationsService';
import '../styles/navbar.css';
import '../styles/buttons.css';

const fetchNotifications = async (userId) => {
  const list = await getNotificationsByUser(userId);
  const items = Array.isArray(list) ? list : [];
  return [items, items.filter((n) => !n.isRead).length];
};

export const Navbar = ({ logo, theme, onToggleTheme, onOpenModal }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const notifRef = useRef(null);
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!currentUser?.id) return;
    let ignore = false;
    const load = async () => {
      try {
        const [list, count] = await fetchNotifications(currentUser.id);
        if (ignore) return;
        setNotifications(list);
        setUnreadCount(count);
      } catch (err) {
        console.error('Error al cargar notificaciones', err);
      }
    };
    load();
    return () => {
      ignore = true;
    };
  }, [currentUser?.id]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notifRef.current && !notifRef.current.contains(e.target)) {
        setIsNotifOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleMenu = () => setIsMenuOpen((prev) => !prev);
  const closeMenu = () => setIsMenuOpen(false);

  const handleLogout = () => {
    logout();
    closeMenu();
    navigate('/');
  };

  const handleToggleNotif = async () => {
    const next = !isNotifOpen;
    setIsNotifOpen(next);
    if (!next || !currentUser?.id) return;
    try {
      const [list, count] = await fetchNotifications(currentUser.id);
      setNotifications(list);
      setUnreadCount(count);
    } catch (err) {
      console.error('Error al cargar notificaciones', err);
    }
  };

  const handleMarkRead = async (id) => {
    const wasUnread = notifications.some((n) => n.id === id && !n.isRead);
    try {
      await markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      if (wasUnread) setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Error al marcar leída', err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      const unread = notifications.filter((n) => !n.isRead);
      await Promise.all(unread.map((n) => markAsRead(n.id)));
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Error al marcar todas leídas', err);
    }
  };

  return (
    <nav className="navbar-b2b">
      <Link to="/" className="navbar-logo" onClick={closeMenu}>
        <img src={logo} alt="B2BMatch" />
      </Link>

      <button
        type="button"
        className={`hamburger-btn ${isMenuOpen ? 'open' : ''}`}
        onClick={toggleMenu}
      >
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
      </button>

      <div className={`navbar-content ${isMenuOpen ? 'open' : ''}`}>
        <ul className="navbar-links">
          {/* Rutas Públicas */}
          <li><Link to="/empleos" onClick={closeMenu}>Empleos</Link></li>
          <li><Link to="/servicios" onClick={closeMenu}>Servicios</Link></li>

          {/* 👨‍💻 Rutas de Postulante / Candidato */}
          {currentUser?.role === 'candidate' && (
            <>
              <li><Link to="/mis-postulaciones" onClick={closeMenu}>Mis Postulaciones</Link></li>
              <li><Link to="/perfil" onClick={closeMenu}>Mi Perfil</Link></li>
            </>
          )}

          {/* 🏢 Rutas de Empresa */}
          {currentUser?.role === 'company' && (
            <>
              <li><Link to="/empresa/ofertas" onClick={closeMenu}>Mis Ofertas</Link></li>
              <li><Link to="/empresa/perfil" onClick={closeMenu}>Perfil Empresa</Link></li>
            </>
          )}

          {/* 🛡️ Rutas de Administrador */}
          {currentUser?.role === 'admin' && (
            <>
              <li><Link to="/admin" onClick={closeMenu}>Dashboard Admin</Link></li>
              <li><Link to="/admin/empresas" onClick={closeMenu}>Empresas</Link></li>
              <li><Link to="/admin/ofertas" onClick={closeMenu}>Ofertas</Link></li>
              <li><Link to="/admin/usuarios" onClick={closeMenu}>Usuarios</Link></li>
            </>
          )}
        </ul>

        <div className="navbar-actions">
          <button className="theme-toggle-btn" onClick={onToggleTheme} type="button">
            {theme === 'dark' ? '☀️' : '🌙'}
          </button>

          {currentUser ? (
            <>
              <div className="notif-bell-wrap" ref={notifRef}>
                <button
                  className="notif-bell"
                  onClick={handleToggleNotif}
                  type="button"
                  aria-label="Notificaciones"
                >
                  🔔
                  {unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
                </button>

                {isNotifOpen && (
                  <div className="notif-dropdown">
                    <div className="notif-header">
                      <span className="notif-title">Notificaciones</span>
                      {unreadCount > 0 && (
                        <button className="notif-mark-all" onClick={handleMarkAllRead} type="button">
                          Marcar todas leídas
                        </button>
                      )}
                    </div>

                    <div className="notif-list">
                      {notifications.length === 0 ? (
                        <p className="notif-empty">No tienes notificaciones</p>
                      ) : (
                        notifications.map((n) => (
                          <button
                            key={n.id}
                            className={`notif-item ${n.isRead ? 'read' : 'unread'}`}
                            onClick={() => handleMarkRead(n.id)}
                            type="button"
                          >
                            <div className="notif-item-title">
                              {n.title}
                              {!n.isRead && <span className="notif-dot"></span>}
                            </div>
                            {n.message && <div className="notif-item-message">{n.message}</div>}
                            <div className="notif-item-date">
                              {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                            </div>
                          </button>
                        ))
                      )}
                    </div>
                  </div>
                )}
              </div>

              <button className="btn-b2b-outline" onClick={handleLogout} type="button">
                Cerrar Sesión ({currentUser.name || currentUser.email})
              </button>
            </>
          ) : (
            <>
              <button className="btn-b2b-outline" onClick={() => onOpenModal('login')} type="button">
                Iniciar Sesión
              </button>
              <button className="btn-b2b-primary" onClick={() => onOpenModal('register')} type="button">
                Registrarse
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
