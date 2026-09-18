import React from 'react';
import '../styles/buttons.css';

export const Button = ({ children, variant = 'primary', onClick, type = 'button', disabled, style }) => {
    const className =
        variant === 'outline' ? 'btn-b2b-outline' :
        variant === 'accent' ? 'btn-b2b-accent' :
        'btn-b2b-primary';
    return (
        <button type={type} className={className} onClick={onClick} disabled={disabled} style={style}>
            {children}
        </button>
    );
};

export default Button;