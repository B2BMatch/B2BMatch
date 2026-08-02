import React from 'react';
import '../styles/cards.css';

export const UserCard = ({ name, role, skills }) => {
    return (
        <div className="card-b2b">
            <span className="badge-tag">Talento</span>
            <h3 style={{ marginTop: '10px' }}>{name || 'Alex Rodríguez'}</h3>
            <p style={{ color: 'var(--text-accent)' }}>{role || 'UI/UX Designer & Dev'}</p>
            <p style={{ marginTop: '10px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                ⚡ {skills || 'React, CSS Modules, Figma'}
            </p>
        </div>
    );
};

export default UserCard;