import { createContext, useContext } from 'react';

export const AuthContext = createContext();

export const normalizeUser = (backendUser) => {
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

// Hook personalizado para usar el contexto más fácil en cualquier componente
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser usado dentro de un AuthProvider');
  }
  return context;
};
