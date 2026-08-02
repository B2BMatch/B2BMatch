import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

export const ProtectedRoute = ({ isAllowed, redirectTo = "/login", children }) => {
  // Si no cumple con los permisos/autenticación, se le redirige
  if (!isAllowed) {
    return <Navigate to={redirectTo} replace />;
  }

  // Si tiene hijos los renderiza, de lo contrario renderiza el <Outlet /> para rutas anidadas
  return children ? children : <Outlet />;
};

export default ProtectedRoute;