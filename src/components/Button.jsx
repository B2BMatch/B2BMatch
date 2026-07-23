import React from 'react';

/**
 * EXPLICACIÓN PARA EL EQUIPO:
 * Botón genérico reutilizable con la identidad visual B2B:
 * - Por defecto: fondo blanco con borde en degradado rojo.
 * - Hover: el fondo cambia al azul corporativo B2B manteniendo el borde rojo.
 */
export const Button = ({ children, onClick, type = 'button', className = '' }) => {
  return (
    <button 
      type={type} 
      className={`btn-b2b ${className}`} 
      onClick={onClick}
    >
      {children}
    </button>
  );
};

export default Button;