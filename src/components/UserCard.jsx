import React from 'react';

export const UserCard = ({ name, role, email, skills = [], onProfile }) => {
  return (
    <div className="card-b2b">
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '16px' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '12px',
            background: 'linear-gradient(135deg, var(--red-glow), var(--bg-main))',
            color: '#FFFFFF',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: '800',
            fontSize: '1.2rem',
            boxShadow: '0 0 15px rgba(229, 62, 62, 0.4)'
          }}>
            {name ? name.charAt(0) : 'U'}
          </div>
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--text-title)' }}>{name}</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{role}</p>
          </div>
        </div>

        <p style={{ fontSize: '0.85rem', color: 'var(--text-sub)', marginBottom: '16px' }}>✉️ {email}</p>

        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginBottom: '20px' }}>
          {skills.map((skill, idx) => (
            <span key={idx} style={{
              fontSize: '0.75rem',
              background: 'var(--bg-input)',
              color: 'var(--text-main)',
              padding: '4px 10px',
              borderRadius: '6px',
              border: '1px solid var(--border-color)'
            }}>
              {skill}
            </span>
          ))}
        </div>
      </div>

      <button className="btn-b2b-outline" style={{ width: '100%' }} onClick={onProfile}>
        Ver Portafolio
      </button>
    </div>
  );
};

export default UserCard;