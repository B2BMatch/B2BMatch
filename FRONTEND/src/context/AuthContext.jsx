import React, { createContext, useState, useEffect, useContext } from 'react';
import { login as loginService } from '../services/authService';

const AuthContext = createContext();

const normalizeUser = (backendUser) => {
  if (!backendUser) return null;

  const roleName = (backendUser.roleName || backendUser.role || '').toUpperCase();
  let normalizedRole;
  if (roleName === 'COMPANY') {
    normalizedRole = 'company';
  } else if (roleName === 'ADMIN') {
    normalizedRole = 'admin';
  } else if (roleName === 'CUSTOMER') {
    normalizedRole = 'candidate';
  } else {
    normalizedRole = 'candidate'; // PROFESSIONAL y otros
  }

  const name = backendUser.name || backendUser.email?.split('@')[0] || backendUser.email;

  return {
    ...backendUser,
    roleName,
    role: normalizedRole,
    name,
  };
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(normalizeUser(JSON.parse(storedUser)));
    }
    setLoading(false);
  }, []);

  const loginUser = async (credentials) => {
    const data = await loginService(credentials);
    const { token, ...userData } = data || {};

    if (token) {
      localStorage.setItem('token', token);
    }

    const normalized = normalizeUser(userData);

    if (normalized) {
      localStorage.setItem('user', JSON.stringify(normalized));
      setUser(normalized);
    }

    return normalized;
  };

  const logoutUser = () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ currentUser: user, user, isAuthenticated: !!user, loginUser, logoutUser, logout: logoutUser, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

// Hook personalizado para usar el contexto más fácil en cualquier componente
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser usado dentro de un AuthProvider');
  }
  return context;
};