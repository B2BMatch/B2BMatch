import React from 'react';
import '../styles/modal.css';

export const Modal = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose} aria-label="Cerrar">✕</button>
        {title && <h2 className="modal-heading">{title}</h2>}
        {children}
      </div>
    </div>
  );
};

export default Modal;
