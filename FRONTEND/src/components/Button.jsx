import React from 'react';
import '../styles/buttons.css';

export const Button = ({ children, variant = 'primary', onClick, type = 'button', disabled, style }) => {
    const className = variant === 'primary' ? 'btn-b2b-primary' : 'btn-b2b-outline';
    return (
        <button type={type} className={className} onClick={onClick} disabled={disabled} style={style}>
            {children}
        </button>
    );
};

export default Button;
