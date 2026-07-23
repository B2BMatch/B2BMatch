import React from 'react';

export const Modal = ({ isOpen, onClose, title, subtitle, children }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      {/* e.stopPropagation evita que la modal se cierre al hacer clic adentro */}
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        
        {/* Botón Cerrar (X) */}
        <button className="modal-close-btn" onClick={onClose}>
          &times;
        </button>

        {/* Encabezado */}
        <div className="modal-header">
          {title && <h2>{title}</h2>}
          {subtitle && <p>{subtitle}</p>}
        </div>

        {/* Contenido dinámico */}
        <div className="modal-body">
          {children}
        </div>

      </div>
    </div>
  );
};

export default Modal;