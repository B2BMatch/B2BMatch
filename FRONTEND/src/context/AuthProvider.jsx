import { useState } from 'react';
import { AuthContext, normalizeUser } from './AuthContext';
import { login as loginService } from '../services/authService';

const getStoredUser = () => {
  try {
    const storedUser = localStorage.getItem('user');
    return storedUser ? normalizeUser(JSON.parse(storedUser)) : null;
  } catch {
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(getStoredUser);

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
    <AuthContext.Provider value={{ currentUser: user, user, isAuthenticated: !!user, loginUser, logoutUser, logout: logoutUser }}>
      {children}
    </AuthContext.Provider>
  );
};
